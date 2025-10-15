package de.cristelknight.cristellib.neoforge;

import de.cristelknight.cristellib.CristelLib;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;
import java.util.Optional;

public class ModLoadingUtilImpl {

    public static boolean isModLoaded(String modID) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modID);
        }
        return isModPreLoaded(modID);
    }

    public static boolean isModPreLoaded(String modID) {
        return getPreLoadedModInfo(modID) != null;
    }

    public static @Nullable ModInfo getPreLoadedModInfo(String modID) {
        for (ModInfo info : FMLLoader.getCurrent().getLoadingModList().getMods()) {
            if (info.getModId().equals(modID)) {
                return info;
            }
        }
        return null;
    }


    public static Optional<Integer> compare(String modID, String version) {
        if (isModLoaded(modID)) {
            ModList modList = ModList.get();
            ArtifactVersion modVersion;
            if (modList != null) modVersion = modList.getModContainerById(modID).get().getModInfo().getVersion();
            else modVersion = getPreLoadedModVersion(modID);

            ArtifactVersion min;
            min = new DefaultArtifactVersion(version);
            return Optional.of(modVersion.compareTo(min));
        }
        return Optional.empty();
    }


    public static ArtifactVersion getPreLoadedModVersion(String modID) {
        ModInfo info = getPreLoadedModInfo(modID);
        if (info == null) throw new RuntimeException(CristelLib.getWithPrefix("Couldn't find mod: " + modID));
        return info.getVersion();
    }
}
