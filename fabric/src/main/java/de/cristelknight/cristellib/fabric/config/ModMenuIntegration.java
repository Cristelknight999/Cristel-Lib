package de.cristelknight.cristellib.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;

import java.util.HashMap;
import java.util.Map;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screenFactory)  -> new ScreenBuilder().create(screenFactory, CristelLib.MOD_ID);
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        ACConfig acConfig = ConfigRegistry.get(ACConfig.class);
        if(acConfig.disableAutoConfig() || acConfig.disableAutoConfigScreens()) return Map.of();

        Map<String, ConfigScreenFactory<?>> screens = new HashMap<>();
        for(String modID : CristelLibRegistry.getConfigs().keySet()){
            if(acConfig.clientExcludedMods().contains(modID) || modID.equals(CristelLib.MOD_ID) || modID.equals("minecraft")) continue;
            screens.put(modID, (providedConfigScreenFactories) -> new ScreenBuilder().create(providedConfigScreenFactories, modID));
        }

        return screens;
    }
}
