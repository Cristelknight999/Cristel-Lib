package de.cristelknight.cristellib.neoforge.client;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Optional;

public class NeoForgeClient {

    public static void registerMainConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (mc, screenFactory) ->
                new ScreenBuilder().create(screenFactory, CristelLib.MOD_ID, true, true));
    }

    public static void addOtherConfigScreens() {
        ACConfig acConfig = ConfigRegistry.get(ACConfig.class);
        boolean structureMain = !acConfig.disableAutoConfig() && !acConfig.disableAutoConfigScreens();

        for(String modID : ScreenBuilder.allConfigMods(structureMain)){
            Pair<Boolean, Boolean> structureSimple = ScreenBuilder.shouldCreateScreen(modID, structureMain);
            boolean structure = structureSimple.getFirst();
            boolean simple = structureSimple.getSecond();
            if(!structure && !simple) continue;

            Optional<? extends ModContainer> container = ModList.get().getModContainerById(modID);
            if(container.isEmpty() || container.get().getCustomExtension(IConfigScreenFactory.class).isPresent()) continue;

            container.get().registerExtensionPoint(IConfigScreenFactory.class, (mc, screenFactory) ->
                    new ScreenBuilder().create(screenFactory, modID, structure, simple));
        }
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(NeoForgeClient::addOtherConfigScreens);
    }

}