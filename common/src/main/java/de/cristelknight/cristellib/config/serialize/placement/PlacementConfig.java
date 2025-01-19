package de.cristelknight.cristellib.config.serialize.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record PlacementConfig(int spacing, int separation, double frequency, int salt) {

    public static void loadClass(){}

    public PlacementConfig {
        // Ensure spacing is greater than or equal to separation
        if (spacing < separation) {
            throw new IllegalArgumentException("Spacing must be greater than or equal to separation.");
        }
    }

    public static final Codec<PlacementConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.INT.fieldOf("spacing").forGetter(config -> config.spacing),
                    Codec.INT.fieldOf("separation").forGetter(config -> config.separation),
                    Codec.DOUBLE.optionalFieldOf("frequency", 1.0).forGetter(config -> config.frequency),
                    Codec.INT.fieldOf("salt").forGetter(config -> config.salt)
            ).apply(builder, PlacementConfig::new)
    );

    public static final Codec<Map<String, PlacementConfig>> PLACEMENT_CODEC = Codec.unboundedMap(
            Codec.STRING, // String keys
            PlacementConfig.CODEC // NestedStructure values
    );

}
