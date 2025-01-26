package de.cristelknight.cristellib.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record StructureSetHolder(String modID, List<ResourceLocation> sets) {

    public static final Codec<StructureSetHolder> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.STRING.fieldOf("modid").forGetter(config -> String.valueOf(config.modID())),
                    Codec.list(ResourceLocation.CODEC).fieldOf("structure_set").orElse(List.of()).forGetter(config -> config.sets)
            ).apply(builder, StructureSetHolder::new)
    );

    @Override
    public String toString() {
        return modID() + " " + sets.toString();
    }
}
