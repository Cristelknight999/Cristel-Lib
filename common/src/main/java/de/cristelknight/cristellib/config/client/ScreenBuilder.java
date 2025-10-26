package de.cristelknight.cristellib.config.client;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.serialize.ed.EDConfig;
import de.cristelknight.cristellib.config.serialize.ed.EDConfigTransformer;
import de.cristelknight.cristellib.config.serialize.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
import de.cristelknight.cristellib.config.client.simple.SimpleConfigScreen;
import de.cristelknight.cristellib.config.client.simple.SimpleScreenBuilder;
import de.cristelknight.cristellib.util.Util;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

import static de.cristelknight.cristellib.config.client.simple.SimpleScreenBuilder.tooltip;

@Environment(EnvType.CLIENT)
public class ScreenBuilder {

    private final String modID;

    private ConfigEntryBuilder entryBuilder;

    private final Set<ClientStructureConfig> clientStructureConfigs = new HashSet<>();

    public ScreenBuilder(String modID) {
        this.modID = modID;
    }

    /**
     * Creates and builds a configuration screen for the specified mod.
     *
     * <p>This method initializes a {@link ConfigBuilder}, sets up its properties,
     * and populates it with config entries for either structure configs,
     * simple configs, or both depending on the provided flags.</p>
     *
     * @param parent     the parent screen to return to when the config screen is closed
     * @param structure  if {@code true}, includes structure-related configuration categories
     * @param simple     if {@code true}, includes simple (non-structure) configuration categories
     * @return the fully built {@link Screen} instance representing the mod's configuration screen
     */
    public Screen create(Screen parent, boolean structure, boolean simple){
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable("§7" + CristelLibExpectPlatform.getModDisplayName(modID) + " Configuration (via %s§7)", Util.CRISTEL_LIB));

        builder.setParentScreen(parent);
        builder.setSavingRunnable(this::onConfigSave);

        addToBuilder(builder, structure, simple);

        return builder.build();
    }

    /**
     * Adds configuration entries to the provided {@link ConfigBuilder}.
     *
     * <p>This method determines which types of configs to include based on the
     * given flags, and adds the corresponding categories and entries to the builder.</p>
     *
     * @param builder    the {@link ConfigBuilder} to add config entries to
     * @param structure  if {@code true}, adds structure-related configuration categories
     * @param simple     if {@code true}, adds simple (non-structure) configuration categories
     */
    public void addToBuilder(ConfigBuilder builder, boolean structure, boolean simple) {
        entryBuilder = builder.entryBuilder();
        if(structure) {
            for(StructureConfig structureConfig : sorted(CristelLibRegistry.getConfigs().get(modID))){
                if(structureConfig.getType().equals(ConfigType.PLACEMENT)) addPlacementCategory(builder, structureConfig);
                else addEDCategory(builder, structureConfig);
            }
        }
        if(simple) {
            for(SimpleConfigScreen simpleConfigScreen : ClientConfigRegistry.getScreens(modID)) {
                SimpleScreenBuilder.addConfigToCategory(builder, entryBuilder, simpleConfigScreen);
            }
        }
    }

    private void addPlacementCategory(ConfigBuilder builder, StructureConfig structureConfig) {
        ConfigCategory placementCategory = builder.getOrCreateCategory(Component.translatable("cristellib.placementCategoryTitle"));
        addHeader(structureConfig, placementCategory, entryBuilder);

        Map<ResourceLocation, PlacementConfig> placementConfigs = structureConfig.placementConfig;
        Map<ResourceLocation, PlacementConfig> defaultPlacementConfigs = structureConfig.getDefaultStructurePlacement();

        Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs = new HashMap<>();
        for (ResourceLocation structureSetLocation : Util.sortedKeyList(placementConfigs)) {
            PlacementConfig currentConfig = placementConfigs.get(structureSetLocation);
            PlacementConfig defaultConfig = defaultPlacementConfigs.get(structureSetLocation);
            if(defaultConfig == null) {
                getWarn(structureConfig, structureSetLocation);
                continue;
            }
            String structureSetName = structureConfig.toDefaultString(structureSetLocation);
            SubCategoryBuilder subCategory = entryBuilder.startSubCategory(Component.literal(structureSetName));
            subCategory.setTooltip(tooltip(structureSetName, structureConfig.getComments()));

            ClientPlacementConfig clientPlacementConfig = new ClientPlacementConfig(
                    frequencyEntry(entryBuilder, "frequency", currentConfig.frequency(), defaultConfig.frequency(), subCategory),
                    intEntry(entryBuilder, "salt", currentConfig.salt(), defaultConfig.salt(), subCategory),
                    intEntry(entryBuilder, "separation", currentConfig.separation(), defaultConfig.separation(), subCategory),
                    intEntry(entryBuilder, "spacing", currentConfig.spacing(), defaultConfig.spacing(), subCategory)
            );

            placementCategory.addEntry(subCategory.build());

            clientPlacementConfigs.put(structureSetLocation, clientPlacementConfig);
        }
        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, clientPlacementConfigs, null));
    }

    private void addEDCategory(ConfigBuilder builder, StructureConfig structureConfig) {
        ConfigCategory edCategory = builder.getOrCreateCategory(Component.translatable("cristellib.toggleCategoryTitle"));
        addHeader(structureConfig, edCategory, entryBuilder);

        Map<String, NestedEDConfig> nestedStructureMap = EDConfigTransformer.mapToNestedStructuresWithValues(structureConfig.enableDisableConfig, structureConfig);
        Map<ResourceLocation, ClientEDConfig> clientEDConfigs = new HashMap<>();
        for (String structureSetName : Util.sortedKeyList(nestedStructureMap)) {
            Map<String, BooleanListEntry> structures = new HashMap<>();
            SubCategoryBuilder rootSubCategory = entryBuilder.startSubCategory(Component.literal(structureSetName));
            rootSubCategory.setTooltip(tooltip(structureSetName, structureConfig.getComments()));
            NestedEDConfig nestedEDConfig = nestedStructureMap.get(structureSetName);

            if(!checkForSingle(nestedEDConfig, structureSetName, structureConfig, structures, edCategory)) {
                addEDSubCategory(rootSubCategory, null, nestedEDConfig, "", structures, structureConfig, structureSetName);
                edCategory.addEntry(rootSubCategory.build());
            }

            clientEDConfigs.put(structureConfig.toDefaultRL(structureSetName), new ClientEDConfig(structures));
        }

        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, null, clientEDConfigs));
    }

    private void addEDSubCategory(SubCategoryBuilder rootCategory, SubCategoryBuilder parent, NestedEDConfig edConfig, String pathPrefix,
                                  Map<String, BooleanListEntry> structures, StructureConfig structureConfig, String structureSetName) {
        Map<String, NestedEDConfig.Entry> entries = edConfig.entries();
        for (String key : Util.sortedKeyList(entries)) {
            NestedEDConfig.Entry value = entries.get(key);
            String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "/" + key;

            if (value.isBoolean()) {
                // Simple toggle
                BooleanListEntry toggle = entryBuilder.startBooleanToggle(
                                Component.literal(key),
                                value.value()
                        ).setDefaultValue(true)
                        .setTooltipSupplier(() -> tooltip(structureSetName + "." + fullPath, structureConfig.getComments()))
                        .build();

                structures.put(fullPath, toggle);

                if(getEDSubWarn(structureConfig, fullPath, structureSetName)) continue;

                if(parent == null) rootCategory.add(toggle);
                else parent.add(toggle);

            } else {
                // Nested structure → make subcategory
                SubCategoryBuilder subCategory = entryBuilder.startSubCategory(Component.literal(key));
                subCategory.setTooltip(tooltip(structureSetName + "." + fullPath, structureConfig.getComments()));

                // Recursively build its children
                addEDSubCategory(rootCategory, subCategory, value.nested(), fullPath, structures, structureConfig, structureSetName);

                if(parent == null) rootCategory.add(subCategory.build());
                else parent.add(subCategory.build());
            }
        }
    }

    private boolean checkForSingle(NestedEDConfig nestedEDConfig, String structureSetName, StructureConfig structureConfig, Map<String, BooleanListEntry> structures, ConfigCategory edCategory) {
        if(nestedEDConfig.entries().size() != 1) return false;
        NestedEDConfig.Entry entry = nestedEDConfig.entries().values().stream().findAny().get();
        if(!entry.isBoolean()) return false;

        String key = nestedEDConfig.entries().keySet().stream().findAny().get();

        BooleanListEntry toggle = entryBuilder.startBooleanToggle(
                        Component.literal(key),
                        entry.value()
                ).setDefaultValue(true)
                .setTooltipSupplier(() -> tooltip(structureSetName + "." + key, structureConfig.getComments()))
                .build();

        structures.put(key, toggle);

        if(getEDSubWarn(structureConfig, key, structureSetName)) return true;

        edCategory.addEntry(toggle);
        return true;
    }

    // Saving
    private void onConfigSave(){
        for(ClientStructureConfig clientStructureConfig : clientStructureConfigs) {
            StructureConfig structureConfig = clientStructureConfig.structureConfig();
            if(structureConfig.getType().equals(ConfigType.PLACEMENT)) updatePlacements(clientStructureConfig.structureConfig(), clientStructureConfig.clientPlacementConfigs());
            else updateEDs(clientStructureConfig.structureConfig(), clientStructureConfig.clientEDConfigs());

            structureConfig.writeConfig(true);
            structureConfig.addSetsToRuntimePack();
        }

        SimpleScreenBuilder.saveConfigs(modID);
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

    // Entry helpers
    private IntegerListEntry intEntry(ConfigEntryBuilder configEntry, String name, int value, int defaultValue, SubCategoryBuilder subCategory){
        IntegerListEntry intEntry = configEntry.startIntField(Component.literal(name), value).setDefaultValue(defaultValue).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    private DoubleListEntry frequencyEntry(ConfigEntryBuilder configEntry, String name, double value, double defaultValue, SubCategoryBuilder subCategory){
        DoubleListEntry intEntry = configEntry.startDoubleField(Component.literal(name), value).setDefaultValue(defaultValue).setMin(0).setMax(1.0).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    // Warn helpers
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

    // Header helpers
    public void addHeader(StructureConfig structureConfig, ConfigCategory configCategory, ConfigEntryBuilder entryBuilder) {
        if(structureConfig.isAutoGenerated && !ACInfoData.currentData.containsKey(modID)) configCategory.addEntry(entryBuilder.startTextDescription(
                Component.translatable("cristellib.autoCategoryInfo", Util.CRISTEL_LIB).withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Cristelknight999/Cristel-Lib/wiki/5.-Controlling-Structure-Auto-Config-(for-Mod-Authors)")))
        ).build());
        else if(structureConfig.getHeader() != null && !structureConfig.getHeader().isEmpty())
            configCategory.addEntry(entryBuilder.startTextDescription(Component.literal(getHeader(structureConfig.getHeader()))).build());
    }

    // idk anymore :( what is this
    public String getHeader(String header) {
        String separator = "=====";
        if(header.contains(separator)) {
            int start = header.lastIndexOf(separator);
            header = header.substring(0, start);
            List<String> list = new ArrayList<>(header.lines().toList()) ;
            if(list.size() >= 2 && list.get(list.size() - 2).isBlank()){
                list.removeLast();
                StringBuilder builder = new StringBuilder();
                list.forEach(line -> builder.append(line).append("\n"));
                header = builder.toString();
            }
        }
        return header.replace("\t", "    ").trim();
    }

    public List<StructureConfig> sorted(Set<StructureConfig> structureConfigs) {
        List<StructureConfig> sortedList = new ArrayList<>();
        structureConfigs.forEach(structureConfig -> {
            if(structureConfig.getType().equals(ConfigType.ENABLE_DISABLE)) {
                sortedList.addFirst(structureConfig);
            } else sortedList.addLast(structureConfig);
        });
        return sortedList;
    }

    // Get correct screens helpers
    public static Pair<Boolean, Boolean> shouldCreateScreen(String modID, boolean mainStructure) {
        if(modID.equals(CristelLib.MOD_ID) || modID.equals("minecraft")) return new Pair<>(false, false);

        boolean structure = mainStructure &&
                CristelLibRegistry.getConfigs().containsKey(modID) &&
                !ConfigRegistry.get(ACConfig.class).clientExcludedMods().contains(modID);

        return new Pair<>(structure, ClientConfigRegistry.hasScreens(modID));
    }

    public static Set<String> allConfigMods(boolean structure) {
        Set<String> allMods = new HashSet<>(ClientConfigRegistry.getAllConfigsWithScreen().keySet());
        if(structure) allMods.addAll(new HashSet<>(CristelLibRegistry.getConfigs().keySet()));
        return allMods;
    }
}
