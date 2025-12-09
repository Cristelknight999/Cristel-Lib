package de.cristelknight.cristellib.data.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.Conditions;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record BuiltInPackData(Identifier location, String displayName, Optional<List<JsonElement>> conditions) {

    public static final Codec<BuiltInPackData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("location").forGetter(config -> config.location),
                    Codec.STRING.fieldOf("display_name").forGetter(config -> config.displayName),
                    Conditions.CODEC.forGetter(config -> config.conditions)
            ).apply(builder, BuiltInPackData::new)
    );

    public static final Codec<Either<BuiltInPackData, BuiltInPackDataWrapper>> PACKS_CODEC = Codec.either(CODEC, BuiltInPackDataWrapper.CODEC);

}