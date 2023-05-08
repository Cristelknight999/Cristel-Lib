package net.cristellib.fabriclike;

import net.cristellib.CristelLib;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class CristelLibPreInit implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        CristelLib.preInit();
    }
}
