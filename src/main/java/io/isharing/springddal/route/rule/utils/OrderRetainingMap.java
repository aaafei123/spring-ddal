package io.isharing.springddal.route.rule.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class OrderRetainingMap<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 8538722653898458547L;
	private ArraySet<K> keyOrder = new ArraySet<K>();
    private List<V> valueOrder = new ArrayList<V>();
 
    public OrderRetainingMap() {
        super();
    }
 
    public OrderRetainingMap(Map<K, V> m) {
        super();
        putAll(m);
    }
 
    public V put(K key, V value) {
        int idx = keyOrder.lastIndexOf(key);
        if (idx < 0) {
            keyOrder.add(key);
            valueOrder.add(value);
        } else {
            valueOrder.set(idx, value);
        }
        return super.put(key, value);
    }
 
    public V remove(Object key) {
        int idx = keyOrder.lastIndexOf(key);
        if (idx != 0) {
            keyOrder.remove(idx);
            valueOrder.remove(idx);
        }
        return super.remove(key);
    }
 
    public void clear() {
        keyOrder.clear();
        valueOrder.clear();
        super.clear();
    }
 
    public Collection<V> values() {
        return Collections.unmodifiableList(valueOrder);
    }
 
    public Set<K> keySet() {
        return Collections.unmodifiableSet(keyOrder);
    }
 
    public Set<Entry<K, V>> entrySet() {
        Map.Entry<K,V>[] entries = new Map.Entry[size()];
        for (Iterator<Entry<K, V>> iter = super.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<K,V> entry = (Map.Entry<K,V>)iter.next();
            entries[keyOrder.indexOf(entry.getKey())] = entry;
        }
        Set<Entry<K, V>> set = new ArraySet<Entry<K, V>>();
        set.addAll(Arrays.asList(entries));
        return Collections.unmodifiableSet(set);
    }
 
    private static class ArraySet<K> extends ArrayList<K> implements Set<K> {
		private static final long serialVersionUID = 3153152900548049706L;
    }
}