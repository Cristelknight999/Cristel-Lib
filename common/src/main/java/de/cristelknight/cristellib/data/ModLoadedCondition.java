package de.cristelknight.cristellib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.ModLoadingUtil;

public record ModLoadedCondition(String modId) implements ICondition {

    public static final MapCodec<ModLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.fieldOf("mod").forGetter(ModLoadedCondition::modId)
            ).apply(instance, ModLoadedCondition::new));

    @Override
    public boolean test() {
        return ModLoadingUtil.isModLoaded(modId);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
