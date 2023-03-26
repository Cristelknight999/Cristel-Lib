package net.cristellib;

import java.util.Map;
import java.util.Set;

public class Util {

    public static <V, S> void addAll(Map<V, Set<S>> addTo, Map<V, Set<S>> addFrom){
        for(V string : addFrom.keySet()){
            if(addTo.containsKey(string)){
                Set<S> s = addTo.get(string);
                s.addAll(addFrom.get(string));
                addTo.put(string, s);
            }
            else {
                addTo.put(string, addFrom.get(string));
            }
        }
    }

}
