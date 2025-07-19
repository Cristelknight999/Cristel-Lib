package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.serialize.ed.EDConfigTransformer;
import de.cristelknight.cristellib.config.serialize.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ScreenBuilder {

    private final Set<ClientStructureConfig> clientStructureConfigs = new HashSet<>();

    public Screen createP(Screen parent, String modID){
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Component.literal("Cristel Lib config for: " + modID));
        builder.setParentScreen(parent);
        builder.setSavingRunnable(this::onConfigSave);


        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        for(StructureConfig structureConfig : CristelLibRegistry.getConfigs().get(modID)){
            if(structureConfig.getType().equals(ConfigType.PLACEMENT)) addPlacementCategory(builder, structureConfig, entryBuilder);
            else addEDCategory(builder, structureConfig, entryBuilder);
        }

        return builder.build();
    }

    private void addPlacementCategory(ConfigBuilder builder, StructureConfig structureConfig, ConfigEntryBuilder configEntry) {
        ConfigCategory placementCategory = builder.getOrCreateCategory(Component.literal("Placement Category"));
        Map<String, PlacementConfig> placementConfigs = structureConfig.placementConfig;
        Map<String, PlacementConfig> defaultPlacementConfigs = structureConfig.getDefaultStructurePlacement();

        Map<String, ClientPlacementConfig> clientPlacementConfigs = new HashMap<>();
        for (String structureName : placementConfigs.keySet()) {
            PlacementConfig currentConfig = placementConfigs.get(structureName);
            PlacementConfig defaultConfig = defaultPlacementConfigs.get(structureName);
            SubCategoryBuilder subCategory = configEntry.startSubCategory(Component.literal(structureName));

            ClientPlacementConfig clientPlacementConfig = new ClientPlacementConfig(
                    intEntry(configEntry, "spacing", currentConfig.spacing(), defaultConfig.spacing(), subCategory),
                    intEntry(configEntry, "separation", currentConfig.separation(), defaultConfig.separation(), subCategory),
                    doubleEntry(configEntry, "frequency", currentConfig.frequency(), defaultConfig.frequency(), subCategory),
                    intEntry(configEntry, "salt", currentConfig.salt(), defaultConfig.salt(), subCategory)
            );

            placementCategory.addEntry(subCategory.build());

            clientPlacementConfigs.put(structureName, clientPlacementConfig);
        }
        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, clientPlacementConfigs, null));
    }

    private void addEDCategory(ConfigBuilder builder, StructureConfig structureConfig, ConfigEntryBuilder configEntry) {
        ConfigCategory edCategory = builder.getOrCreateCategory(Component.literal("ED Category"));
        Map<ResourceLocation, List<String>> sets = structureConfig.getDefaultStructures();
        Map<String, NestedEDConfig> nestedStructureMap = EDConfigTransformer.mapToNestedStructures(sets);

        Map<String, BooleanListEntry> clientEDConfigs = new HashMap<>();

        Map<String, Boolean> placementConfigs = structureConfig.enableDisableConfig;
        CristelLib.LOGGER.warn(placementConfigs.toString());
        for (String structureSetName : nestedStructureMap.keySet()) {
            SubCategoryBuilder rootSubCategory = configEntry.startSubCategory(Component.literal(structureSetName));
            addEDSubCategory(rootSubCategory, null, nestedStructureMap.get(structureSetName), "", configEntry, clientEDConfigs);
            edCategory.addEntry(rootSubCategory.build());
        }

        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, null, clientEDConfigs));
    }


    private void addEDSubCategory(SubCategoryBuilder rootCategory, SubCategoryBuilder parent, NestedEDConfig edConfig, String pathPrefix, ConfigEntryBuilder configEntry, Map<String, BooleanListEntry> clientEDConfigs) {
        for (Map.Entry<String, NestedEDConfig.Entry> entry : edConfig.entries().entrySet()) {
            String key = entry.getKey();
            NestedEDConfig.Entry value = entry.getValue();
            String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "/" + key;

            if (value.isBoolean()) {
                // Simple toggle
                BooleanListEntry toggle = configEntry.startBooleanToggle(
                                Component.literal(key),
                                value.value()
                        ).setDefaultValue(true)
                        .build();

                clientEDConfigs.put(fullPath, toggle);
                CristelLib.LOGGER.error(fullPath);

                if(parent == null) rootCategory.add(toggle);
                else parent.add(toggle);

            } else {
                // Nested structure → make subcategory
                SubCategoryBuilder subCategory = configEntry.startSubCategory(Component.literal(key));

                // Recursively build its children
                addEDSubCategory(rootCategory, subCategory, value.nested(), fullPath, configEntry, clientEDConfigs);

                if(parent == null) rootCategory.add(subCategory.build());
                else parent.add(subCategory.build());
            }
        }


    }




    private void onConfigSave(){
        for(ClientStructureConfig clientStructureConfig : clientStructureConfigs) {
            StructureConfig structureConfig = clientStructureConfig.getStructureConfig();
            if(structureConfig.getType().equals(ConfigType.PLACEMENT)) updatePlacements(clientStructureConfig.getStructureConfig(), clientStructureConfig.getPlacementConfigs());
            else updateEDs(clientStructureConfig.getStructureConfig(), clientStructureConfig.getEDConfigs());

            structureConfig.writeConfig(true);
            structureConfig.addSetsToRuntimePack();
        }
    }
    private void updatePlacements(StructureConfig structureConfig, Map<String, ClientPlacementConfig> clientPlacementConfigs) {
        Map<String, PlacementConfig> placementConfigs = structureConfig.placementConfig;
        for(String structureName : clientPlacementConfigs.keySet()) {
            placementConfigs.put(structureName, clientPlacementConfigs.get(structureName).toPlacement());
        }
    }
    private void updateEDs(StructureConfig structureConfig, Map<String, BooleanListEntry> clientEDConfigs){
        Map<String, Boolean> placementConfigs = structureConfig.enableDisableConfig;
        for(String structureName : clientEDConfigs.keySet()) {
            placementConfigs.put(structureName, clientEDConfigs.get(structureName).getValue());
        }
    }




    private IntegerListEntry intEntry(ConfigEntryBuilder configEntry, String name, int value, int defaultValue, SubCategoryBuilder subCategory){
        IntegerListEntry intEntry = configEntry.startIntField(Component.literal(name), value).setDefaultValue(defaultValue).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    private BooleanListEntry booleanEntry(ConfigEntryBuilder configEntry, String name, boolean value){
        return configEntry.startBooleanToggle(Component.literal(name), value).setDefaultValue(true).build();
    }

    private DoubleListEntry doubleEntry(ConfigEntryBuilder configEntry, String name, double value, double defaultValue, SubCategoryBuilder subCategory){
        DoubleListEntry intEntry = configEntry.startDoubleField(Component.literal(name), value).setDefaultValue(defaultValue).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    /*
    private void addEDCategory(ConfigBuilder builder, StructureConfig structureConfig, ConfigEntryBuilder configEntry) {
        ConfigCategory edCategory = builder.getOrCreateCategory(Component.literal("ED Category"));
        Map<String, Boolean> placementConfigs = structureConfig.enableDisableConfig;

        Map<String, BooleanListEntry> clientEDConfigs = new HashMap<>();
        for (String structureName : placementConfigs.keySet()) {
            boolean currentConfig = placementConfigs.get(structureName);

            BooleanListEntry booleanEntry = booleanEntry(configEntry, structureName, currentConfig);

            edCategory.addEntry(booleanEntry);

            clientEDConfigs.put(structureName, booleanEntry);
        }
        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, null, clientEDConfigs));
    }
     */
}
