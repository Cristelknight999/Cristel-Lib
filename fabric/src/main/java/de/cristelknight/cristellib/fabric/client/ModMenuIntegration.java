package de.cristelknight.cristellib.fabric.client;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screenFactory -> Util.isClothConfigLoaded() ? new ScreenBuilder(CristelLib.MOD_ID).create(screenFactory,true, true) : null;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        if(!Util.isClothConfigLoaded()) return Map.of();
        ACConfig acConfig = ConfigRegistry.get(ACConfig.class);
        boolean structureEnabled = !acConfig.disableAutoConfigScreens();

        Map<String, ConfigScreenFactory<?>> screens = new HashMap<>();
        for(String modID : ScreenBuilder.allConfigMods(structureEnabled)){
            Pair<Boolean, Boolean> structureSimple = ScreenBuilder.shouldCreateScreen(modID, structureEnabled);
            boolean structure = structureSimple.getFirst();
            boolean simple = structureSimple.getSecond();
            if(!structure && !simple) continue;

            screens.put(modID, (providedConfigScreenFactories) ->
                    new ScreenBuilder(modID).create(providedConfigScreenFactories, structure, simple)
            );
        }

        return screens;
    }
}
