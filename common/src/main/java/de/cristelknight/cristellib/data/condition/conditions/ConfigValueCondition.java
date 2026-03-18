package de.cristelknight.cristellib.data.condition.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.simple.ConfigHolder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.data.condition.ICondition;

import java.util.Map;

public record ConfigValueCondition(String className, String key, JsonElement expected) implements ICondition<ConfigValueCondition> {

    private static final Codec<JsonElement> JSON_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            condition -> new Dynamic<>(JsonOps.INSTANCE, condition)
    );

    public static final Codec<ConfigValueCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("class_name").forGetter(ConfigValueCondition::className),
                    Codec.STRING.fieldOf("key").forGetter(ConfigValueCondition::key),
                    JSON_CODEC.fieldOf("expected").forGetter(ConfigValueCondition::expected)
            ).apply(instance, ConfigValueCondition::new));

    @Override
    public boolean test() {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            CristelLib.LOGGER.warn("Couldn't parse class_name: {} for ConfigValueCondition", className);
            return false;
        }
        JsonElement element = fromClass(clazz);
        if(!(element instanceof JsonObject object))
            return false;
        
        JsonElement actual = find(key, object, "");
        if (actual == null)
            return false;
        
        return actual.equals(expected); // TODO: semantic comparison
    }

    private static <T> JsonElement fromClass(Class<T> clazz) {
        ConfigHolder<T> holder = ConfigRegistry.holder(clazz);
        return ConfigManager.createElement(":(", holder.getSettings().getCodec(), JsonOps.INSTANCE, holder.getInstance());
    }

    public static JsonElement find(String searchedKey, JsonObject object, String parentKey) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String fullKey = parentKey + entry.getKey();
            JsonElement value = entry.getValue();

            JsonElement find = getElement(searchedKey, fullKey, value);
            if (find != null)
                return find;
        }

        return null;
    }

    public static JsonElement findInArray(String searchedKey, JsonArray array, String parentKey) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            String fullKey = parentKey + "." + getIndex(i);

            JsonElement find = getElement(searchedKey, fullKey, element);
            if (find != null)
                return find;
        }
        return null;
    }

    public static JsonElement getElement(String searchedKey, String fullKey, JsonElement value) {
        if (searchedKey.equals(fullKey))
            return value;

        if (value instanceof JsonArray array)
            return findInArray(searchedKey, array, fullKey + ".");

        if (value instanceof JsonObject nestedObject)
            return find(searchedKey, nestedObject, fullKey + ".");

        return null;
    }

    private static String getIndex(int i) {
        return "\\" + i + "\\";
    }

    @Override
    public Codec<ConfigValueCondition> getCodec() {
        return CODEC;
    }

}
