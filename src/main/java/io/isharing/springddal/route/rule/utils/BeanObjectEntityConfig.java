package io.isharing.springddal.route.rule.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.isharing.springddal.route.exception.ConfigurationException;


public class BeanObjectEntityConfig implements Cloneable {
	
    private static final ReflectionProvider refProvider = new ReflectionProvider();

    private String name;
    private String className;
    private Map<String, Object> params = new HashMap<String, Object>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String beanObject) {
        this.className = beanObject;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object create(boolean initEarly) throws IllegalAccessException, InvocationTargetException {
        Object obj = null;
        try {
            obj = refProvider.newInstance(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
        ParameterMapping.mapping(obj, params);
        if (initEarly && (obj instanceof Initializable)) {
            ((Initializable) obj).init();
        }
        return obj;
    }

    @Override
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ConfigurationException(e);
        }
        BeanObjectEntityConfig bc = null;
        try {
            bc = getClass().newInstance();
        } catch (InstantiationException e) {
            throw new ConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(e);
        }
//        if (bc == null) {
//            return null;
//        }
        bc.className = className;
        bc.name = name;
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.putAll(params);
        return bc;
    }

    @Override
    public int hashCode() {
        int hashcode = 37;
        hashcode += (name == null ? 0 : name.hashCode());
        hashcode += (className == null ? 0 : className.hashCode());
        hashcode += (params == null ? 0 : params.hashCode());
        return hashcode;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof BeanObjectEntityConfig) {
            BeanObjectEntityConfig entity = (BeanObjectEntityConfig) object;
            boolean isEquals = equals(name, entity.name);
            isEquals = isEquals && equals(className, entity.getClassName());
            isEquals = isEquals && (ObjectUtil.equals(params, entity.params));
            return isEquals;
        }
        return false;
    }

    private static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

}