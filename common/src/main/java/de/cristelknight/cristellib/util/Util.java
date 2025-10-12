package de.cristelknight.cristellib.util;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.autoconfig.ModFinder;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.data.ReadData;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {

    public static boolean isClothConfigLoaded() {
        if(CristelLibExpectPlatform.getPlatform().equals(Platform.FABRIC)) return ModLoadingUtil.isModLoaded("cloth-config");
        else return ModLoadingUtil.isModLoaded("cloth_config");
    }

    public static Path janksonPathFromString(String path, String name){
        return pathFromString(path).resolve(name + ".json5");
    }

    public static Path pathFromString(String path){
        return path.startsWith("<CONFIG_DIR>/") ? ConfigManager.CONFIG_DIR.resolve(path.replace("<CONFIG_DIR>/", "")) : Path.of(path);
    }

    public static String fileName(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        Path file = path.getFileName();
        if (file == null) {
            throw new IllegalArgumentException("Path cannot have zero elements");
        }

        return cutFileType(file);
    }

    public static String cutFileType(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        String fileName = path.toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * Parses a filename into namespace and path parts using a custom separator.
     *
     * @param fileName the filename to parse
     * @param separator the character used to separate namespace and path
     * @return a Pair where left = namespace, right = path
     * @throws IllegalArgumentException if the filename is invalid or separator is missing
     */
    public static Pair<String, String> parseNamespaceAndPath(String fileName, char separator) throws IllegalArgumentException {
        int sepIndex = fileName.indexOf(separator);
        if (sepIndex < 1 || sepIndex == fileName.length() - 1) {
            throw new ResourceLocationException("Invalid file name: " + fileName + ", missing or misplaced separator '" + separator + "'");
        }

        String namespace = fileName.substring(0, sepIndex); // keep case as-is
        String path = fileName.substring(sepIndex + 1);     // keep case as-is
        return new Pair<>(namespace, path);
    }

    public static Component CRISTEL_LIB = Component.literal("Cristel Lib").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.UNDERLINE);

    public static <V, S> void addAll(Map<V, Set<S>> addTo, Map<V, Set<S>> addFrom){
        for(V key : addFrom.keySet()){
            if(addTo.containsKey(key)){
                Set<S> valueSet = addTo.get(key);
                valueSet.addAll(addFrom.get(key));
                addTo.put(key, valueSet);
            }
            else {
                addTo.put(key, addFrom.get(key));
            }
        }
    }

    public static <T extends Comparable<T>> List<T> sortedKeyList(Map<T, ?> map) {
        return map.keySet().stream().sorted().toList();
    }

    public static void readData(Map<String, Set<StructureConfig>> configs, CristelLibRegistry registry){
        updateOldFiles();
        Map<String, Set<String>> modIdAndSets = new HashMap<>();
        Map<String, ACInfoData> autoConfigInfoData = new HashMap<>();

        for(String modID : CristelLibExpectPlatform.getModIds()) {
            Set<String> structureSets = ReadData.readData(modID, autoConfigInfoData, configs);
            modIdAndSets.put(modID, structureSets);
        }

        ACInfoData.currentData = autoConfigInfoData;
        ACConfig.updateConfig();

        for(String modId : modIdAndSets.keySet()) {
            if(isForge() && ModFinder.shouldSkipModForAC(modId, configs.keySet())) continue;
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
            throw new RuntimeException(e);
        }
    }

    private static void updateDirectories(String subPath, Path oldPath) throws IOException {
        Path oldSubPath = oldPath.resolve(subPath);
        Path oldOldSubPath = oldPath.resolve(subPath + "s");
        if (Files.exists(oldSubPath)) {
            FileUtils.copyDirectory(oldSubPath.toFile(), ConfigManager.CONFIG_LIB.resolve(subPath).toFile());
        }
        else if (Files.exists(oldOldSubPath)) {
            FileUtils.copyDirectory(oldOldSubPath.toFile(), ConfigManager.CONFIG_LIB.resolve(subPath).toFile());
        }
    }

    public static boolean isForge() {
        return CristelLibExpectPlatform.getPlatform().equals(Platform.FORGE);
    }

}
