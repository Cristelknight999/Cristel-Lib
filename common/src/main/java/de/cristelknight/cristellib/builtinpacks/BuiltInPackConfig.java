package de.cristelknight.cristellib.builtinpacks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.ConfigSettings;

import java.util.ArrayList;
import java.util.List;

public record BuiltInPackConfig(List<String> defaultPacks, List<String> disabledPacks)  {

    public static final Codec<BuiltInPackConfig> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
            Codec.list(Codec.STRING).fieldOf("defaultPacks").forGetter(BuiltInPackConfig::defaultPacks),
            Codec.list(Codec.STRING).fieldOf("disabledPacks").forGetter(BuiltInPackConfig::disabledPacks)
        ).apply(builder, BuiltInPackConfig::new)
    );

    public static void updateConfig() {
        BuiltInPackConfig config = ConfigRegistry.get(BuiltInPackConfig.class);

        List<String> defaultPacks = new ArrayList<>(config.defaultPacks());
        List<String> disabledPacks = new ArrayList<>(config.disabledPacks());

        boolean bl = false;
        // Remove elements not in the default list
        if(defaultPacks.retainAll(SETTINGS.getDefault().defaultPacks())) bl = true;
        if(disabledPacks.retainAll(SETTINGS.getDefault().defaultPacks())) bl = true;

        // Add missing elements from the default list only if both lists miss them
        for (String item : SETTINGS.getDefault().defaultPacks()) {
            if (!defaultPacks.contains(item) && !disabledPacks.contains(item)) {
                defaultPacks.add(item);
                bl = true;
            }
        }

        if(bl) {
            ConfigRegistry.updateAndSave(new BuiltInPackConfig(defaultPacks, disabledPacks));
        }
    }

    public static final ConfigSettings<BuiltInPackConfig> SETTINGS = new ConfigSettings<>() {
        @Override
        public String getSubPath() {
            return CristelLib.MOD_ID + "/built_in_packs";
        }

        @Override
        public Codec<BuiltInPackConfig> getCodec() {
            return CODEC;
        }

        @Override
        public BuiltInPackConfig getDefault() {
            return new BuiltInPackConfig(BuiltInDataPackLoader.getIDs(), List.of());
        }

        @Override
        public String getHeader() {
            return """
                   This config file allows disabling built-in packs supplied by Cristel Lib.
                   Move entries from 'defaultPacks' to 'disabledPacks' to disable them.
                   """;
        }
    };

    static {
        ConfigRegistry.register(BuiltInPackConfig.class, SETTINGS);
    }
}
