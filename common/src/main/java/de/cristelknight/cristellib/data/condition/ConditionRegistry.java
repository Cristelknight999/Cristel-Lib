package de.cristelknight.cristellib.data.condition;

import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.data.condition.conditions.ModLoadedCondition;
import de.cristelknight.cristellib.data.condition.conditions.NotCondition;
import de.cristelknight.cristellib.data.condition.conditions.OrCondition;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ConditionRegistry {

    private static final Map<String, Codec<? extends ICondition>> CONDITIONS = new HashMap<>();

    protected static Codec<? extends ICondition> getCodec(String type) {
        return CONDITIONS.get(type);
    }

    private static void registerCondition(String type, Codec<? extends ICondition> codec) {
        CONDITIONS.put(type, codec);
    }

    public static void registerCondition(Identifier type, Codec<? extends ICondition> codec) {
        registerCondition(type.toString(), codec);
    }

    public static void init() {
        registerCondition("mod_loaded", ModLoadedCondition.CODEC);
        registerCondition("or", OrCondition.CODEC);
        registerCondition("not", NotCondition.CODEC);
    }

}
