package de.cristelknight.cristellib.util;

import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.autoconfig.ACInfoData;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.data.ReadData;
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

    public static Map<String, Set<StructureConfig>> readData(){
        Map<String, Set<StructureConfig>> modidAndConfigs = new HashMap<>();
        Map<String, ACInfoData> autoConfigInfoData = new HashMap<>();
        updateOldFiles();
        for(String modID : CristelLibExpectPlatform.getModIds()) {
            ReadData.getBuiltInPacks(modID);
            ReadData.copyFile(modID);
            //ReadData.modifyJson5File(modid);
            ReadData.getStructureConfigs(modID, modidAndConfigs);
            ReadData.getAutoConfigSettings(modID, autoConfigInfoData);
        }

        ACInfoData.currentData = autoConfigInfoData;
        ACConfig.updateConfig();
        return modidAndConfigs;
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

}
