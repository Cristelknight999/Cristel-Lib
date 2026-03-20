package de.cristelknight.cristellib.config.structure;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.structure.placement.PlacementConfig;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import de.cristelknight.cristellib.util.JsonHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReadStructureSets {

    public static Map<Identifier, List<Identifier>> readSetsAndAddStructures(List<StructureSetData> structureSetHolder) {
        ImmutableMap.Builder<Identifier, List<Identifier>> structures = new ImmutableMap.Builder<>();
        structureSetHolder.forEach(holder -> holder.sets().forEach(setLocation -> {
            String modId = holder.modId();

            JsonElement e = JsonHelper.getSetElement(modId, setLocation);
            if(checkElement(e, modId, setLocation)) return;


            JsonArray structureArray = GsonHelper.getAsJsonArray(e.getAsJsonObject(), "structures");
            List<Identifier> structureList = new ArrayList<>();
            for(JsonElement element : structureArray){
                if(!element.isJsonObject()) continue;

                structureList.add(Identifier.tryParse(GsonHelper.getAsString(element.getAsJsonObject(), "structure")));
            }
            structures.put(setLocation, structureList);

        }));
        return structures.build();
    }

    public static Map<Identifier, PlacementConfig> readSetsAndAddPlacements(List<StructureSetData> structureSetHolder) {
        ImmutableMap.Builder<Identifier, PlacementConfig> structurePlacement = new ImmutableMap.Builder<>();
        structureSetHolder.forEach(holder -> holder.sets().forEach(setLocation -> {
            String modId = holder.modId();

            JsonElement e = JsonHelper.getSetElement(modId, setLocation);
            if(checkElement(e, modId, setLocation)) return;


            JsonObject placement = GsonHelper.getAsJsonObject(e.getAsJsonObject(), "placement");
            PlacementConfig config = ConfigManager.readElement(String.format("Couldn't read %s in %s, crashing instead. Maybe try to delete the config files!", setLocation, modId), PlacementConfig.CODEC, JsonOps.INSTANCE, placement);
            structurePlacement.put(setLocation, config);

        }));
        return structurePlacement.build();
    }

    private static boolean checkElement(JsonElement element, String modId, Identifier setLocation) {
        if(element == null || !element.isJsonObject()){
            Constants.LOGGER.error("Set for {} {} is not a JsonObject", modId, setLocation);
            return true;
        }
        return false;
    }
}
