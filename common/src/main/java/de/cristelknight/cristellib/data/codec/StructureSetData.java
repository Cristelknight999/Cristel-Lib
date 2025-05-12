package de.cristelknight.cristellib.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record StructureSetData(String modID, List<ResourceLocation> sets) {

    public static final Codec<StructureSetData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.STRING.fieldOf("modid").forGetter(config -> String.valueOf(config.modID())),
                    Codec.list(ResourceLocation.CODEC).fieldOf("structure_set").orElse(List.of()).forGetter(config -> config.sets)
            ).apply(builder, StructureSetData::new)
    );

    @Override
    public @NotNull String toString() {
        return modID() + " " + sets.toString();
    }
}
