package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

public class CristelLibRegistry {

    protected CristelLibRegistry() {
    }

    protected static ImmutableMap<String, Set<StructureConfig>> configs = ImmutableMap.of();

    public static ImmutableMap<String, Set<StructureConfig>> getConfigs() {
        if (!configs.isEmpty()) return configs;
        else throw new RuntimeException(Constants.getWithPrefix("Tried to access Registry before initialized."));
    }

    public void registerSetToConfig(String modId, String namespace, List<String> sets, StructureConfig... configs) {
        boolean isMC = namespace == null || namespace.equals(Constants.MC_ID);

        registerSetToConfig(modId, sets.stream()
                .map(string -> isMC
                        ? Identifier.withDefaultNamespace(string)
                        : Identifier.fromNamespaceAndPath(namespace, string)
                )
                .toList(), configs);
    }

    public void registerSetToConfig(String modId, List<Identifier> sets, StructureConfig... configs) {
        for (StructureConfig config : configs) config.addSet(new StructureSetData(modId, sets));
    }

    public void registerSetToConfig(String modId, Identifier set, StructureConfig... configs) {
        for (StructureConfig config : configs) config.addSet(new StructureSetData(modId, List.of(set)));
    }

}