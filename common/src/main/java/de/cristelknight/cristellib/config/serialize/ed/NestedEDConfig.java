package de.cristelknight.cristellib.config.serialize.ed;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.Map;

public record NestedEDConfig(Map<String, Entry> entries) {

    public record Entry(Boolean value, NestedEDConfig nested) {

        public boolean isBoolean() {
                return value != null;
            }

            public static final Codec<Entry> CODEC = Codec.either(
                    Codec.BOOL,
                    Codec.lazyInitialized(() -> NestedEDConfig.CODEC)
            ).xmap(
                    either -> either.map(b -> new Entry(b, null), n -> new Entry(null, n)), //read
                    entry -> entry.isBoolean() ? Either.left(entry.value) : Either.right(entry.nested) //write
            );

            public static Entry ofBoolean(Boolean value) {
                return new Entry(value, null);
            }

            public static Entry ofNested(NestedEDConfig nested) {
                return new Entry(null, nested);
            }
        }

    public static final Codec<NestedEDConfig> CODEC = Codec.unboundedMap(
            Codec.STRING,
            Entry.CODEC
    ).xmap(NestedEDConfig::new, NestedEDConfig::entries);

    public static final Codec<Map<String, NestedEDConfig>> ED_CODEC = Codec.unboundedMap(
            Codec.STRING, // String keys
            NestedEDConfig.CODEC // NestedStructure values
    );
}
