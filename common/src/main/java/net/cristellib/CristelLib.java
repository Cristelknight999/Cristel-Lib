package net.cristellib;

import com.google.common.collect.ImmutableMap;
import net.cristellib.builtinpacks.BuiltInDataPacks;
import net.cristellib.builtinpacks.RuntimePack;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class CristelLib {
    public static final String MOD_ID = "cristellib";

    public static final Logger LOGGER = LogManager.getLogger("Cristel Lib");

    public static RuntimePack DATA_PACK = new RuntimePack("Runtime Pack", 10, CristelLibExpectPlatform.getResourceDirectory(MOD_ID, "assets/cristellib/textures/icon.png"));

    private static final CristelLibRegistry REGISTRY = new CristelLibRegistry();

    public static void init() {
    }

    public static void preInit(){
        BuiltInDataPacks.registerPack(DATA_PACK, Component.literal("Cristel Lib Config Pack"), () -> true);

        CristelLibRegistry.configs = ImmutableMap.copyOf(CristelLibExpectPlatform.getConfigs(REGISTRY));

        for(Set<StructureConfig> pack : CristelLibRegistry.getConfigs().values()){
            for(StructureConfig config : pack){
                config.writeConfig();
                config.addSetsToRuntimePack();
            }
        }
    }
}
