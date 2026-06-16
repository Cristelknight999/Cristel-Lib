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
        return Util.isClothConfigLoaded() ?
                screenFactory -> new ScreenBuilder(Constants.MOD_ID).create(screenFactory)
                : ModMenuApi.super.getModConfigScreenFactory();
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        if (!Util.isClothConfigLoaded())
            return ModMenuApi.super.getProvidedConfigScreenFactories();

        Map<String, ConfigScreenFactory<?>> screens = new HashMap<>();
        for (String modId : ScreenBuilder.allModsWithScreen()) {
            screens.put(modId, (providedConfigScreenFactories) ->
                    new ScreenBuilder(modId).create(providedConfigScreenFactories)
            );
        }

        return screens;
    }


}