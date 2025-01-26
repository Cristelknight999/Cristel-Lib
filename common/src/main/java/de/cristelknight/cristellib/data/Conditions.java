package de.cristelknight.cristellib.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.util.ModVersionComparator;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.Optional;

public class Conditions {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean readConditions(Optional<List<JsonElement>> conditions){
        return conditions.isEmpty() || readConditions(conditions.get());
    }

    public static boolean readConditions(List<JsonElement> jsonElements){
        boolean bl = true;
        for(JsonElement e : jsonElements){
            if(!(e instanceof JsonObject o)) continue;
            if(!readCondition(o)) bl = false;
        }

        return bl;
    }

    public static boolean readCondition(JsonObject object){
        String type = GsonHelper.getAsString(object, "type");
        if(type.equals("mod_loaded")){
            return ModLoadingUtil.isModLoaded(GsonHelper.getAsString(object, "mod"));
        }
        else if(type.equals("mod_loaded_with_version")){
            String version = GsonHelper.getAsString(object, "version");
            for (ModVersionComparator comparator : ModVersionComparator.values()){
                String sign = comparator.getSerialized();
                if(!version.startsWith(sign)) continue;

                return comparator.test(GsonHelper.getAsString(object, "mod"), version.replaceFirst(sign, ""));
            }
            CristelLib.LOGGER.warn("Couldn't compare \"min_version\" value: {}", version);
        }

        return false;
    }

    public static MapCodec<Optional<List<JsonElement>>> CODEC = Codec.list(Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            jsonObject -> new Dynamic<>(JsonOps.INSTANCE, jsonObject)
    )).optionalFieldOf("conditions");

}
