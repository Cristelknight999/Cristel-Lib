package de.cristelknight.cristellib.platform;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.platform.services.IModLoadingUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.List;
import java.util.Optional;

public class FabricModLoadingUtil implements IModLoadingUtil {

    @Override
    public List<String> getModIds() {
        return FabricLoader.getInstance().getAllMods().stream().map(mod -> mod.getMetadata().getId()).toList();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Optional<Integer> compare(String modId, String version) {
        if (isModLoaded(modId)) {
            Version modVersion = FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getVersion();
            Version min;
            try {
                min = Version.parse(version);
            } catch (VersionParsingException e) {
                Constants.LOG.error("Couldn't parse version: {}", version);
                return Optional.empty();
            }
            return Optional.of(modVersion.compareTo(min));
        }
        return Optional.empty();
    }
}
