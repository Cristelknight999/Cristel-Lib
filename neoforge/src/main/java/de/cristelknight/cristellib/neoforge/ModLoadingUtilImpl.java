package de.cristelknight.cristellib.neoforge;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;
import java.util.Optional;

public class ModLoadingUtilImpl {

    public static boolean isModLoaded(String modid) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modid);
        }
        return isModPreLoaded(modid);
    }

    public static boolean isModPreLoaded(String modid) {
        return getPreLoadedModInfo(modid) != null;
    }

    public static @Nullable ModInfo getPreLoadedModInfo(String modId) {
        for (ModInfo info : LoadingModList.get().getMods()) {
            if (info.getModId().equals(modId)) {
                return info;
            }
        }
        return null;
    }


    public static Optional<Integer> compare(String modid, String version) {
        if (isModLoaded(modid)) {
            ModList modList = ModList.get();
            ArtifactVersion modVersion;
            if (modList != null) modVersion = modList.getModContainerById(modid).get().getModInfo().getVersion();
            else modVersion = getPreLoadedModVersion(modid);

            ArtifactVersion min;
            min = new DefaultArtifactVersion(version);
            return Optional.of(modVersion.compareTo(min));
        }
        return Optional.empty();
    }


    public static ArtifactVersion getPreLoadedModVersion(String modid) {
        ModInfo info = getPreLoadedModInfo(modid);
        if (info == null) throw new RuntimeException("Couldn't find mod: " + modid);
        return info.getVersion();
    }
}
