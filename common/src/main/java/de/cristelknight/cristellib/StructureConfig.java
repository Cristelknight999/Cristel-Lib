package de.cristelknight.cristellib;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.registry.ReadStructureSets;
import de.cristelknight.cristellib.util.JanksonUtil;
import de.cristelknight.cristellib.util.RuntimePackUtil;
import de.cristelknight.cristellib.config.ConfigUtil;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.*;

public class StructureConfig {

    private final Path path;

    private String header = "";

    private Map<String, String> comments = new HashMap<>();

    private final ConfigType type;

    private final List<Pair<String, ResourceLocation>> structureSets = new ArrayList<>();

    // default values
    private final Supplier<Map<ResourceLocation, List<String>>> structuresForED =
            Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSets));

    private final Supplier<Map<String, PlacementConfig>> structurePlacement =
            Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddPlacements(structureSets));

    // current values
    private Map<String, Boolean> enableDisableConfig = null;
    private Map<String, PlacementConfig> placementConfig = null;


    private StructureConfig(Path path, ConfigType type) {
        this.path = path;
        this.type = type;
    }

    void addSet(Pair<String, ResourceLocation> set) {
        structureSets.add(set);
    }

    void addSetsToRuntimePack() {
        if(type.equals(ConfigType.ENABLE_DISABLE)) enableDisableConfig = ConfigUtil.readEDConfig(this);
        else placementConfig = ConfigUtil.readPlacementConfig(this);

        for(Pair<String, ResourceLocation> s : structureSets) {
            String modID = s.getFirst();
            ResourceLocation setLocation = s.getSecond();

            JsonElement structureSetElement = getStructureSet(setLocation, modID);
            if (!(structureSetElement instanceof JsonObject structureSet)) {
                CristelLib.LOGGER.warn("Set for {} {} is not a JsonObject, skipping...", modID, setLocation);
                continue;
            }

            JsonObject originalSet = structureSet.deepCopy();

            if(type.equals(ConfigType.ENABLE_DISABLE)) {
                removeStructureInSets(structureSet);
            } else if(type.equals(ConfigType.PLACEMENT)) {
                updatePlacementsInSet(structureSet, setLocation);
            }

            if(!structureSet.equals(originalSet)){
                CristelLib.DATA_PACK.addStructureSet(setLocation, structureSet);
            }
        }
    }

    private void removeStructureInSets(JsonObject structureSet){
        JsonArray array = structureSet.get("structures").getAsJsonArray();
        Iterator<JsonElement> structureIterator = array.iterator();
        while (structureIterator.hasNext()){
            JsonElement structure = structureIterator.next();
            String structureName = structure.getAsJsonObject().get("structure").getAsString().split(":")[1];
            if(enableDisableConfig.containsKey(structureName) && !enableDisableConfig.get(structureName)) structureIterator.remove();
        }
    }

    private void updatePlacementsInSet(JsonObject structureSet, ResourceLocation setLocation){
        String structureSetName = setLocation.getPath();
        if(placementConfig.containsKey(structureSetName)) {
            JsonObject a = structureSet.get("placement").getAsJsonObject();
            PlacementConfig p = placementConfig.get(structureSetName);
            a.addProperty("salt", p.salt());
            a.addProperty("spacing", p.spacing());
            a.addProperty("separation", p.separation());

            double f = p.frequency();

            if(f != 0 && a.has("frequency") && (a.get("frequency").getAsFloat() != f)) a.addProperty("frequency", f);
        }
    }

    private JsonElement getStructureSet(ResourceLocation location, String modID) {
        ResourceLocation structureLocation = RuntimePackUtil.getLocationForStructureSet(location);
        if (CristelLib.DATA_PACK.hasResource(structureLocation)) {
            return CristelLib.DATA_PACK.getResource(structureLocation);
        }
        return JanksonUtil.getSetElement(modID, location);
    }


    /**
     * Writes the configuration to the filesystem.
     * This method initializes the structures or placements depending on the {@link ConfigType}.
     */
    void writeConfig() {
        if(type.equals(ConfigType.ENABLE_DISABLE)){
            ConfigUtil.createEDConfig(this, false);
        }
        else if(type.equals(ConfigType.PLACEMENT)){
            ConfigUtil.createPlacementConfig(this, false);
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

    public void setComments(Map<String, String> comments){
        this.comments = comments;
    }

    public void setHeader(String header){
        this.header = "/*\n" + header + "*/";
    }

    public String getHeader(){
        return header;
    }

    public Map<String, String> getComments() {
        return comments;
    }

    public Map<ResourceLocation, List<String>> getStructures() {
        return structuresForED.get();
    }

    public Map<String, PlacementConfig> getStructurePlacement() {
        return structurePlacement.get();
    }

    public Path getPath() {
        return path;
    }
}
