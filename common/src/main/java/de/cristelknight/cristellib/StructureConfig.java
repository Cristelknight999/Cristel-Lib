package de.cristelknight.cristellib;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.serialize.ed.EDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import de.cristelknight.cristellib.registry.ReadStructureSets;
import de.cristelknight.cristellib.util.JanksonUtil;
import de.cristelknight.cristellib.util.RuntimePackUtil;
import de.cristelknight.cristellib.util.Util;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.*;

public class StructureConfig {

    public static final Codec<StructureConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.STRING.fieldOf("name").forGetter(config -> Util.fileName(config.getPath())),
                    Codec.STRING.fieldOf("path").forGetter(config -> String.valueOf(config.path.getParent())),
                    Codec.STRING.optionalFieldOf("header", "").forGetter(config -> config.header),
                    ConfigType.CODEC.fieldOf("config_type").forGetter(config -> config.type),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("comments", new HashMap<>()).forGetter(config -> config.comments),
                    Codec.STRING.fieldOf("default_namespace").forGetter(config -> config.defaultNamespace),
                    Codec.list(StructureSetData.CODEC).fieldOf("structure_sets").forGetter(config -> config.structureSetHolders)
            ).apply(builder, StructureConfig::new)
    );

    private final Path path;

    private String header = "";

    private Map<String, String> comments;

    private final ConfigType type;

    private final String defaultNamespace;

    private final List<StructureSetData> structureSetHolders;

    // default values
    private final Supplier<Map<ResourceLocation, List<String>>> structuresForED;
    private final Supplier<Map<String, PlacementConfig>> structurePlacement;

    // current values (structure_set + Config)
    public Map<String, EDConfig> enableDisableConfig = null;
    public Map<String, PlacementConfig> placementConfig = null;


    private StructureConfig(Path path, ConfigType type, String defaultNamespace) {
        this(path, null, new HashMap<>(), type, defaultNamespace, new ArrayList<>());
    }

    private StructureConfig(String name, String path, String header, ConfigType type, Map<String, String> comments, String defaultNamespace, List<StructureSetData> structureSetHolders) { // for CODEC
        this(Util.janksonPathFromString(path, name), header, ImmutableMap.copyOf(comments), type, defaultNamespace, ImmutableList.copyOf(structureSetHolders));
    }

    private StructureConfig(Path path, String header, Map<String, String> comments, ConfigType type, String defaultNamespace, List<StructureSetData> structureSetHolders) {
        this.path = path;
        if (header != null && !header.isEmpty()) setHeader(header);
        this.comments = comments;
        this.type = type;
        this.defaultNamespace = defaultNamespace;
        this.structureSetHolders = structureSetHolders;

        this.structuresForED = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSetHolders));
        this.structurePlacement = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddPlacements(structureSetHolders));
    }

    void addSet(StructureSetData set) {
        structureSetHolders.add(set);
    }

    public void addSetsToRuntimePack() {
        if (type.equals(ConfigType.ENABLE_DISABLE)) enableDisableConfig = ConfigManager.readEDConfig(this);
        else placementConfig = new HashMap<>(ConfigManager.readPlacementConfig(this));

        structureSetHolders.forEach(holder -> holder.sets().forEach(setLocation -> {
            String modID = holder.modID();

            JsonElement structureSetElement = getStructureSet(setLocation, modID);
            if (!(structureSetElement instanceof JsonObject structureSet)) {
                CristelLib.LOGGER.warn("Set for {} {} is not a JsonObject, skipping...", modID, setLocation);
                return;
            }

            JsonObject originalSet = structureSet.deepCopy();

            if (type.equals(ConfigType.ENABLE_DISABLE)) {
                removeStructureInSets(structureSet, setLocation);
            } else if (type.equals(ConfigType.PLACEMENT)) {
                updatePlacementsInSet(structureSet, setLocation);
            }

            if (!structureSet.equals(originalSet)) {
                CristelLib.RUNTIME_PACK.addStructureSet(setLocation, structureSet);
            }

        }));

    }

    private void removeStructureInSets(JsonObject structureSet, ResourceLocation setLocation) {
        JsonArray array = structureSet.get("structures").getAsJsonArray();
        Iterator<JsonElement> structureIterator = array.iterator();
        while (structureIterator.hasNext()) {
            JsonElement structure = structureIterator.next();
            String structureName = structure.getAsJsonObject().get("structure").getAsString().split(":")[1];
            if (!enableDisableConfig.get(structureName) && enableDisableConfig.containsKey(structureName))
                structureIterator.remove();
        }
    }

    private void updatePlacementsInSet(JsonObject structureSet, ResourceLocation setLocation) {
        String structureSetName = setLocation.getPath();
        if (placementConfig.containsKey(structureSetName)) {
            JsonObject a = structureSet.get("placement").getAsJsonObject();
            PlacementConfig p = placementConfig.get(structureSetName);
            a.addProperty("salt", p.salt());
            a.addProperty("spacing", p.spacing());
            a.addProperty("separation", p.separation());

            double f = p.frequency();

            if (f != 0 && a.has("frequency") && (a.get("frequency").getAsFloat() != f)) a.addProperty("frequency", f);
        }
    }

    private JsonElement getStructureSet(ResourceLocation location, String modID) {
        ResourceLocation structureLocation = RuntimePackUtil.getLocationForStructureSet(location);
        if (CristelLib.RUNTIME_PACK.hasResource(structureLocation)) {
            return CristelLib.RUNTIME_PACK.getResource(structureLocation);
        }
        return JanksonUtil.getSetElement(modID, location);
    }


    /**
     * Writes the configuration to the filesystem.
     * This method initializes the structures or placements depending on the {@link ConfigType}.
     */
    public void writeConfig(boolean override) {
        if (type.equals(ConfigType.ENABLE_DISABLE)) {
            ConfigManager.createEDConfig(this, override);
        } else if (type.equals(ConfigType.PLACEMENT)) {
            ConfigManager.createPlacementConfig(this, override);
        }
    }


    // API
    public static StructureConfig create(Path path, String name, ConfigType type, String defaultNamespace) {
        return new StructureConfig(path.resolve(name + ".json5"), type, defaultNamespace);
    }

    public static StructureConfig createWithDefaultConfigPath(String subPath, String name, ConfigType type, String defaultNamespace) {
        return new StructureConfig(CristelLibExpectPlatform.getConfigDirectory().resolve(subPath).resolve(name + ".json5"), type, defaultNamespace);
    }

    public static StructureConfig createWithDefaultConfigPath(String name, ConfigType type, String defaultNamespace) {
        return new StructureConfig(CristelLibExpectPlatform.getConfigDirectory().resolve(name + ".json5"), type, defaultNamespace);
    }

    public void setComments(Map<String, String> comments) {
        this.comments = comments;
    }

    public void setHeader(String header) {
        this.header = ConfigManager.createHeader(header);
    }

    public String getHeader() {
        return header;
    }

    public Map<String, String> getComments() {
        return comments;
    }

    public Map<ResourceLocation, List<String>> getDefaultStructures() {
        return structuresForED.get();
    }

    public Map<String, PlacementConfig> getDefaultStructurePlacement() {
        return structurePlacement.get();
    }

    public Path getPath() {
        return path;
    }

    public ConfigType getType() {
        return type;
    }
}
