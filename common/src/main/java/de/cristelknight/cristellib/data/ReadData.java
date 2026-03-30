package de.cristelknight.cristellib.data;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackLoader;
import de.cristelknight.cristellib.config.FileWriter;
import de.cristelknight.cristellib.data.codec.BuiltInPackData;
import de.cristelknight.cristellib.data.codec.BuiltInPackDataWrapper;
import de.cristelknight.cristellib.data.condition.ConditionNode;
import de.cristelknight.cristellib.util.FileHelper;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class ReadData {

    public static void readData(
            String modId,
            Map<String, ACInfoData> autoConfigInfoData,
            Map<String, Set<StructureConfig>> structureConfigData,
            Map<String, Set<String>> modIdAndSets
    ) {
        PathFinder.PathFinderData finder = PathFinder.getSubPathsInMod(modId, structureConfigData.keySet());

        // order matters
        getAutoConfigSettings(modId, finder.autoConfig(), autoConfigInfoData);
        getStructureConfigs(modId, finder.structureConfig(), structureConfigData);
        getBuiltInPacks(modId, finder.dataPack());

        //
        modIdAndSets.put(modId, finder.structureSets()); // add found structure sets for auto config creation
    }

    private static void getAutoConfigSettings(String modId, Set<String> subPaths, Map<String, ACInfoData> data) {
        for (String subPath : subPaths) {
            ACInfoData acInfoData = FileWriter.readFromModContainer(modId, subPath, ACInfoData.CODEC, String.format("Couldn't read %s, crashing instead. This file is corrupted!", subPath));

            if (data.containsKey(modId)) {
                Constants.LOG.warn("Overriding Auto Config data for modId: {} from path: {}", modId, subPath);
            }
            data.put(modId, acInfoData);
        }
    }

    private static void getStructureConfigs(String modId, Set<String> subPaths, Map<String, Set<StructureConfig>> modIdAndConfigs) {
        for (String subPath : subPaths) {
            // Read the structure config from disk
            StructureConfig config;

            // Special-case for replacement configs:
            // We use 'minecraft' as a placeholder modId to indicate that this config
            // could replace an existing config from another mod.
            // checkForReplace ensures that we only replace the correct config in the
            // original mod's set. If a replacement was applied, we skip adding it as a new config.
            if (modId.equals(Constants.MC_ID)) {
                Path fullPath = Path.of(subPath);
                config = FileWriter.readFromJanksonPath(fullPath, StructureConfig.CODEC);
                 if(checkForReplace(modIdAndConfigs, fullPath, config))
                     continue;
            }
            else
                config = FileWriter.readFromModContainer(
                        modId,
                        subPath,
                        StructureConfig.CODEC,
                        String.format("Couldn't read %s, crashing instead. This file is corrupted!", subPath)
                );

            modIdAndConfigs.computeIfAbsent(modId, k -> new HashSet<>()).add(config);
        }
    }

    private static boolean checkForReplace(Map<String, Set<StructureConfig>> modIdAndConfigs, Path path, StructureConfig config) {
        try {
            // Use '@' as separator in filenames
            // e.g., t_and_t@t_and_t_ED.json → t_and_t:t_and_t_ED

            Pair<String, String> pair = FileHelper.parseNamespaceAndPath(FileHelper.fileName(path), '@');
            String modId = pair.getFirst();

            Set<StructureConfig> configs = modIdAndConfigs.computeIfAbsent(modId, k -> new HashSet<>());

            Iterator<StructureConfig> it = configs.iterator();
            while (it.hasNext()) {
                StructureConfig old = it.next();
                if (!FileHelper.fileName(old.getPath()).equals(pair.getSecond()))
                    continue;

                it.remove();
                configs.add(config);
                return true; // replaced successfully
            }

            configs.add(config); // modId exists but no match; just add
            return true;
        } catch (IdentifierException ignored) {
            return false; // fallback: can't parse namespace, add to "minecraft"
        }
    }

    private static void getBuiltInPacks(String modId, Set<String> subPaths) {
        for (String subPath : subPaths) {

            Either<BuiltInPackData, BuiltInPackDataWrapper> either;
            if (modId.equals(Constants.MC_ID))
                either = FileWriter.readFromJanksonPath(Path.of(subPath), BuiltInPackData.PACKS_CODEC);
            else
                either = FileWriter.readFromModContainer(
                        modId,
                        subPath,
                        BuiltInPackData.PACKS_CODEC,
                        String.format("Couldn't read %s, crashing instead. This file is corrupted!", subPath)
                );

            either.ifLeft(ReadData::loadPack);
            either.ifRight(wrapper -> {
                List<BuiltInPackData> packs = new ArrayList<>(wrapper.packs());
                Collections.reverse(packs);
                packs.forEach(ReadData::loadPack);
            });
        }
    }

    private static void loadPack(BuiltInPackData pack) {
        Supplier<Boolean> bl = () -> ConditionNode.testConditionNode(pack.conditionNode());
        BuiltInPackLoader.registerPack(pack.location(), Component.nullToEmpty(pack.displayName()), bl);
    }

}
