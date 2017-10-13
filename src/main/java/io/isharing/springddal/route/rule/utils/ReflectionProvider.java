/*
* Copyright (C) 2017 ChenFei, All Rights Reserved
*
* This program is free software; you can redistribute it and/or modify it 
* under the terms of the GNU General Public License as published by the Free 
* Software Foundation; either version 3 of the License, or (at your option) 
* any later version.
*
* This program is distributed in the hope that it will be useful, but 
* WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
* or FITNESS FOR A PARTICULAR PURPOSE. 
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, see <http://www.gnu.org/licenses>.
*
* This code is available under licenses for commercial use. Please contact
* ChenFei for more information.
*
* http://www.gplgpu.com
* http://www.chenfei.me
*
* Title       :  Spring DDAL
* Author      :  Chen Fei
* Email       :  cn.fei.chen@qq.com
*
*/
package io.isharing.springddal.route.rule.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.isharing.springddal.route.exception.ObjectAccessException;

public class ReflectionProvider {

    private transient Map<Class<?>, byte[]> serializedDataCache = Collections.synchronizedMap(new HashMap<Class<?>, byte[]>());
    private transient FieldDictionary fieldDictionary = new FieldDictionary();

    public Object newInstance(Class<?> type) {
        try {
            Constructor<?>[] c = type.getDeclaredConstructors();
            for (int i = 0; i < c.length; i++) {
                if (c[i].getParameterTypes().length == 0) {
                    if (!Modifier.isPublic(c[i].getModifiers())) {
                        c[i].setAccessible(true);
                    }
                    return c[i].newInstance(new Object[0]);
                }
            }
            if (Serializable.class.isAssignableFrom(type)) {
                return instantiateUsingSerialization(type);
            } else {
                throw new ObjectAccessException("Cannot construct " + type.getName()
                        + " as it does not have a no-args constructor");
            }
        } catch (InstantiationException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        } catch (IllegalAccessException e) {
            throw new ObjectAccessException("Cannot construct " + type.getName(), e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else if (e.getTargetException() instanceof Error) {
                throw (Error) e.getTargetException();
            } else {
                throw new ObjectAccessException("Constructor for " + type.getName() + " threw an exception", e.getTargetException());
            }
        }
    }

    public void visitSerializableFields(Object object, Visitor visitor) {
        for (Iterator<Field> iterator = fieldDictionary.serializableFieldsFor(object.getClass()); iterator.hasNext();) {
            Field field = iterator.next();
            if (!fieldModifiersSupported(field)) {
                continue;
            }
            validateFieldAccess(field);
            try {
                Object value = field.get(object);
                visitor.visit(field.getName(), field.getType(), field.getDeclaringClass(), value);
            } catch (IllegalArgumentException e) {
                throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
            } catch (IllegalAccessException e) {
                throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
            }
        }
    }

    public void writeField(Object object, String fieldName, Object value, Class<?> definedIn) {
        Field field = fieldDictionary.field(object.getClass(), fieldName, definedIn);
        validateFieldAccess(field);
        try {
            field.set(object, value);
        } catch (IllegalArgumentException e) {
            throw new ObjectAccessException("Could not set field " + field.getName() + "@" + object.getClass(), e);
        } catch (IllegalAccessException e) {
            throw new ObjectAccessException("Could not set field " + field.getName() + "@" + object.getClass(), e);
        }
    }

    public void invokeMethod(Object object, String methodName, Object value, Class<?> definedIn) {
        try {
            Method method = object.getClass().getMethod(methodName, new Class[] { value.getClass() });
            method.invoke(object, new Object[] { value });
        } catch (Exception e) {
            throw new ObjectAccessException("Could not invoke " + object.getClass() + "." + methodName, e);
        }
    }

    public Class<?> getFieldType(Object object, String fieldName, Class<?> definedIn) {
        return fieldDictionary.field(object.getClass(), fieldName, definedIn).getType();
    }

    public boolean fieldDefinedInClass(String fieldName, Class<?> type) {
        try {
            Field field = fieldDictionary.field(type, fieldName, null);
            return fieldModifiersSupported(field);
        } catch (ObjectAccessException e) {
            return false;
        }
    }

    public Field getField(Class<?> definedIn, String fieldName) {
        return fieldDictionary.field(definedIn, fieldName, null);
    }

    private Object instantiateUsingSerialization(Class<?> type) {
        try {
            byte[] data;
            if (serializedDataCache.containsKey(type)) {
                data = serializedDataCache.get(type);
            } else {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(bytes);
                stream.writeShort(ObjectStreamConstants.STREAM_MAGIC);
                stream.writeShort(ObjectStreamConstants.STREAM_VERSION);
                stream.writeByte(ObjectStreamConstants.TC_OBJECT);
                stream.writeByte(ObjectStreamConstants.TC_CLASSDESC);
                stream.writeUTF(type.getName());
                stream.writeLong(ObjectStreamClass.lookup(type).getSerialVersionUID());
                stream.writeByte(2); // classDescFlags (2 = Serializable)
                stream.writeShort(0); // field count
                stream.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);
                stream.writeByte(ObjectStreamConstants.TC_NULL);
                data = bytes.toByteArray();
                serializedDataCache.put(type, data);
            }

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            return in.readObject();
        } catch (IOException e) {
            throw new ObjectAccessException("Cannot create " + type.getName() + " by JDK serialization", e);
        } catch (ClassNotFoundException e) {
            throw new ObjectAccessException("Cannot find class " + e.getMessage());
        }
    }

    private boolean fieldModifiersSupported(Field field) {
        return !(Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()));
    }

    private void validateFieldAccess(Field field) {
        if (Modifier.isFinal(field.getModifiers())) {
            if (JVMInfo.is15()) {
                field.setAccessible(true);
            } else {
                throw new ObjectAccessException("Invalid final field " + field.getDeclaringClass().getName() + "."
                        + field.getName());
            }
        }
    }

    private Object readResolve() {
        serializedDataCache = Collections.synchronizedMap(new HashMap<Class<?>, byte[]>());
        fieldDictionary = new FieldDictionary();
        return this;
    }

}