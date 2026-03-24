package de.cristelknight.cristellib.data.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.ConditionNode;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record BuiltInPackData(Identifier location, String displayName, Optional<ConditionNode> conditionNode) {

    public static final Codec<BuiltInPackData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Identifier.CODEC.fieldOf("location").forGetter(BuiltInPackData::location),
                    Codec.STRING.fieldOf("display_name").forGetter(BuiltInPackData::displayName),
                    ConditionNode.CODEC.optionalFieldOf("condition").forGetter(BuiltInPackData::conditionNode)
            ).apply(builder, BuiltInPackData::new)
    );

    public static final Codec<Either<BuiltInPackData, BuiltInPackDataWrapper>> PACKS_CODEC = Codec.either(CODEC, BuiltInPackDataWrapper.CODEC);

}