package de.cristelknight.cristellib.api;

import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;

import java.util.Set;

public interface CristelLibAPI {

    default void registerConfigs(Set<StructureConfig> sets){
    }

    default void registerStructureSets(CristelLibRegistry registry){
    }

}
