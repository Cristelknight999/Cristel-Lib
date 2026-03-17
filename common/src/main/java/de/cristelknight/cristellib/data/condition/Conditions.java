package de.cristelknight.cristellib.data.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.config.ConfigManager;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.Optional;

public class Conditions {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean readConditions(Optional<List<JsonElement>> conditions){
        return conditions.isEmpty() || readConditions(conditions.get());
    }

    public static boolean readConditions(List<JsonElement> jsonElements){
        for(JsonElement e : jsonElements){
            if(!(e instanceof JsonObject o)) continue;
            if(!readCondition(o)) return false;
        }
        return true;
    }

    public static boolean readCondition(JsonObject object) {
        String type = GsonHelper.getAsString(object, "type");
        Codec<? extends ICondition> conditionCodec = ConditionRegistry.getCodec(type);
        object.remove("type");
        ICondition condition = ConfigManager.readElement("Couldn't read condition of type: " + type, conditionCodec, JsonOps.INSTANCE, object);
        return condition.test();
    }

    public static final Codec<List<JsonElement>> CODEC = Codec.list(Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            jsonObject -> new Dynamic<>(JsonOps.INSTANCE, jsonObject)
    ));
}
