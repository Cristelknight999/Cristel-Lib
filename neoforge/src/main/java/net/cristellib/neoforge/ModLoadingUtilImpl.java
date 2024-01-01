package net.cristellib.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;

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

    public static boolean isModLoadedWithVersion(String modid, String minVersion) {
        if (isModLoaded(modid)) {
            ModList modList = ModList.get();
            ArtifactVersion version;
            if (modList != null) version = modList.getModContainerById(modid).get().getModInfo().getVersion();
            else version = getPreLoadedModVersion(modid);

            ArtifactVersion min;
            min = new DefaultArtifactVersion(minVersion);
            return version.compareTo(min) >= 0;
        }
        return false;
    }

    public static ArtifactVersion getPreLoadedModVersion(String modid) {
        ModInfo info = getPreLoadedModInfo(modid);
        if (info == null) throw new RuntimeException("Couldn't find mod: " + modid);
        return info.getVersion();
    }
}
