package de.cristelknight.cristellib.data.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.Conditions;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record CopyFileData(Identifier location, String destination, Optional<List<JsonElement>> conditions) {

    public static final Codec<CopyFileData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("location").forGetter(config -> config.location),
                    Codec.STRING.fieldOf("destination").forGetter(config -> config.destination),
                    Conditions.CODEC.optionalFieldOf("conditions").forGetter(config -> config.conditions)
            ).apply(builder, CopyFileData::new)
    );

}