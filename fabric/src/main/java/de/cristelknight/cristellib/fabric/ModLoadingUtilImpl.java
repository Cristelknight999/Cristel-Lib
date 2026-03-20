package de.cristelknight.cristellib.fabric;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.ModLoadingUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.List;
import java.util.Optional;

public class ModLoadingUtilImpl {

    public static List<String> getModIds() {
        return FabricLoader.getInstance().getAllMods().stream().map(mod -> mod.getMetadata().getId()).toList();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static Optional<Integer> compare(String modId, String version) {
        if (ModLoadingUtil.isModLoaded(modId)) {
            Version modVersion = FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getVersion();
            Version min;
            try {
                min = Version.parse(version);
            } catch (VersionParsingException e) {
                Constants.LOGGER.error("Couldn't parse version: {}", version);
                return Optional.empty();
            }
            return Optional.of(modVersion.compareTo(min));
        }
        return Optional.empty();
    }
}
