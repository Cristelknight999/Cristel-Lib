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
        if(!configs.isEmpty()) return configs;
        else throw new RuntimeException(CristelLib.getWithPrefix("Tried to access Registry before initialized."));
    }

    public void registerSetToConfig(String modID, String namespace, List<String> sets, StructureConfig... configs) {
        boolean isMC = namespace == null || namespace.equals(CristelLib.MC_ID);

        registerSetToConfig(modID, sets.stream()
                .map(string -> isMC
                        ? Identifier.withDefaultNamespace(string)
                        : Identifier.fromNamespaceAndPath(namespace, string)
                )
                .toList(), configs);
    }

    public void registerSetToConfig(String modID, List<Identifier> sets, StructureConfig... configs) {
        for (StructureConfig config : configs) config.addSet(new StructureSetData(modID, sets));
    }

    public void registerSetToConfig(String modID, Identifier set, StructureConfig... configs) {
        for (StructureConfig config : configs) config.addSet(new StructureSetData(modID, List.of(set)));
    }

}
