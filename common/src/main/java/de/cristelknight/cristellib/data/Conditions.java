package de.cristelknight.cristellib.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.util.ModVersionComparator;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Conditions {

    private static Map<String, Codec<ICondition>> CONDITIONS = new HashMap<>();

    public static void registerCond(String type, Codec<ICondition> codec) {

    }

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
            object.remove("type");
            ModLoadedCondition condition = ConfigManager.readElement("idj", ModLoadedCondition.CODEC, JsonOps.INSTANCE, object);
            condition.test()
            return ModLoadingUtil.isModLoaded(GsonHelper.getAsString(object, "mod"));
        }
        else if(type.equals("mod_loaded_with_version")){
            String version = GsonHelper.getAsString(object, "version");
            String mod = GsonHelper.getAsString(object, "mod");
            for (ModVersionComparator comparator : ModVersionComparator.values()){
                String sign = comparator.getSerialized();
                if(!version.startsWith(sign)) continue;

                return comparator.test(mod, version.replaceFirst(sign, ""));
            }
            CristelLib.LOGGER.warn("Couldn't compare \"version\": \"{}\" of \"mod\": \"{}\"", version, mod);
        }

        return false;
    }

    public static final MapCodec<Optional<List<JsonElement>>> CODEC = Codec.list(Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            jsonObject -> new Dynamic<>(JsonOps.INSTANCE, jsonObject)
    )).optionalFieldOf("conditions");
}
