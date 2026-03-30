package de.cristelknight.cristellib;


import de.cristelknight.cristellib.client.CristelLibNeoForgeClient;
import de.cristelknight.cristellib.extrapackutil.RepositorySourceMaker;
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
        event.addRepositorySource(new RepositorySourceMaker(event.getPackType()));
    }
}