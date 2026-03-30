package de.cristelknight.cristellib.util;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.autoconfig.ModFinder;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.data.ReadData;
import de.cristelknight.cristellib.platform.Services;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Util {

    public static boolean isClothConfigLoaded() {
        if (Services.PLATFORM.getPlatform().equals(Platform.FABRIC))
            return Services.MOD_LOADING.isModLoaded("cloth-config");
        else
            return Services.MOD_LOADING.isModLoaded("cloth_config");
    }


    public static <V> V getFirst(Collection<V> collection) {
        Iterator<V> it = collection.iterator();
        return it.hasNext() ? it.next() : null;
    }

    public static <T extends Comparable<T>> List<T> sortedKeyList(Map<T, ?> map) {
        return map.keySet().stream().sorted().toList();
    }

    private static final Set<String> SKIP_MODS = Set.of("neoforge", "java", Constants.MOD_ID,
            "modmenu", "cloth-config", "cloth-basic-math"
    );

    // TODO: fix this mess, do excludes properly
    public static void readData(Map<String, Set<StructureConfig>> configs, CristelLibRegistry registry) {
        updateOldFiles();
        Map<String, Set<String>> modIdAndSets = new HashMap<>();
        Map<String, ACInfoData> autoConfigInfoData = new HashMap<>();

        for (String modId : Services.MOD_LOADING.getModIds()) {
            if (SKIP_MODS.contains(modId)) continue;
            ReadData.readData(modId, autoConfigInfoData, configs, modIdAndSets);
        }

        ACInfoData.currentData = autoConfigInfoData;
        ACConfig.update();

        for (String modId : modIdAndSets.keySet()) {
            if (ModFinder.shouldSkipModForACAfter(modId, configs.keySet())) continue;
            ModFinder.addAutoConfigs(modId, modIdAndSets.get(modId), configs, registry);
        }
    }

    private static void updateOldFiles() {
        try {
            Path oldPath = ConfigManager.CONFIG_LIB.resolve("data");

            updateDirectories("structure_config", oldPath);
            updateDirectories("data_pack", oldPath);
            updateDirectories("copy_file", oldPath);

            if (Files.exists(oldPath)) {
                Files.move(oldPath, ConfigManager.CONFIG_LIB.resolve("~OUTDATED DIRECTORY~ data"));
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to update custom configs in <instance>/config/cristellib/data/", e);
        }
    }

    private static void updateDirectories(String subPath, Path oldPath) throws IOException {
        Path oldSubPath = oldPath.resolve(subPath);
        Path oldOldSubPath = oldPath.resolve(subPath + "s");
        if (Files.exists(oldSubPath)) {
            FileUtils.copyDirectory(oldSubPath.toFile(), ConfigManager.CONFIG_LIB.resolve(subPath).toFile());
        } else if (Files.exists(oldOldSubPath)) {
            FileUtils.copyDirectory(oldOldSubPath.toFile(), ConfigManager.CONFIG_LIB.resolve(subPath).toFile());
        }
    }
}