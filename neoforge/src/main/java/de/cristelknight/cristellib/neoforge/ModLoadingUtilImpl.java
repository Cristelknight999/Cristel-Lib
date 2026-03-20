package de.cristelknight.cristellib.neoforge;

import de.cristelknight.cristellib.Constants;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModLoadingUtilImpl {

    public static List<String> getModIds() {
        ModList modList = ModList.get();
        List<String> modIds = new ArrayList<>();
        if (modList != null) {
            for (IModInfo modInfo : modList.getMods()) {
                modIds.add(modInfo.getModId());
            }
        } else {
            for (IModInfo modInfo : FMLLoader.getCurrent().getLoadingModList().getMods()) {
                modIds.add(modInfo.getModId());
            }
        }
        return modIds;
    }

    public static boolean isModLoaded(String modId) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modId);
        }
        return isModPreLoaded(modId);
    }

    public static boolean isModPreLoaded(String modId) {
        return getPreLoadedModInfo(modId) != null;
    }

    public static @Nullable ModInfo getPreLoadedModInfo(String modId) {
        for (ModInfo info : FMLLoader.getCurrent().getLoadingModList().getMods()) {
            if (info.getModId().equals(modId)) {
                return info;
            }
        }
        return null;
    }


    public static Optional<Integer> compare(String modId, String version) {
        if (isModLoaded(modId)) {
            ModList modList = ModList.get();
            ArtifactVersion modVersion;
            if (modList != null) modVersion = modList.getModContainerById(modId).get().getModInfo().getVersion();
            else modVersion = getPreLoadedModVersion(modId);

            ArtifactVersion min;
            min = new DefaultArtifactVersion(version);
            return Optional.of(modVersion.compareTo(min));
        }
        return Optional.empty();
    }


    public static ArtifactVersion getPreLoadedModVersion(String modId) {
        ModInfo info = getPreLoadedModInfo(modId);
        if (info == null) throw new RuntimeException(Constants.getWithPrefix("Couldn't find mod: " + modId));
        return info.getVersion();
    }
}
