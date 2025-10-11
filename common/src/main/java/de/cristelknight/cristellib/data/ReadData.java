package de.cristelknight.cristellib.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPackLoader;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.data.codec.BuiltInPackData;
import de.cristelknight.cristellib.data.codec.BuiltInPackDataWrapper;
import de.cristelknight.cristellib.data.codec.CopyFileData;
import de.cristelknight.cristellib.util.Util;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class ReadData {

    public static void getAutoConfigSettings(String modId, Map<String, ACInfoData> data) {
        for (Path path : PathFinder.getPathsInDir(modId, "auto_config")) {
            ACInfoData acInfoData = ConfigManager.readFromJsonPath(String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, ACInfoData.CODEC);

            if (data.containsKey(modId)) {
                CristelLib.LOGGER.warn("Overriding Auto Config data for modID: {} from path: {}", modId, path);
            }
            data.put(modId, acInfoData);
        }
    }

    public static void getStructureConfigs(String modId, Map<String, Set<StructureConfig>> modIdAndConfigs) {
        for (Path path : PathFinder.getPathsInDir(modId, "structure_config")) {
            // Read the structure config from disk
            StructureConfig config = ConfigManager.readFromJsonPath(String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, StructureConfig.CODEC);

            // Special-case for replacement configs:
            // We use 'minecraft' as a placeholder modId to indicate that this config
            // should replace an existing config from another mod.
            // checkForReplace ensures that we only replace the correct config in the
            // original mod's set. If a replacement was applied, we skip adding it as a new config.
            if (modId.equals("minecraft") && checkForReplace(modIdAndConfigs, path, config))
                continue;

            modIdAndConfigs.computeIfAbsent(modId, k -> new HashSet<>()).add(config);
        }
    }

    private static boolean checkForReplace(Map<String, Set<StructureConfig>> modIdAndConfigs, Path path, StructureConfig config) {
        try {
            // Use '@' as separator in filenames
            // e.g., t_and_t@t_and_t_ED.json → t_and_t:t_and_t_ED

            Pair<String, String> pair = Util.parseNamespaceAndPath(Util.fileName(path), '@');
            String namespace = pair.getFirst();

            Set<StructureConfig> configs = modIdAndConfigs.computeIfAbsent(namespace, k -> new HashSet<>());

            for (Iterator<StructureConfig> it = configs.iterator(); it.hasNext(); ) {
                StructureConfig old = it.next();
                if (!Util.fileName(old.getPath()).equals(pair.getSecond())) continue;

                it.remove();
                configs.add(config);
                return true; // replaced successfully
            }

            configs.add(config); // namespace exists but no match; just add
            return true;
        } catch (ResourceLocationException ignored) {
            return false; // fallback: can't parse namespace, add to "minecraft"
        }
    }

    public static void getBuiltInPacks(String modId) {
        for (Path path : PathFinder.getPathsInDir(modId, "data_pack")) {

            Either<BuiltInPackData, BuiltInPackDataWrapper> either = ConfigManager.readFromJsonPath(
                    String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, BuiltInPackData.PACKS_CODEC);

            either.left().ifPresent(ReadData::loadPack);
            either.right().ifPresent(wrapper -> {
                List<BuiltInPackData> packs = new ArrayList<>(wrapper.packs());
                Collections.reverse(packs);
                packs.forEach(ReadData::loadPack);
            });
        }
    }

    public static void loadPack(BuiltInPackData pack) {
        boolean bl = Conditions.readConditions(pack.conditions());
        BuiltInDataPackLoader.registerPack(pack.location(), Component.nullToEmpty(pack.displayName()), () -> bl);
    }


    public static void copyFile(String modId) {
        for (Path path : PathFinder.getPathsInDir(modId, "copy_file")) {

            CopyFileData copyFileData = ConfigManager.readFromJsonPath(String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, CopyFileData.CODEC);

            if (Conditions.readConditions(copyFileData.conditions())) {
                copyFileFromJar(copyFileData.location(), copyFileData.destination());
            }
        }
    }

    public static void copyFileFromJar(ResourceLocation from, String to) {
        String modID = from.getNamespace();
        String location = from.getPath();

        List<Path> inputUrl = CristelLibExpectPlatform.getRootPaths(modID);
        for (Path p : inputUrl) {
            Path fromFile = p.resolve(location);
            File toFile = Util.pathFromString(to).toFile();
            if (fromFile == null || toFile == null || toFile.exists()) continue;
            try {
                FileUtils.copyURLToFile(fromFile.toUri().toURL(), toFile);
            } catch (IOException e) {
                CristelLib.LOGGER.error("Couldn't copy file from: {} to: {}", fromFile, toFile, e);
            }
        }
    }


}
