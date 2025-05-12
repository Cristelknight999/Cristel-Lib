package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public class CristelLibRegistry {

    protected CristelLibRegistry() {
    }

    protected static ImmutableMap<String, Set<StructureConfig>> configs = ImmutableMap.of();

    public static ImmutableMap<String, Set<StructureConfig>> getConfigs() {
        return configs;
    }

    public void registerSetToConfig(String modID, String namespace, List<String> sets, StructureConfig... configs) {
        boolean isMC = namespace == null || namespace.equals("minecraft");

        registerSetToConfig(modID, sets.stream()
                .map(string -> isMC
                        ? ResourceLocation.withDefaultNamespace(string)
                        : ResourceLocation.fromNamespaceAndPath(namespace, string)
                )
                .toList(), configs);
    }

    public void registerSetToConfig(String modID, List<ResourceLocation> sets, StructureConfig... configs) {
        for (StructureConfig config : configs) config.addSet(new StructureSetData(modID, sets));
    }

    public void registerSetToConfig(String modID, ResourceLocation set, StructureConfig... configs) {
        for (StructureConfig config : configs) config.addSet(new StructureSetData(modID, List.of(set)));
    }

}
