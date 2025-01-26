package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPackLoader;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import de.cristelknight.cristellib.data.BuiltInPackConfig;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

public class CristelLib {
    public static final String MOD_ID = "cristellib";

    public static String getWithPrefix(String message){
        return String.format("[%s] %s", CristelLib.MOD_ID, message);
    }

    public static final Logger LOGGER = LogManager.getLogger("Cristel Lib");

    public static final RuntimePack DATA_PACK = new RuntimePack(ResourceLocation.fromNamespaceAndPath(CristelLib.MOD_ID, "runtime_pack"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA), "Runtime Pack for built-in features", CristelLibExpectPlatform.getResourceDirectory(MOD_ID, "pack.png"));

    public static final String minTerraBlenderVersion = "3.3.0.12";
    private static final CristelLibRegistry REGISTRY = new CristelLibRegistry();

    public static void init() {
    }


    public static void preInit(){
        BuiltInDataPackLoader.registerPack(DATA_PACK, Component.literal("Cristel Lib Config Pack"), () -> true);
        CristelLibRegistry.configs = ImmutableMap.copyOf(CristelLibExpectPlatform.getConfigs(REGISTRY));
        BuiltInDataPackLoader.freeze();
        BuiltInPackConfig.updateConfig();

        for(Set<StructureConfig> pack : CristelLibRegistry.getConfigs().values()){
            for(StructureConfig structureConfig : pack){
                structureConfig.writeConfig();
                structureConfig.addSetsToRuntimePack();
            }
        }
    }
}
