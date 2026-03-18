package de.cristelknight.cristellib.data.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.ConfigManager;
import net.minecraft.util.GsonHelper;

import java.util.Map;

public interface ICondition<T extends ICondition<T>> {

    Codec<ICondition<?>> FULL_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> decode(dynamic.convert(JsonOps.INSTANCE).getValue()),
            condition -> new Dynamic<>(JsonOps.INSTANCE, encode(condition))
    );

    private static ICondition<?> decode(JsonElement e) {
        if (!(e instanceof JsonObject object))
            throw new RuntimeException(CristelLib.getWithPrefix("Expected ICondition to be an Object"));

        String type = GsonHelper.getAsString(object, "type");
        Codec<? extends ICondition<?>> conditionCodec = ConditionRegistry.getCodec(type);
        object.remove("type");
        return ConfigManager.readElement("Couldn't read ICondition of type: " + type, conditionCodec, JsonOps.INSTANCE, object);
    }

    private static <T extends ICondition<?>> JsonObject encode(T condition) {
        Codec<T> codec = (Codec<T>) condition.getCodec();
        String type = ConditionRegistry.getType(codec);
        if (type == null)
            throw new RuntimeException(CristelLib.getWithPrefix("Unregistered Codec for ICondition"));

        JsonElement e = ConfigManager.createElement("Couldn't encode ICondition", codec, JsonOps.INSTANCE, condition);
        if (!(e instanceof JsonObject object))
            throw new RuntimeException(CristelLib.getWithPrefix("Expected ICondition to be an Object"));

        JsonObject reordered = new JsonObject();
        reordered.addProperty("type", type);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            reordered.add(entry.getKey(), entry.getValue());
        }
        return reordered;
    }

    boolean test();

    Codec<T> getCodec();

}
