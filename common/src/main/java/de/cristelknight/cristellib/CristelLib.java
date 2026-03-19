package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPackLoader;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackConfig;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import de.cristelknight.cristellib.config.simple.datafixer.DataFixer;
import de.cristelknight.cristellib.data.condition.ConditionRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CristelLib {
    public static final String MOD_ID = "cristellib";
    public static final String MC_ID = Identifier.DEFAULT_NAMESPACE;

    public static String getWithPrefix(String message){
        return String.format("[%s] %s", MOD_ID, message);
    }

    public static final Logger LOGGER = LogManager.getLogger("Cristel Lib");

    public static final Identifier CRISTEL_LIB_PACK_RL = Identifier.fromNamespaceAndPath(MOD_ID, "runtime_pack");

    public static final RuntimePack RUNTIME_PACK = new RuntimePack(CRISTEL_LIB_PACK_RL, SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major(), "Runtime Pack for built-in features", CristelLibExpectPlatform.getResourceStream(MOD_ID, "pack.png"));

    private static final CristelLibRegistry REGISTRY = new CristelLibRegistry();

    public static void init() {
    }


    public static void preInit(){
        DataFixer.registerFixer();
        ConditionRegistry.init();
        CristelLibRegistry.configs = ImmutableMap.copyOf(CristelLibExpectPlatform.getConfigs(REGISTRY));
        BuiltInDataPackLoader.freeze();
        BuiltInPackConfig.updateConfig();

        for(Set<StructureConfig> pack : CristelLibRegistry.getConfigs().values()){
            for(StructureConfig structureConfig : pack){
                structureConfig.writeConfig(false);
                structureConfig.addSetsToRuntimePack();
            }
        }
        
    }

    public static void readAPI(CristelLibRegistry registry, String modId, CristelLibAPI api, Map<String, Set<StructureConfig>> configs) {
        try {
            api.registerBuiltInPacks();
            Set<StructureConfig> set = new HashSet<>();
            api.registerConfigs(set);
            configs.put(modId, set);
            api.registerStructureSets(registry);
            set.forEach(StructureConfig::getDefaultNamespace);
        } catch (Throwable e) {
            CristelLib.LOGGER.error("Mod: {} provides a broken implementation of CristelLibAPI", modId, e);
        }
    }
}