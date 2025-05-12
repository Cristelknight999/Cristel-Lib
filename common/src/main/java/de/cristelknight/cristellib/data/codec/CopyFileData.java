package de.cristelknight.cristellib.data.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.Conditions;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record CopyFileData(ResourceLocation location, String destination, Optional<List<JsonElement>> conditions) {

    public static final Codec<CopyFileData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("location").forGetter(config -> config.location),
                    Codec.STRING.fieldOf("destination").forGetter(config -> config.destination),
                    Conditions.CODEC.forGetter(config -> config.conditions)
            ).apply(builder, CopyFileData::new)
    );

}