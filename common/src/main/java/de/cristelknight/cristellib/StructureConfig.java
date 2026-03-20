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
import de.cristelknight.cristellib.config.structure.ReadStructureSets;
import de.cristelknight.cristellib.config.structure.ed.EDConfig;
import de.cristelknight.cristellib.config.structure.placement.PlacementConfig;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import de.cristelknight.cristellib.util.FileHelper;
import de.cristelknight.cristellib.util.JsonHelper;
import de.cristelknight.cristellib.util.runtimepack.RuntimePackUtil;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.*;

public class StructureConfig {

    public static final Codec<StructureConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.STRING.fieldOf("name").forGetter(config -> FileHelper.fileName(config.getPath())),
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
    private final Supplier<Map<Identifier, List<Identifier>>> structuresForED;
    private final Supplier<Map<Identifier, PlacementConfig>> structurePlacement;

    // current values (structure_set + Config)
    public Map<Identifier, EDConfig> enableDisableConfig = null;
    public Map<Identifier, PlacementConfig> placementConfig = null;


    private StructureConfig(Path path, ConfigType type) {
        this(path, null, new HashMap<>(), type, new ArrayList<>());
    }

    private StructureConfig(String name, String path, String header, ConfigType type, Map<String, String> comments, List<StructureSetData> structureSetHolders) { // for CODEC
        this(FileHelper.janksonPathFromString(path, name), header, ImmutableMap.copyOf(comments), type, ImmutableList.copyOf(structureSetHolders));
    }

    private StructureConfig(Path path, String header, Map<String, String> comments, ConfigType type, List<StructureSetData> structureSetHolders) {
        this.path = path;
        if (header != null && !header.isEmpty()) setHeader(header);
        this.comments = comments;
        this.type = type;
        this.structureSetHolders = structureSetHolders;

        if (!this.structureSetHolders.isEmpty()) getDefaultNamespace();

        this.structuresForED = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSetHolders));
        this.structurePlacement = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddPlacements(structureSetHolders));
    }

    void addSet(StructureSetData set) {
        structureSetHolders.add(set);
    }

    public void addSetsToRuntimePack() {
        readConfig(false);
        checkForError();

        structureSetHolders.forEach(holder -> holder.sets().forEach(setLocation -> {
            String modId = holder.modId();

            JsonElement structureSetElement = getStructureSet(setLocation, modId);
            if (!(structureSetElement instanceof JsonObject structureSet)) {
                Constants.LOGGER.warn("Set for {} {} is not a JsonObject, skipping...", modId, setLocation);
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

    //TODO:
    private void checkForError() {
        Set<Identifier> setsToCheck = type.equals(ConfigType.ENABLE_DISABLE) ? enableDisableConfig.keySet() : placementConfig.keySet();

        boolean error = false;
        for (StructureSetData data : structureSetHolders) {
            List<Identifier> newlyReadSets = data.sets();
            boolean hasAll = setsToCheck.containsAll(newlyReadSets);
            if (!hasAll) {
                error = true;
                var newlyReadSetsCopy = new ArrayList<>(newlyReadSets);
                newlyReadSetsCopy.removeAll(setsToCheck);
                Constants.LOGGER.error("Structure sets are missing from config: {}", newlyReadSetsCopy);
                //break;
            }
        }
        if (!error) return;

        enableDisableConfig = null;
        placementConfig = null;
        String name = FileHelper.fileName(path);
        FileHelper.renameFile(path, name + "-had-error");
        writeConfig(true);
        readConfig(true);
    }

    private void removeStructureInSets(JsonObject structureSet, Identifier setLocation) {
        EDConfig setConfig = enableDisableConfig.get(setLocation);

        JsonArray array = structureSet.get("structures").getAsJsonArray();
        Iterator<JsonElement> structureIterator = array.iterator();
        while (structureIterator.hasNext()) {
            JsonElement structure = structureIterator.next();
            String structureName = toDefaultString(Objects.requireNonNull(Identifier.tryParse(structure.getAsJsonObject().get("structure").getAsString())));
            if (setConfig.containsStructure(structureName)) {
                if (setConfig.isStructureDisabled(structureName)) structureIterator.remove();

            } else
                Constants.LOGGER.error("{} is not included in: {} for mod with path: {}", structureName, setLocation, path);
        }
    }

    private void updatePlacementsInSet(JsonObject structureSet, Identifier setLocation) {
        PlacementConfig p = placementConfig.get(setLocation);
        JsonObject o = structureSet.get("placement").getAsJsonObject();
        o.addProperty("salt", p.salt());
        o.addProperty("spacing", p.spacing());
        o.addProperty("separation", p.separation());

        double newF = p.frequency();

        if ((!o.has("frequency") && newF == 1.0) ||
                (o.has("frequency") && newF == (double) o.get("frequency").getAsFloat())) return;

        o.addProperty("frequency", newF);
    }

    private JsonElement getStructureSet(Identifier location, String modId) {
        Identifier structureLocation = RuntimePackUtil.getLocationForStructureSet(location);
        if (CristelLib.RUNTIME_PACK.hasResource(structureLocation)) {
            return CristelLib.RUNTIME_PACK.getResource(structureLocation);
        }
        return JsonHelper.getSetElement(modId, location);
    }


    /**
     * Writes the configuration to the filesystem.
     * This method initializes the structures or placements depending on the {@link ConfigType}.
     */
    public void writeConfig(boolean override) {
        if (!override && getPath().toFile().exists()) return;

        if (type.equals(ConfigType.ENABLE_DISABLE)) {
            ConfigManager.createEDConfig(this, override);
        } else if (type.equals(ConfigType.PLACEMENT)) {
            ConfigManager.createPlacementConfig(this, override);
        }
    }

    public void readConfig(boolean override) {
        if (type.equals(ConfigType.ENABLE_DISABLE) && (enableDisableConfig == null || override))
            enableDisableConfig = ConfigManager.readEDConfig(this);
        else if (type.equals(ConfigType.PLACEMENT) && (placementConfig == null || override))
            placementConfig = ConfigManager.readPlacementConfig(this);
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
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public Map<String, String> getComments() {
        return comments;
    }

    public Map<Identifier, List<Identifier>> getDefaultStructures() {
        return structuresForED.get();
    }

    public Map<Identifier, PlacementConfig> getDefaultStructurePlacement() {
        return structurePlacement.get();
    }

    public Path getPath() {
        return path;
    }

    public ConfigType getType() {
        return type;
    }

    public Identifier toDefaultRL(String location) {
        if (location.contains(":")) return Identifier.parse(location);
        else if (defaultNamespace.equals(Constants.MC_ID)) return Identifier.withDefaultNamespace(location);
        else return Identifier.fromNamespaceAndPath(defaultNamespace, location);
    }

    public String toDefaultString(Identifier location) {
        return location.getNamespace().equals(defaultNamespace)
                ? location.getPath()
                : location.toString();
    }

    public void getDefaultNamespace() {
        Map<String, Integer> namespaceCounts = new HashMap<>();

        for (StructureSetData data : structureSetHolders) {
            for (Identifier set : data.sets()) {
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