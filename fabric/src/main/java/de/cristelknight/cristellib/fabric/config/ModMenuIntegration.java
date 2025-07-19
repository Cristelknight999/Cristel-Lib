package de.cristelknight.cristellib.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.config.client.ScreenBuilder;

import java.util.HashMap;
import java.util.Map;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screenFactory)  -> new ScreenBuilder().createP(screenFactory, CristelLib.MOD_ID);
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> screens = new HashMap<>();

        for(String modID : CristelLibRegistry.getConfigs().keySet()){
            if(modID.equals(CristelLib.MOD_ID)) continue;
            screens.put(modID, (providedConfigScreenFactories) -> new ScreenBuilder().createP(providedConfigScreenFactories, modID));
        }

        return screens;
    }
}
