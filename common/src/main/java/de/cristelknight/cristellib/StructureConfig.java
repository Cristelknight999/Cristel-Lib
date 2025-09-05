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
                    Codec.list(StructureSetData.CODEC).fieldOf("structure_sets").forGetter(config -> config.structureSetHolders)
            ).apply(builder, StructureConfig::new)
    );

    private final Path path;

    private String header = "";

    private Map<String, String> comments;

    private final ConfigType type;

    private String defaultNamespace = "";

    private final List<StructureSetData> structureSetHolders;

    // default values
    private final Supplier<Map<ResourceLocation, List<ResourceLocation>>> structuresForED;
    private final Supplier<Map<ResourceLocation, PlacementConfig>> structurePlacement;

    // current values (structure_set + Config)
    public Map<ResourceLocation, EDConfig> enableDisableConfig = null;
    public Map<ResourceLocation, PlacementConfig> placementConfig = null;


    private StructureConfig(Path path, ConfigType type) {
        this(path, null, new HashMap<>(), type, new ArrayList<>());
    }

    private StructureConfig(String name, String path, String header, ConfigType type, Map<String, String> comments, List<StructureSetData> structureSetHolders) { // for CODEC
        this(Util.janksonPathFromString(path, name), header, ImmutableMap.copyOf(comments), type,  ImmutableList.copyOf(structureSetHolders));
    }

    private StructureConfig(Path path, String header, Map<String, String> comments, ConfigType type, List<StructureSetData> structureSetHolders) {
        this.path = path;
        if (header != null && !header.isEmpty()) setHeader(header);
        this.comments = comments;
        this.type = type;
        this.structureSetHolders = structureSetHolders;

        if(!this.structureSetHolders.isEmpty()) getDefaultNamespace();

        this.structuresForED = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSetHolders));
        this.structurePlacement = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddPlacements(structureSetHolders));
    }

    void addSet(StructureSetData set) {
        structureSetHolders.add(set);
    }

    public void addSetsToRuntimePack() {
        if (type.equals(ConfigType.ENABLE_DISABLE) && enableDisableConfig == null) enableDisableConfig = ConfigManager.readEDConfig(this);
        else if(type.equals(ConfigType.PLACEMENT) && placementConfig == null) placementConfig = ConfigManager.readPlacementConfig(this);

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
        if(!enableDisableConfig.containsKey(setLocation)) {
            CristelLib.LOGGER.error("{} {}", setLocation, enableDisableConfig.toString());
            return;
        }

        EDConfig setConfig = enableDisableConfig.get(setLocation);

        JsonArray array = structureSet.get("structures").getAsJsonArray();
        Iterator<JsonElement> structureIterator = array.iterator();
        while (structureIterator.hasNext()) {
            JsonElement structure = structureIterator.next();
            String structureName = structure.getAsJsonObject().get("structure").getAsString().split(":")[1];
            if (/*setConfig.containsStructure(structureName) && */setConfig.isStructureDisabled(structureName))
                structureIterator.remove();
        }
    }

    private void updatePlacementsInSet(JsonObject structureSet, ResourceLocation setLocation) {
        if (!placementConfig.containsKey(setLocation)) {
            CristelLib.LOGGER.error("{} {}", setLocation, placementConfig.toString());
            return;
        }

        JsonObject a = structureSet.get("placement").getAsJsonObject();
        PlacementConfig p = placementConfig.get(setLocation);
        a.addProperty("salt", p.salt());
        a.addProperty("spacing", p.spacing());
        a.addProperty("separation", p.separation());

        double f = p.frequency();

        if (f != 0 &&
                ((a.has("frequency") && a.get("frequency").getAsFloat() != f) || !a.has("frequency")))
            a.addProperty("frequency", f);
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
    public static StructureConfig create(Path path, String name, ConfigType type) {
        return new StructureConfig(path.resolve(name + ".json5"), type);
    }

    public static StructureConfig createWithDefaultConfigPath(String subPath, String name, ConfigType type) {
        return new StructureConfig(CristelLibExpectPlatform.getConfigDirectory().resolve(subPath).resolve(name + ".json5"), type);
    }

    public static StructureConfig createWithDefaultConfigPath(String name, ConfigType type) {
        return new StructureConfig(CristelLibExpectPlatform.getConfigDirectory().resolve(name + ".json5"), type);
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

    public Map<ResourceLocation, List<ResourceLocation>> getDefaultStructures() {
        return structuresForED.get();
    }

    public Map<ResourceLocation, PlacementConfig> getDefaultStructurePlacement() {
        return structurePlacement.get();
    }

    public Path getPath() {
        return path;
    }

    public ConfigType getType() {
        return type;
    }

    public ResourceLocation toDefaultRL(String location) {
        if(location.contains(":")) return ResourceLocation.parse(location);
        else if(defaultNamespace.equals("minecraft")) return ResourceLocation.withDefaultNamespace(location);
        else return ResourceLocation.fromNamespaceAndPath(defaultNamespace, location);
    }

    public String toDefaultString(ResourceLocation location) {
        return location.getNamespace().equals(defaultNamespace)
                ? location.getPath()
                : location.toString();
    }

    public void getDefaultNamespace() {
        Map<String, Integer> namespaceCounts = new HashMap<>();

        for (StructureSetData data : structureSetHolders) {
            for (ResourceLocation set : data.sets()) {
                namespaceCounts.merge(set.getNamespace(), 1, Integer::sum);
            }
        }

        if (namespaceCounts.isEmpty()) {
            throw new RuntimeException("No namespaces found in config with path: " + getPath() + " :(((");
        }

        this.defaultNamespace = namespaceCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("Could not determine default namespace :((("));
    }

    public boolean isSetsEmpty() {
        return structureSetHolders.isEmpty();
    }

    public boolean isAutoGenerated = false;


}
