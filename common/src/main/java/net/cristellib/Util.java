package net.cristellib;

import net.minecraft.resources.ResourceLocation;

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

    public static ResourceLocation getLocationForStructureSet(ResourceLocation location){
        return createJsonLocation("worldgen/structure_set", location);
    }

    public static ResourceLocation createJsonLocation(String prefix, ResourceLocation identifier) {
        return createResourceLocation(prefix, "json", identifier);
    }

    public static ResourceLocation createResourceLocation(String prefix, String end, ResourceLocation identifier) {
        return new ResourceLocation(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end);
    }

}
