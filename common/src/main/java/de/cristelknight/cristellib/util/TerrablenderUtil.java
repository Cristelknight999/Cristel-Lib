package de.cristelknight.cristellib.util;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;

public class TerrablenderUtil {

    private static boolean enableMixin = false;

    public static boolean mixinEnabled(){
        return enableMixin;
    }

    public static void setMixinEnabled(boolean bl){
        enableMixin = bl;
    }

    public static boolean isModLoaded(){
        return ModLoadingUtil.isModLoadedWithVersion("terrablender", CristelLib.minTerraBlenderVersion);
    }
}
