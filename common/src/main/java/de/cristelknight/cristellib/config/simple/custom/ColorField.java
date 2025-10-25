package de.cristelknight.cristellib.config.simple.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ARGB;

public record ColorField(int red, int green, int yellow) {

    public static final Codec<ColorField> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.INT.fieldOf("red").forGetter(ColorField::red),
                    Codec.INT.fieldOf("green").forGetter(ColorField::green),
                    Codec.INT.fieldOf("yellow").forGetter(ColorField::yellow)
            ).apply(builder, ColorField::new)
    );

    public int toInt() {
        return ARGB.color(red, green, yellow);
    }

    public static ColorField fromInt(int color) {
        return new ColorField(ARGB.red(color), ARGB.green(color), ARGB.blue(color));
    }
}
