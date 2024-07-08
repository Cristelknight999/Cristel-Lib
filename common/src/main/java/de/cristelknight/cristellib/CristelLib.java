package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPacks;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class CristelLib {
    public static final String MOD_ID = "cristellib";

    public static final Logger LOGGER = LogManager.getLogger("Cristel Lib");

    public static final RuntimePack DATA_PACK = new RuntimePack("Runtime Pack", SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA), CristelLibExpectPlatform.getResourceDirectory(MOD_ID, "pack.png"));

    public static final String minTerraBlenderVersion = "3.3.0.12";
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
