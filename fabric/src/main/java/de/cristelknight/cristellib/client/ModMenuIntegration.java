package de.cristelknight.cristellib.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
import de.cristelknight.cristellib.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screenFactory -> Util.isClothConfigLoaded() ? new ScreenBuilder(Constants.MOD_ID).create(screenFactory) : null;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        if (!Util.isClothConfigLoaded()) return Map.of();

        Map<String, ConfigScreenFactory<?>> screens = new HashMap<>();
        for (String modId : ScreenBuilder.allModsWithScreen()) {

            screens.put(modId, (providedConfigScreenFactories) ->
                    new ScreenBuilder(modId).create(providedConfigScreenFactories)
            );
        }

        return screens;
    }


}