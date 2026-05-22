package de.cristelknight.cristellib.neoforge;


import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackLoader;
import de.cristelknight.cristellib.neoforge.client.CristelLibNeoForgeClient;
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

        if (FMLEnvironment.dist.isClient() && Util.isClothConfigLoaded()) {
            CristelLibNeoForgeClient.registerMainConfigScreen();
        }

    }

    private void injectPackRepositories(AddPackFindersEvent event) {
        // Register one source per pack to avoid NeoForge's per-source alphabetical sort
        // (PackRepository.discoverAvailable puts each source's packs into a TreeMap).
        BuiltInPackLoader.registerEachPackAsSource(event.getPackType(), event::addRepositorySource);
    }
}