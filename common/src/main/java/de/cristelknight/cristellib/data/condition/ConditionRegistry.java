package de.cristelknight.cristellib.data.condition;

import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.data.condition.conditions.ModLoadedCondition;
import de.cristelknight.cristellib.data.condition.conditions.NotCondition;
import de.cristelknight.cristellib.data.condition.conditions.OrCondition;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ConditionRegistry {

    private static final Map<String, Codec<? extends ICondition<?>>> CONDITIONS = new HashMap<>();

    protected static Codec<? extends ICondition<?>> getCodec(String type) {
        return CONDITIONS.get(type);
    }

    protected static String getType(Codec<? extends ICondition<?>> codec) {
        for (Map.Entry<String, Codec<? extends ICondition<?>>> codecEntry : CONDITIONS.entrySet()) {
            if (codecEntry.getValue().equals(codec)) {
                return codecEntry.getKey();
            }
        }
        return null;
    }

    private static void registerCondition(String type, Codec<? extends ICondition<?>> codec) {
        CONDITIONS.put(type, codec);
    }

    @SuppressWarnings("unused")
    public static void registerCondition(Identifier type, Codec<? extends ICondition<?>> codec) {
        registerCondition(type.toString(), codec);
    }

    public static void init() {
        registerCondition("mod_loaded", ModLoadedCondition.CODEC);
        registerCondition("or", OrCondition.CODEC);
        registerCondition("not", NotCondition.CODEC);
    }

}
