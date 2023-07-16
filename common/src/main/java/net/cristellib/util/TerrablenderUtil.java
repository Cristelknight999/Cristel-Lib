package net.cristellib.util;

public class TerrablenderUtil {

    private static boolean enableMixin = false;

    public static boolean mixinEnabled(){
        return enableMixin;
    }

    public static void setMixinEnabled(boolean bl){
        enableMixin = bl;
    }

}
