package de.cristelknight.cristellib.util;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.ConfigManager;
import net.minecraft.ResourceLocationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHelper {

    public static Path janksonPathFromString(String path, String name) {
        return pathFromString(path).resolve(name + ".json5");
    }

    public static Path pathFromString(String path) {
        return path.startsWith("<CONFIG_DIR>/") ? ConfigManager.CONFIG_DIR.resolve(path.replace("<CONFIG_DIR>/", "")) : Path.of(path);
    }

    public static String fileName(Path path) {
        if (path == null) {
            throw new IllegalArgumentException(Constants.getWithPrefix("Path to get filename from is null!"));
        }

        Path file = path.getFileName();
        if (file == null) {
            throw new IllegalArgumentException(Constants.getWithPrefix("Path cannot have zero elements!"));
        }

        return cutFileType(file);
    }

    public static String cutFileType(Path path) {
        if (path == null) {
            throw new IllegalArgumentException(Constants.getWithPrefix("Path to cut filename from is null!"));
        }

        String fileName = path.toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * Normalizes a potential Minecraft resource path fragment to forward slashes.
     * <p>
     * Inputs:
     * - path: A platform-dependent path fragment (e.g., produced from java.nio.file.Path)
     * <p>
     * Behavior:
     * - Replaces all '\\' with '/'
     * - Removes a single leading '/' if present
     * - Collapses duplicate '/'
     * <p>
     * Output:
     * - A normalized path string safe to pass to ResourceLocation.fromNamespaceAndPath
     */
    public static String normalizeResourcePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException(Constants.getWithPrefix("Path to normalize is null!"));
        }

        String normalized = path.replace('\\', '/');

        if (!normalized.isEmpty() && normalized.charAt(0) == '/') {
            normalized = normalized.substring(1);
        }

        // Collapse any accidental duplicate slashes
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }

        return normalized;
    }

    public static void renameFile(Path path, String newBaseName) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        String ext = (dotIndex == -1) ? "" : filename.substring(dotIndex);
        Path newPath = path.resolveSibling(newBaseName + ext);
        try {
            Files.move(path, newPath);
        } catch (IOException e) {
            Constants.LOG.error("Failed to rename file at path: {}, to: {}", path, newBaseName, e);
        }
    }


    /**
     * Parses a filename into namespace and path parts using a custom separator.
     *
     * @param fileName  the filename to parse
     * @param separator the character used to separate namespace and path
     * @return a Pair where left = namespace, right = path
     * @throws IllegalArgumentException if the filename is invalid or separator is missing
     */
    public static Pair<String, String> parseNamespaceAndPath(String fileName, char separator) throws IllegalArgumentException {
        int sepIndex = fileName.indexOf(separator);
        if (sepIndex < 1 || sepIndex == fileName.length() - 1) {
            throw new ResourceLocationException("Invalid file name: " + fileName + ", missing or misplaced separator '" + separator + "'");
        }

        String namespace = fileName.substring(0, sepIndex);
        String path = fileName.substring(sepIndex + 1);
        return new Pair<>(namespace, path);
    }

}