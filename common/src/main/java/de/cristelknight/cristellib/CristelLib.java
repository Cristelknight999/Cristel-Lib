package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPackLoader;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackConfig;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import de.cristelknight.cristellib.config.simple.datafixer.DataFixer;
import de.cristelknight.cristellib.data.condition.ConditionRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CristelLib {

    public static final RuntimePack RUNTIME_PACK = new RuntimePack(
            Constants.CRISTEL_LIB_PACK_ID,
            SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major(),
            "Runtime Pack for built-in features",
            CristelLibExpectPlatform.getResourceStream(Constants.MOD_ID, "pack.png")
    );

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
            Constants.LOGGER.error("Mod: {} provides a broken implementation of CristelLibAPI", modId, e);
        }
    }
}