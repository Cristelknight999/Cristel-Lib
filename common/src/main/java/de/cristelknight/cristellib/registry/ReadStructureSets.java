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
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.data.StructureSetHolder;
import de.cristelknight.cristellib.util.JanksonUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.*;

public class ReadStructureSets {

    public static Map<ResourceLocation, List<String>> readSetsAndAddStructures(List<StructureSetHolder> structureSetHolder) {
        Map<ResourceLocation, List<String>> structures = new HashMap<>();
        structureSetHolder.forEach(holder -> holder.sets().forEach(setLocation -> {
            String modID = holder.modID();

            JsonElement e = JanksonUtil.getSetElement(modID, setLocation);
            if(checkElement(e, modID, setLocation)) return;


            JsonArray structureArray = GsonHelper.getAsJsonArray(e.getAsJsonObject(), "structures");
            List<String> structureList = new ArrayList<>();
            for(JsonElement element : structureArray){
                if(!element.isJsonObject()) continue;
                structureList.add(GsonHelper.getAsString(element.getAsJsonObject(), "structure"));
            }
            structures.put(setLocation, structureList);

        }));
        return structures;
    }

    public static Map<String, PlacementConfig> readSetsAndAddPlacements(List<StructureSetHolder> structureSetHolder) {
        Map<String, PlacementConfig> structurePlacement = new HashMap<>();
        structureSetHolder.forEach(holder -> holder.sets().forEach(setLocation -> {
            String modID = holder.modID();

            JsonElement e = JanksonUtil.getSetElement(modID, setLocation);
            if(checkElement(e, modID, setLocation)) return;


            JsonObject placement = GsonHelper.getAsJsonObject(e.getAsJsonObject(), "placement");
            PlacementConfig config = ConfigManager.readElement(String.format("Couldn't read %s in %s, crashing instead. Maybe try to delete the config files!", setLocation, modID), PlacementConfig.CODEC, JsonOps.INSTANCE, placement);
            structurePlacement.put(setLocation.getPath(), config);

        }));
        return structurePlacement;
    }

    private static boolean checkElement(JsonElement element, String modID, ResourceLocation setLocation) {
        if(!element.isJsonObject()){
            CristelLib.LOGGER.error("Set for {} {} is not a JsonObject", modID, setLocation);
            return true;
        }
        return false;
    }
}
