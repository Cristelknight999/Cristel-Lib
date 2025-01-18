package de.cristelknight.cristellib.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.ConfigUtil;
import de.cristelknight.cristellib.config.Placement;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadStructureSets {

    public static Map<ResourceLocation, List<String>> readSetsAndAddStructures(List<Pair<String, ResourceLocation>> structureSets) {
        Map<ResourceLocation, List<String>> structures = new HashMap<>();
        for(Pair<String, ResourceLocation> p : structureSets){
            ResourceLocation setLocation = p.getSecond();
            JsonElement e = ConfigUtil.getSetElement(p.getFirst(), setLocation);
            if(e == null){
                CristelLib.LOGGER.error("Set for {} {} is not a JsonObject", p.getFirst(), setLocation);
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

    public static Map<ResourceLocation, Placement> readSetsAndAddPlacements(List<Pair<String, ResourceLocation>> structureSets) {
        Map<ResourceLocation, Placement> structurePlacement = new HashMap<>();
        for(Pair<String, ResourceLocation> pair : structureSets){
            ResourceLocation setLocation = pair.getSecond();
            JsonElement e = ConfigUtil.getSetElement(pair.getFirst(), setLocation);
            if(e == null){
                CristelLib.LOGGER.error("Set for {} {} is not a JsonObject", pair.getFirst(), setLocation);
                continue;
            }
            JsonObject a = e.getAsJsonObject().get("placement").getAsJsonObject();
            Placement p = new Placement();



            JsonElement salt = a.get("salt");
            JsonElement spacing = a.get("spacing");
            JsonElement separation = a.get("separation");
            JsonElement frequency = a.get("frequency");


            if(salt != null) p.salt = salt.getAsInt();
            if(spacing != null) p.spacing = spacing.getAsInt();
            if(separation != null) p.separation = separation.getAsInt();
            if(frequency != null) p.frequency = frequency.getAsDouble();

            structurePlacement.put(setLocation, p);
        }
        return structurePlacement;
    }

}
