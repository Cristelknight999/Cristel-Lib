package de.cristelknight.cristellib.config.serialize.ed;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EDUtil {

    public static Map<String, Boolean> stringBooleanMap(NestedEDConfig edConfig, String parent) {
        Map<String, Boolean> map = new HashMap<>();
        for (Map.Entry<String, NestedEDConfig.Entry> entry : edConfig.getEntries().entrySet()) {
            String key = entry.getKey();
            NestedEDConfig.Entry object = entry.getValue();

            String finalKey = parent.isEmpty() ? key : parent + "/" + key;
            if (object.isBoolean()) {
                map.put(finalKey, object.getValue());
            } else {
                map.putAll(stringBooleanMap(object.getNested(), key));
            }
        }
        return map;
    }

    public static Map<String, NestedEDConfig> mapToNestedStructures(Map<ResourceLocation, List<String>> sets) {
        Map<String, NestedEDConfig> nestedStructures = new HashMap<>();

        for (Map.Entry<ResourceLocation, List<String>> mapEntry : sets.entrySet()) {
            ResourceLocation location = mapEntry.getKey();
            List<String> stringList = mapEntry.getValue();

            // Prepare the map for the NestedStructure
            Map<String, NestedEDConfig.Entry> entries = new HashMap<>();

            // Process each string in the list
            for (String structure : stringList) {
                String structureName = structure.split(":")[1];
                putStructureName(structureName, entries);
            }

            // Create NestedStructure for this ResourceLocation
            NestedEDConfig nestedStructure = new NestedEDConfig(entries);
            nestedStructures.put(location.toString().split(":")[1], nestedStructure);
        }

        return nestedStructures;
    }

    public static void putStructureName(String structureName, Map<String, NestedEDConfig.Entry> entries){
        if (structureName.contains("/")) {
            // Handle key-value pair scenario
            String[] parts = structureName.split("/", 2);
            String key = parts[0];
            String restOfStructureName = parts[1];

            boolean containsNestedStructure = entries.containsKey(key);
            Map<String, NestedEDConfig.Entry> nestedEntries;
            if(containsNestedStructure) nestedEntries = entries.get(key).getNested().getEntries();
            else nestedEntries = new HashMap<>();

            putStructureName(restOfStructureName, nestedEntries);
            if(!containsNestedStructure) entries.put(key, NestedEDConfig.Entry.ofNested(new NestedEDConfig(nestedEntries)));

        } else {
            // Handle simple key-value pair where value is always true
            entries.put(structureName, NestedEDConfig.Entry.ofBoolean(true));
        }
    }

}
