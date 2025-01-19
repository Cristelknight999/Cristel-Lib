package de.cristelknight.cristellib.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.ConfigUtil;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.util.JanksonUtil;
import de.cristelknight.cristellib.util.jankson.JanksonOps;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ReadStructureSets {

    public static Map<ResourceLocation, List<String>> readSetsAndAddStructures(List<Pair<String, ResourceLocation>> structureSets) {
        Map<ResourceLocation, List<String>> structures = new HashMap<>();
        for(Pair<String, ResourceLocation> pair : structureSets){
            String modID = pair.getFirst();
            ResourceLocation setLocation = pair.getSecond();
            JsonElement e = JanksonUtil.getSetElement(modID, setLocation);
            if(e == null){
                CristelLib.LOGGER.error("Set for {} {} is not a JsonObject", modID, setLocation);
                continue;
            }
            JsonArray a = e.getAsJsonObject().get("structures").getAsJsonArray();
            List<String> structureList = new ArrayList<>();
            for(JsonElement element : a){
                if(element instanceof JsonObject) {
                    structureList.add(element.getAsJsonObject().get("structure").getAsString());
                }
            }
            structures.put(setLocation, structureList);
        }
        return structures;
    }

    public static Map<String, PlacementConfig> readSetsAndAddPlacements(List<Pair<String, ResourceLocation>> structureSets) {
        Map<String, PlacementConfig> structurePlacement = new HashMap<>();
        for(Pair<String, ResourceLocation> pair : structureSets){
            String modID = pair.getFirst();
            ResourceLocation setLocation = pair.getSecond();


            JsonElement e = JanksonUtil.getSetElement(modID, setLocation);
            if(e == null){
                CristelLib.LOGGER.error("Set for {} {} is not a JsonObject", modID, setLocation);
                continue;
            }
            JsonObject placement = e.getAsJsonObject().get("placement").getAsJsonObject();

            PlacementConfig config = readElement(setLocation.toString(), PlacementConfig.CODEC, JsonOps.INSTANCE, placement);
            structurePlacement.put(setLocation.getPath(), config);
        }
        return structurePlacement;
    }

    public static <T> T readElement(String path, Codec<T> codec, DynamicOps<JsonElement> ops, JsonElement load) {
        DataResult<Pair<T, JsonElement>> decode = codec.decode(ops, load);
        Optional<DataResult.Error<Pair<T, JsonElement>>> error = decode.error();
        if (error.isPresent()) {
            throw new IllegalArgumentException("["+CristelLib.MOD_ID+"] Couldn't read " + path + ", crashing instead. Maybe try to delete the config files!");
        }
        return decode.result().orElseThrow().getFirst();
    }

}
