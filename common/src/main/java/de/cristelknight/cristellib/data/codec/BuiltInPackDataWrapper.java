package de.cristelknight.cristellib.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record BuiltInPackDataWrapper(List<BuiltInPackData> packs) {
    public static final Codec<BuiltInPackDataWrapper> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInPackData.CODEC.listOf().fieldOf("packs").forGetter(BuiltInPackDataWrapper::packs)
            ).apply(instance, BuiltInPackDataWrapper::new)
    );
}
