package de.cristelknight.cristellib.client;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
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

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class CristelLibNeoForgeClient {

    public static void registerMainConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (mc, screenFactory) ->
                new ScreenBuilder(Constants.MOD_ID).create(screenFactory));
    }

    public static void addOtherConfigScreens() {
        for (String modId : ScreenBuilder.allModsWithScreen()) {

            Optional<? extends ModContainer> container = ModList.get().getModContainerById(modId);
            if (container.isEmpty() || container.get().getCustomExtension(IConfigScreenFactory.class).isPresent()) continue;

            container.get().registerExtensionPoint(IConfigScreenFactory.class, (mc, screenFactory) ->
                    new ScreenBuilder(modId).create(screenFactory));
        }
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        if (!Util.isClothConfigLoaded()) return;
        event.enqueueWork(CristelLibNeoForgeClient::addOtherConfigScreens);
    }

}