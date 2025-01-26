package de.cristelknight.cristellib.util;

import de.cristelknight.cristellib.config.ConfigManager;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class Util {

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

        String fileName = file.toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }

    public static <V, S> void addAll(Map<V, Set<S>> addTo, Map<V, Set<S>> addFrom){
        for(V string : addFrom.keySet()){
            if(addTo.containsKey(string)){
                Set<S> s = addTo.get(string);
                s.addAll(addFrom.get(string));
                addTo.put(string, s);
            }
            else {
                addTo.put(string, addFrom.get(string));
            }
        }
    }

}
