package de.tu_dresden.inf.lat.data;

import java.util.*;

public class MultiMap<KEY,VALUE> {

    private final Map<KEY, Set<VALUE>> inner = new HashMap<>();

    public Set<VALUE> get(KEY key) {
        if(inner.containsKey(key))
        return Collections.unmodifiableSet(inner.get(key));
        else
            return Collections.emptySet();
    }

    public void add(KEY key, VALUE value) {
        if(!inner.containsKey(key))
            inner.put(key, new HashSet<>());
        inner.get(key).add(value);
    }

    public boolean hasKey(KEY key){
        return inner.containsKey(key);
    }

    public Set<KEY> keySet() {
        return inner.keySet();
    }
}
