package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.serialize.ed.EDConfig;
import de.cristelknight.cristellib.config.serialize.ed.EDConfigTransformer;
import de.cristelknight.cristellib.config.serialize.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.util.Util;
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

    public Screen create(Screen parent, String modID){
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
        Map<ResourceLocation, PlacementConfig> placementConfigs = structureConfig.placementConfig;
        Map<ResourceLocation, PlacementConfig> defaultPlacementConfigs = structureConfig.getDefaultStructurePlacement();

        Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs = new HashMap<>();
        for (ResourceLocation structureSetName : Util.sortedKeyList(placementConfigs)) {
            PlacementConfig currentConfig = placementConfigs.get(structureSetName);
            PlacementConfig defaultConfig = defaultPlacementConfigs.get(structureSetName);
            if(defaultConfig == null) {
                getWarn(structureConfig, structureSetName);
                continue;
            }
            SubCategoryBuilder subCategory = configEntry.startSubCategory(Component.literal(structureConfig.toDefaultString(structureSetName)));

            ClientPlacementConfig clientPlacementConfig = new ClientPlacementConfig(
                    frequencyEntry(configEntry, "frequency", currentConfig.frequency(), defaultConfig.frequency(), subCategory),
                    intEntry(configEntry, "salt", currentConfig.salt(), defaultConfig.salt(), subCategory),
                    intEntry(configEntry, "separation", currentConfig.separation(), defaultConfig.separation(), subCategory),
                    intEntry(configEntry, "spacing", currentConfig.spacing(), defaultConfig.spacing(), subCategory)
            );

            placementCategory.addEntry(subCategory.build());

            clientPlacementConfigs.put(structureSetName, clientPlacementConfig);
        }
        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, clientPlacementConfigs, null));
    }

    private void addEDCategory(ConfigBuilder builder, StructureConfig structureConfig, ConfigEntryBuilder configEntry) {
        ConfigCategory edCategory = builder.getOrCreateCategory(Component.literal("ED Category"));
        Map<String, NestedEDConfig> nestedStructureMap = EDConfigTransformer.mapToNestedStructuresWithValues(structureConfig.enableDisableConfig, structureConfig);

        Map<ResourceLocation, ClientEDConfig> clientEDConfigs = new HashMap<>();
        for (String structureSetName : Util.sortedKeyList(nestedStructureMap)) {
            Map<String, BooleanListEntry> structures = new HashMap<>();
            SubCategoryBuilder rootSubCategory = configEntry.startSubCategory(Component.literal(structureSetName));
            addEDSubCategory(rootSubCategory, null, nestedStructureMap.get(structureSetName), "", configEntry, structures, structureConfig, structureSetName);
            edCategory.addEntry(rootSubCategory.build());
            clientEDConfigs.put(structureConfig.toDefaultRL(structureSetName), new ClientEDConfig(structures));
        }

        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, null, clientEDConfigs));
    }


    private void addEDSubCategory(SubCategoryBuilder rootCategory, SubCategoryBuilder parent, NestedEDConfig edConfig, String pathPrefix,
                                  ConfigEntryBuilder configEntry, Map<String, BooleanListEntry> clientEDConfigs, StructureConfig structureConfig, String structureSetName) {
        Map<String, NestedEDConfig.Entry> entries = edConfig.entries();
        for (String key : Util.sortedKeyList(entries)) {
            NestedEDConfig.Entry value = entries.get(key);
            String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "/" + key;

            if (value.isBoolean()) {
                // Simple toggle
                BooleanListEntry toggle = configEntry.startBooleanToggle(
                                Component.literal(key),
                                value.value()
                        ).setDefaultValue(true)
                        .build();

                clientEDConfigs.put(fullPath, toggle);

                if(getEDSubWarn(structureConfig, fullPath, structureSetName)) continue;

                if(parent == null) rootCategory.add(toggle);
                else parent.add(toggle);

            } else {
                // Nested structure → make subcategory
                SubCategoryBuilder subCategory = configEntry.startSubCategory(Component.literal(key));

                // Recursively build its children
                addEDSubCategory(rootCategory, subCategory, value.nested(), fullPath, configEntry, clientEDConfigs, structureConfig, structureSetName);

                if(parent == null) rootCategory.add(subCategory.build());
                else parent.add(subCategory.build());
            }
        }
    }


    private void onConfigSave(){
        for(ClientStructureConfig clientStructureConfig : clientStructureConfigs) {
            StructureConfig structureConfig = clientStructureConfig.structureConfig();
            if(structureConfig.getType().equals(ConfigType.PLACEMENT)) updatePlacements(clientStructureConfig.structureConfig(), clientStructureConfig.clientPlacementConfigs());
            else updateEDs(clientStructureConfig.structureConfig(), clientStructureConfig.clientEDConfigs());

            structureConfig.writeConfig(true);
            structureConfig.addSetsToRuntimePack();
        }
    }
    private void updatePlacements(StructureConfig structureConfig, Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs) {
        Map<ResourceLocation, PlacementConfig> placementConfigs = structureConfig.placementConfig;
        for(ResourceLocation structureName : clientPlacementConfigs.keySet()) {
            placementConfigs.put(structureName, clientPlacementConfigs.get(structureName).toPlacement());
        }
    }
    private void updateEDs(StructureConfig structureConfig, Map<ResourceLocation, ClientEDConfig> clientEDConfigs){
        Map<ResourceLocation, EDConfig> placementConfigs = structureConfig.enableDisableConfig;
        for(ResourceLocation structureName : clientEDConfigs.keySet()) {
            placementConfigs.put(structureName, clientEDConfigs.get(structureName).toED());
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

    private DoubleListEntry frequencyEntry(ConfigEntryBuilder configEntry, String name, double value, double defaultValue, SubCategoryBuilder subCategory){
        DoubleListEntry intEntry = configEntry.startDoubleField(Component.literal(name), value).setDefaultValue(defaultValue).setMin(0.000001).setMax(1.0).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    private static void getWarn(StructureConfig structureConfig, ResourceLocation structureSetName) {
        CristelLib.LOGGER.warn("Structure Set: {} has no default config, skipping!\nThis probably indicates that this config file is outdated and should be deleted to re-create it. (Path: {})", structureSetName.toString(), structureConfig.getPath());
    }


    private boolean getEDSubWarn(StructureConfig structureConfig, String fullPath, String structureSetName) {
        ResourceLocation setLocation = structureConfig.toDefaultRL(structureSetName);
        List<ResourceLocation> structures = structureConfig.getDefaultStructures().get(setLocation);
        if(structures == null) {
            getWarn(structureConfig, setLocation);
            return true;
        }
        if(!structures.contains(structureConfig.toDefaultRL(fullPath))) {
            CristelLib.LOGGER.warn("Structure: {} has no default config, skipping!\nThis probably indicates that this config file is outdated and should be deleted to re-create it. (Path: {})", fullPath, structureConfig.getPath());
            return true;
        }
        return false;
    }
}
