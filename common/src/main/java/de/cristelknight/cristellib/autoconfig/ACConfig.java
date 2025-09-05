package de.cristelknight.cristellib.autoconfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.ConfigSettings;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record ACConfig(
        boolean disableAutoConfig,
        boolean disableAutoConfigScreens,
        List<String> blacklistedMods,
        List<String> clientExcludedMods,
        List<String> modOverrideWhitelist
) {

    public static final Codec<ACConfig> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("disableAutoConfig").forGetter(ACConfig::disableAutoConfig),
                    Codec.BOOL.fieldOf("disableAutoConfigScreens").forGetter(ACConfig::disableAutoConfigScreens),
                    Codec.list(Codec.STRING).fieldOf("blacklistedMods").forGetter(ACConfig::blacklistedMods),
                    Codec.list(Codec.STRING).fieldOf("clientExcludedMods").forGetter(ACConfig::clientExcludedMods),
                    Codec.list(Codec.STRING).fieldOf("modOverrideWhitelist").forGetter(ACConfig::modOverrideWhitelist)
            ).apply(builder, ACConfig::new)
    );

    public static void updateConfig() {
        ACConfig config = ConfigRegistry.get(ACConfig.class);

        // default values
        List<String> defaultBlacklistedMods = new ArrayList<>(SETTINGS.getDefault().blacklistedMods());
        List<String> defaultClientExcludedMods = new ArrayList<>(SETTINGS.getDefault().clientExcludedMods());

        // Current values
        List<String> modOverrideWhitelist = new ArrayList<>(config.modOverrideWhitelist());
        List<String> blacklistedMods = new ArrayList<>(config.blacklistedMods());
        List<String> clientExcludedMods = new ArrayList<>(config.clientExcludedMods());

        // return if no default values (and no whitelisted mods)
        if(defaultBlacklistedMods.isEmpty() && defaultClientExcludedMods.isEmpty() && modOverrideWhitelist.isEmpty()) return;

        // remove all defaults temporarily
        blacklistedMods.removeAll(defaultBlacklistedMods);
        clientExcludedMods.removeAll(defaultClientExcludedMods);

        // Whitelist only applies to mods that are actually blacklisted
        List<String> allDefaultBlacklists = new ArrayList<>(SETTINGS.getDefault().blacklistedMods());
        allDefaultBlacklists.addAll(SETTINGS.getDefault().clientExcludedMods());
        modOverrideWhitelist.retainAll(allDefaultBlacklists);

        // apply overrides
        defaultBlacklistedMods.removeAll(modOverrideWhitelist);
        defaultClientExcludedMods.removeAll(modOverrideWhitelist);

        // add defaults back
        blacklistedMods.addAll(defaultBlacklistedMods);
        clientExcludedMods.addAll(defaultClientExcludedMods);

        ConfigRegistry.updateAndSave(new ACConfig(config.disableAutoConfig(), config.disableAutoConfigScreens(),
                blacklistedMods, clientExcludedMods, modOverrideWhitelist));
    }

    public static final ConfigSettings<ACConfig> SETTINGS = new ConfigSettings<>() {
        @Override
        public String getSubPath() {
            return CristelLib.MOD_ID + "/auto_config_settings";
        }

        @Override
        public Codec<ACConfig> getCodec() {
            return CODEC;
        }

        @Override
        public ACConfig getDefault() {
            return new ACConfig(
                    false,
                    false,
                    ACInfoData.getBlackListedMods(),
                    ACInfoData.getClientBlackListedMods(),
                    List.of()
            );
        }

        @Override
        public String getHeader() {
            return """
                   Auto-Config Settings
                   The Config for Cristel Lib's automated structure config generation.
                   """;
        }

        @Override
        public HashMap<String, String> getComments() {
            return Util.make(new HashMap<>(), map -> {
                map.put("disableAutoConfig", """
                    Disable automatic config generation fully.""");
                map.put("disableAutoConfigScreens", """
                    Disable automatic screen generation for structure configs.""");

                map.put("blacklistedMods", """
                    Mods where automatic structure config generation is fully disabled.""");
                map.put("clientExcludedMods", """
                    Mods where automatic screen generation for structure configs is disabled.""");
                map.put("modOverrideWhitelist", """        
                    This list let's you override the default settings provided by mod authors.
                    If you add a mod that is blacklisted at default (in the two other lists)
                    you can now remove them without them getting added back. Proceed at your own risk.""");
            });
        }
    };

    static {
        ConfigRegistry.registerWithScreen(ACConfig.class, SETTINGS,
                CristelLib.MOD_ID, "Auto-config", ACConfig::updateConfig);
    }
}
