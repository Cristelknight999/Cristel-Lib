package de.cristelknight.cristellib.config.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.Constants;

import java.util.Map;

public record PlacementConfig(double frequency, int salt, int separation, int spacing) {

    public PlacementConfig {
        // Ensure spacing is greater than or equal to separation
        if (spacing <= separation)
            throw new IllegalArgumentException(Constants.getWithPrefix("Spacing must be greater than separation!"));
        if(frequency < 0)
            throw new IllegalArgumentException(Constants.getWithPrefix("Frequency must be greater or equal to zero."));
    }

    public static final Codec<PlacementConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.DOUBLE.optionalFieldOf("frequency", 1.0).forGetter(config -> config.frequency),
                    Codec.INT.fieldOf("salt").forGetter(config -> config.salt),
                    Codec.INT.fieldOf("separation").forGetter(config -> config.separation),
                    Codec.INT.fieldOf("spacing").forGetter(config -> config.spacing)
            ).apply(builder, PlacementConfig::new)
    );

    public static final Codec<Map<String, PlacementConfig>> PLACEMENT_CODEC = Codec.unboundedMap(
            Codec.STRING, // String keys
            PlacementConfig.CODEC // NestedStructure values
    );

}
