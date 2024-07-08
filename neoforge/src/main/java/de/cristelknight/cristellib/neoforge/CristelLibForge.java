package de.cristelknight.cristellib.neoforge;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.neoforge.extrapackutil.RepositorySourceMaker;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(CristelLib.MOD_ID)
public class CristelLibForge {
    public CristelLibForge(IEventBus bus) {
        CristelLib.preInit();
        CristelLib.init();
        bus.addListener(this::injectPackRepositories);
    }

    private void injectPackRepositories(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(new RepositorySourceMaker());
        }
    }

}
