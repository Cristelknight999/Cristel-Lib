package de.cristelknight.cristellib.data.condition;

import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.data.condition.conditions.ConfigValueCondition;
import de.cristelknight.cristellib.data.condition.conditions.ModLoadedCondition;
import de.cristelknight.cristellib.data.condition.conditions.NotCondition;
import de.cristelknight.cristellib.data.condition.conditions.OrCondition;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConditionRegistry {

    /*
    Could replace this with a Registry, but then I need to deal with NeoForge nonsense:
    public static final Registry<MapCodec<? extends ICondition<?>>> REGISTRY = new MappedRegistry<>(
            ResourceKey.createRegistryKey(Constants.id("condition_types")), Lifecycle.stable());
     */

    private static final ConcurrentMap<String, Codec<? extends ICondition<?>>> CONDITIONS = new ConcurrentHashMap<>();

    protected static Codec<? extends ICondition<?>> getCodec(String type) {
        return CONDITIONS.get(type);
    }

    protected static String getType(Codec<? extends ICondition<?>> codec) {
        for (Map.Entry<String, Codec<? extends ICondition<?>>> codecEntry : CONDITIONS.entrySet()) {
            if (codecEntry.getValue().equals(codec)) {
                return codecEntry.getKey();
            }
        }
        throw new RuntimeException(Constants.getWithPrefix("Unregistered Codec, cannot get ICondition"));
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
        registerCondition("config_value", ConfigValueCondition.CODEC);
    }

}
