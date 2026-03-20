package de.cristelknight.cristellib.data.condition.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.simple.ConfigHolder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.data.condition.ICondition;
import de.cristelknight.cristellib.util.JsonHelper;

import java.util.List;

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
            clazz = Class.forName(className); // TODO: should initialize or not?
        } catch (ClassNotFoundException e) {
            Constants.LOGGER.warn("Couldn't parse class_name: {} for ConfigValueCondition", className);
            return false;
        }
        JsonElement element = fromClass(clazz);
        if (!(element instanceof JsonObject object))
            return false;

        List<JsonElement> possibleFinds = JsonHelper.findAll(key, object, "");
        for (JsonElement actual : possibleFinds) {
            if (actual.equals(expected)) // TODO: semantic comparison
                return true;
        }

        return false;
    }

    private static <T> JsonElement fromClass(Class<T> clazz) {
        ConfigHolder<T> holder = ConfigRegistry.holder(clazz);
        return ConfigManager.createElement("Couldn't write config for class: " + clazz.getName(), holder.getSettings().getCodec(), JsonOps.INSTANCE, holder.getInstance());
    }

    @Override
    public Codec<ConfigValueCondition> getCodec() {
        return CODEC;
    }

}
