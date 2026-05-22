package de.cristelknight.cristellib.config.structure.toggle;

import de.cristelknight.cristellib.StructureConfigToggle;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToggleConfigTransformer {

    public static Map<String, Boolean> stringBooleanMap(NestedToggleConfig toggleConfig, String parent) {
        Map<String, Boolean> map = new HashMap<>();
        for (Map.Entry<String, NestedToggleConfig.Entry> entry : toggleConfig.entries().entrySet()) {
            String key = entry.getKey();
            NestedToggleConfig.Entry object = entry.getValue();

            String finalKey = parent.isEmpty() ? key : parent + "/" + key;
            if (object.isBoolean()) {
                map.put(finalKey, object.value());
            } else {
                map.putAll(stringBooleanMap(object.nested(), finalKey));
            }
        }
        return map;
    }

    public static Map<String, NestedToggleConfig> mapToNestedStructures(StructureConfigToggle structureConfig) {
        Map<ResourceLocation, List<ResourceLocation>> defaultStructures = structureConfig.getDefaultStructureToggles();

        Map<String, NestedToggleConfig> nestedStructures = new HashMap<>();
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> mapEntry : defaultStructures.entrySet()) {
            ResourceLocation location = mapEntry.getKey();
            List<ResourceLocation> stringList = mapEntry.getValue();

            // Prepare the map for the NestedStructure
            Map<String, NestedToggleConfig.Entry> entries = new HashMap<>();

            // Process each string in the list
            for (ResourceLocation structure : stringList) {
                String structureName = structureConfig.toDefaultString(structure);
                putStructureName(structureName, true, entries);
            }

            // Create NestedStructure for this ResourceLocation
            NestedToggleConfig nestedStructure = new NestedToggleConfig(entries);
            nestedStructures.put(structureConfig.toDefaultString(location), nestedStructure);
        }

        return nestedStructures;
    }

    public static Map<String, NestedToggleConfig> mapToNestedStructuresWithValues(StructureConfigToggle structureConfig) {
        Map<ResourceLocation, ToggleConfig> sets = structureConfig.getToggleConfigs();

        Map<String, NestedToggleConfig> nestedStructures = new HashMap<>();
        for (Map.Entry<ResourceLocation, ToggleConfig> mapEntry : sets.entrySet()) {
            ResourceLocation location = mapEntry.getKey();
            ToggleConfig stringList = mapEntry.getValue();

            // Prepare the map for the NestedStructure
            Map<String, NestedToggleConfig.Entry> entries = new HashMap<>();

            // Process each string in the list
            for (Map.Entry<String, Boolean> entry : stringList.setStructureInfo().entrySet()) {
                String structure = entry.getKey();
                boolean value = entry.getValue();
                putStructureName(structure, value, entries);
            }

            // Create NestedStructure for this ResourceLocation
            NestedToggleConfig nestedStructure = new NestedToggleConfig(entries);
            nestedStructures.put(structureConfig.toDefaultString(location), nestedStructure);
        }

        return nestedStructures;
    }

    public static void putStructureName(String structureName, boolean value, Map<String, NestedToggleConfig.Entry> entries) {
        if (structureName.contains("/")) {
            // Handle key-value pair scenario
            String[] parts = structureName.split("/", 2);
            String key = parts[0];
            String restOfStructureName = parts[1];

            boolean containsNestedStructure = entries.containsKey(key);
            Map<String, NestedToggleConfig.Entry> nestedEntries;
            if (containsNestedStructure) nestedEntries = entries.get(key).nested().entries();
            else nestedEntries = new HashMap<>();

            putStructureName(restOfStructureName, value, nestedEntries);
            if (!containsNestedStructure) entries.put(key, NestedToggleConfig.Entry.ofNested(new NestedToggleConfig(nestedEntries)));

        } else {
            // Handle simple key-value pair where value is always true
            entries.put(structureName, NestedToggleConfig.Entry.ofBoolean(value));
        }
    }

}
