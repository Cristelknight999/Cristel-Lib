package de.cristelknight.cristellib.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.ConditionNode;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record CopyFileData(Identifier location, String destination, Optional<ConditionNode> conditions) {

    public static final Codec<CopyFileData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("location").forGetter(config -> config.location),
                    Codec.STRING.fieldOf("destination").forGetter(config -> config.destination),
                    ConditionNode.CODEC.optionalFieldOf("condition").forGetter(config -> config.conditions)
            ).apply(builder, CopyFileData::new)
    );

}