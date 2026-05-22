package de.cristelknight.cristellib.config.client.extension.extensions;

import de.cristelknight.cristellib.*;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.client.extension.ConfigScreenExtension;
import de.cristelknight.cristellib.config.client.extension.ExtensionRegistry;
import de.cristelknight.cristellib.config.client.structure.ClientEDConfig;
import de.cristelknight.cristellib.config.client.structure.ClientPlacementConfig;
import de.cristelknight.cristellib.config.client.structure.ClientStructureConfig;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.structure.placement.PlacementConfig;
import de.cristelknight.cristellib.config.structure.toggle.NestedToggleConfig;
import de.cristelknight.cristellib.config.structure.toggle.ToggleConfigTransformer;
import de.cristelknight.cristellib.util.Util;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

import static de.cristelknight.cristellib.config.client.ScreenBuilder.tooltip;

public class StructureConfigExtension extends ConfigScreenExtension {

    private final Set<ClientStructureConfig> clientStructureConfigs = new HashSet<>();

    public StructureConfigExtension(String modId) {
        super(modId);
    }

    public static final ExtensionRegistry.LoadPredicate SHOULD_LOAD = modId -> {
        if (!CristelLibRegistry.getConfigMap().containsKey(modId))
            return false;

        ACConfig acConfig = ConfigRegistry.get(ACConfig.class);
        boolean structureEnabled = !acConfig.disableAutoConfig() && !acConfig.disableAutoConfigScreens();
        return structureEnabled && !acConfig.clientExcludedMods().contains(modId);
    };

    @Override
    public void addToBuilder(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        for (StructureConfig structureConfig : sortStructureSets(CristelLibRegistry.getConfigMap().get(modId))) {
            if (structureConfig.getType().equals(ConfigType.PLACEMENT))
                addPlacementCategory(builder, entryBuilder, (StructureConfigPlacement) structureConfig);
            else
                addEDCategory(builder, entryBuilder, (StructureConfigToggle) structureConfig);
        }
    }

    private void addPlacementCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, StructureConfigPlacement structureConfig) {
        ConfigCategory placementCategory = builder.getOrCreateCategory(Component.translatable("cristellib.placementCategoryTitle"));
        addHeader(structureConfig, placementCategory, entryBuilder);

        Map<ResourceLocation, PlacementConfig> placementConfigs = structureConfig.getPlacementConfigs();
        Map<ResourceLocation, PlacementConfig> defaultPlacementConfigs = structureConfig.getDefaultStructurePlacements();

        Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs = new HashMap<>();
        for (ResourceLocation structureSetLocation : Util.sortedKeyList(placementConfigs)) {
            PlacementConfig currentConfig = placementConfigs.get(structureSetLocation);
            PlacementConfig defaultConfig = defaultPlacementConfigs.get(structureSetLocation);
            if (defaultConfig == null) {
                getWarn(structureConfig, structureSetLocation);
                continue;
            }
            String structureSetName = structureConfig.toDefaultString(structureSetLocation);
            SubCategoryBuilder subCategory = entryBuilder.startSubCategory(Component.literal(structureSetName));
            subCategory.setTooltip(tooltip(structureSetName, structureConfig.getComments()));

            ClientPlacementConfig clientPlacementConfig = new ClientPlacementConfig(
                    frequencyEntry(entryBuilder, currentConfig.frequency(), defaultConfig.frequency(), subCategory),
                    intEntry(entryBuilder, "salt", currentConfig.salt(), defaultConfig.salt(), subCategory),
                    intEntry(entryBuilder, "separation", currentConfig.separation(), defaultConfig.separation(), subCategory),
                    intEntry(entryBuilder, "spacing", currentConfig.spacing(), defaultConfig.spacing(), subCategory)
            );

            placementCategory.addEntry(subCategory.build());

            clientPlacementConfigs.put(structureSetLocation, clientPlacementConfig);
        }
        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, clientPlacementConfigs, null));
    }

    private void addEDCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, StructureConfigToggle structureConfig) {
        ConfigCategory edCategory = builder.getOrCreateCategory(Component.translatable("cristellib.toggleCategoryTitle"));
        addHeader(structureConfig, edCategory, entryBuilder);

        Map<String, NestedToggleConfig> nestedStructureMap = ToggleConfigTransformer.mapToNestedStructuresWithValues(structureConfig);
        Map<ResourceLocation, ClientEDConfig> clientEDConfigs = new HashMap<>();
        for (String structureSetName : Util.sortedKeyList(nestedStructureMap)) {
            Map<String, BooleanListEntry> structures = new HashMap<>();
            SubCategoryBuilder rootSubCategory = entryBuilder.startSubCategory(Component.literal(structureSetName));
            rootSubCategory.setTooltip(tooltip(structureSetName, structureConfig.getComments()));
            NestedToggleConfig nestedEDConfig = nestedStructureMap.get(structureSetName);

            if (!checkForSingle(entryBuilder, nestedEDConfig, structureSetName, structureConfig, structures, edCategory, "")) {
                addEDSubCategory(rootSubCategory, null, entryBuilder, nestedEDConfig, "", structures, structureConfig, structureSetName);
                edCategory.addEntry(rootSubCategory.build());
            }

            clientEDConfigs.put(structureConfig.toDefaultId(structureSetName), new ClientEDConfig(structures));
        }

        clientStructureConfigs.add(new ClientStructureConfig(structureConfig, null, clientEDConfigs));
    }

    private void addEDSubCategory(SubCategoryBuilder rootCategory, SubCategoryBuilder parent, ConfigEntryBuilder entryBuilder, NestedToggleConfig edConfig, String pathPrefix,
                                  Map<String, BooleanListEntry> structures, StructureConfigToggle structureConfig, String structureSetName) {
        Map<String, NestedToggleConfig.Entry> entries = edConfig.entries();
        for (String key : Util.sortedKeyList(entries)) {
            NestedToggleConfig.Entry value = entries.get(key);
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

                if (getToggleSubWarn(structureConfig, fullPath, structureSetName)) continue;

                if (parent == null) rootCategory.add(toggle);
                else parent.add(toggle);

            } else {
                // Nested structure → make subcategory
                SubCategoryBuilder subCategory = entryBuilder.startSubCategory(Component.literal(key));
                subCategory.setTooltip(tooltip(structureSetName + "." + fullPath, structureConfig.getComments()));

                // Recursively build its children
                addEDSubCategory(rootCategory, subCategory, entryBuilder, value.nested(), fullPath, structures, structureConfig, structureSetName);

                if (parent == null) rootCategory.add(subCategory.build());
                else parent.add(subCategory.build());
            }
        }
    }

    private boolean checkForSingle(
            ConfigEntryBuilder entryBuilder,
            NestedToggleConfig nestedEDConfig,
            String structureSetName,
            StructureConfigToggle structureConfig,
            Map<String, BooleanListEntry> structures,
            ConfigCategory edCategory,
            String pathPrefix
    ) {
        if (nestedEDConfig.entries().size() != 1) return false;
        var entries = nestedEDConfig.entries();
        NestedToggleConfig.Entry entry = Util.getFirst(entries.values());
        String key = Util.getFirst(entries.keySet());
        assert entry != null;
        assert key != null;
        String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "/" + key;

        if (!entry.isBoolean()) {
            return checkForSingle(entryBuilder, entry.nested(), structureSetName, structureConfig, structures, edCategory, fullPath);
        }

        Component[] component = tooltip(structureSetName + "." + fullPath, structureConfig.getComments()).orElse(new MutableComponent[]{Component.literal(structureSetName)});
        BooleanListEntry toggle = entryBuilder.startBooleanToggle(
                        Component.literal(fullPath),
                        entry.value()
                ).setDefaultValue(true)
                .setTooltip(component)
                .build();

        structures.put(fullPath, toggle);

        if (getToggleSubWarn(structureConfig, fullPath, structureSetName)) return true;

        edCategory.addEntry(toggle);
        return true;
    }

    // Saving
    @Override
    public void onSave() {
        List<StructureConfig> configs = new ArrayList<>();
        for (ClientStructureConfig clientStructureConfig : clientStructureConfigs) {
            StructureConfig structureConfig = clientStructureConfig.structureConfig();
            if (structureConfig.getType().equals(ConfigType.PLACEMENT))
                updatePlacements((StructureConfigPlacement) structureConfig, clientStructureConfig.clientPlacementConfigs());
            else
                updateToggles((StructureConfigToggle) structureConfig, clientStructureConfig.clientEDConfigs());

            structureConfig.writeConfig(true);
            configs.add(structureConfig);
        }
        StructureConfig.addSetsToRuntimePack(configs);
    }

    private void updatePlacements(StructureConfigPlacement structureConfig, Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs) {
        for (Map.Entry<ResourceLocation, ClientPlacementConfig> entry : clientPlacementConfigs.entrySet()) {
            structureConfig.updatePlacement(entry.getKey(), entry.getValue().toPlacement());
        }
    }

    private void updateToggles(StructureConfigToggle structureConfig, Map<ResourceLocation, ClientEDConfig> clientEDConfigs) {
        for (Map.Entry<ResourceLocation, ClientEDConfig> entry : clientEDConfigs.entrySet()) {
            structureConfig.updateEDConfig(entry.getKey(), entry.getValue().toED());
        }
    }

    // Entry helpers
    private IntegerListEntry intEntry(ConfigEntryBuilder configEntry, String name, int value, int defaultValue, SubCategoryBuilder subCategory) {
        IntegerListEntry intEntry = configEntry.startIntField(Component.literal(name), value).setDefaultValue(defaultValue).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    private DoubleListEntry frequencyEntry(ConfigEntryBuilder configEntry, double value, double defaultValue, SubCategoryBuilder subCategory) {
        DoubleListEntry intEntry = configEntry.startDoubleField(Component.literal("frequency"), value).setDefaultValue(defaultValue).setMin(0.0).setMax(1.0).build();
        subCategory.add(intEntry);
        return intEntry;
    }

    // Warn helpers
    private static void getWarn(StructureConfig structureConfig, ResourceLocation structureSetName) {
        Constants.LOG.warn("Structure Set: {} has no default config, skipping!\nThis probably indicates that this config file is outdated and should be deleted to re-create it. (Path: {})", structureSetName.toString(), structureConfig.getPath());
    }

    private boolean getToggleSubWarn(StructureConfigToggle structureConfig, String fullPath, String structureSetName) {
        ResourceLocation setLocation = structureConfig.toDefaultId(structureSetName);
        List<ResourceLocation> structures = structureConfig.getDefaultStructureToggles().get(setLocation);
        if (structures == null) {
            getWarn(structureConfig, setLocation);
            return true;
        }
        if (!structures.contains(structureConfig.toDefaultId(fullPath))) {
            Constants.LOG.warn("Structure: {} has no default config, skipping!\nThis probably indicates that this config file is outdated and should be deleted to re-create it. (Path: {})", fullPath, structureConfig.getPath());
            return true;
        }
        return false;
    }

    // Header helpers
    private void addHeader(StructureConfig structureConfig, ConfigCategory configCategory, ConfigEntryBuilder entryBuilder) {
        if (structureConfig.isAutoGenerated() && !ACInfoData.currentData.containsKey(modId))
            configCategory.addEntry(entryBuilder.startTextDescription(
                    Component.translatable("cristellib.autoCategoryInfo", Constants.MOD_COMPONENT).withStyle(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Cristelknight999/Cristel-Lib/wiki/5.-Controlling-Structure-Auto-Config-(for-Mod-Authors)")))
            ).build());
        else if (structureConfig.getHeader() != null && !structureConfig.getHeader().isEmpty())
            configCategory.addEntry(entryBuilder.startTextDescription(Component.literal(getHeader(structureConfig.getHeader()))).build());
    }

    // idk anymore :( what is this
    private String getHeader(String header) {
        String separator = "=====";
        if (header.contains(separator)) {
            int start = header.lastIndexOf(separator);
            header = header.substring(0, start);
            List<String> list = new ArrayList<>(header.lines().toList());
            if (list.size() >= 2 && list.get(list.size() - 2).isBlank()) {
                list.removeLast();
                StringBuilder builder = new StringBuilder();
                list.forEach(line -> builder.append(line).append("\n"));
                header = builder.toString();
            }
        }
        return header.replace("\t", "    ").trim();
    }

    private List<StructureConfig> sortStructureSets(Set<StructureConfig> structureConfigs) {
        List<StructureConfig> sortedList = new ArrayList<>();
        structureConfigs.forEach(structureConfig -> {
            if (structureConfig.getType().equals(ConfigType.TOGGLE)) {
                sortedList.addFirst(structureConfig);
            } else sortedList.addLast(structureConfig);
        });
        return sortedList;
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }
}
