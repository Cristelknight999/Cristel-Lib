package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackConfig;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackLoader;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import de.cristelknight.cristellib.config.simple.datafixer.DataFixer;
import de.cristelknight.cristellib.data.condition.ConditionRegistry;
import de.cristelknight.cristellib.util.Util;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CristelLib {

    public static final RuntimePack CONFIG_PACK = new RuntimePack(
            Constants.CRISTEL_LIB_PACK_ID,
            SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major(),
            "Runtime Pack for built-in features",
            CristelLibExpectPlatform.getResourceStream(Constants.MOD_ID, "pack.png")
    );

    private static final CristelLibRegistry REGISTRY = new CristelLibRegistry();

    public static void init() {
        Constants.LOG.debug("Loading Cristel Lib (Stage 2)");
    }


    public static void preInit() {
        DataFixer.registerFixer();
        ConditionRegistry.init();
        CristelLibRegistry.configs = ImmutableMap.copyOf(getConfigs());
        BuiltInPackLoader.freeze();
        BuiltInPackConfig.updateConfig();

        StructureConfig.clearModifiedSets();
        for (Set<StructureConfig> pack : CristelLibRegistry.getConfigs().values()) {
            for (StructureConfig structureConfig : pack) {
                structureConfig.writeConfig(false);
                structureConfig.addSetsToRuntimePack();
            }
        }

    }

    private static Map<String, Set<StructureConfig>> getConfigs() {
        Map<String, Set<StructureConfig>> configs = new HashMap<>();

        for (Map.Entry<String, CristelLibAPI> entry : CristelLibExpectPlatform.getApis().entrySet()) {
            String modId = entry.getKey();
            CristelLibAPI api = entry.getValue();
            CristelLib.readAPI(modId, api, configs);
        }
        Util.readData(configs, REGISTRY);
        return configs;
    }

    private static void readAPI(String modId, CristelLibAPI api, Map<String, Set<StructureConfig>> configs) {
        try {
            api.registerBuiltInPacks();
            Set<StructureConfig> set = new HashSet<>();
            api.registerConfigs(set);
            configs.put(modId, set);
            api.registerStructureSets(REGISTRY);
            set.forEach(StructureConfig::getDefaultNamespace);
        } catch (Throwable e) {
            Constants.LOG.error("Mod: {} provides a broken implementation of CristelLibAPI", modId, e);
        }
    }
}