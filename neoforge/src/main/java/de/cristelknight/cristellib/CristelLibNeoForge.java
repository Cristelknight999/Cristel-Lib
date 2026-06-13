package de.cristelknight.cristellib;


import de.cristelknight.cristellib.builtinpacks.BuiltInPackLoader;
import de.cristelknight.cristellib.client.CristelLibNeoForgeClient;
import de.cristelknight.cristellib.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(Constants.MOD_ID)
public class CristelLibNeoForge {

    public CristelLibNeoForge(IEventBus eventBus) {
        CristelLib.preInit();
        CristelLib.init();
        eventBus.addListener(this::injectPackRepositories);

        if (FMLEnvironment.getDist().isClient() && Util.isClothConfigLoaded()) {
            CristelLibNeoForgeClient.registerMainConfigScreen();
        }

    }

    private void injectPackRepositories(AddPackFindersEvent event) {
        // Register one source per pack to avoid NeoForge's per-source alphabetical sort
        BuiltInPackLoader.registerEachPackAsSource(event.getPackType(), event::addRepositorySource);
    }
}