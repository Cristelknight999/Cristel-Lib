package net.cristellib.api;

import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;

import java.util.Set;

public interface CristelLibAPI {

    default void registerConfigs(Set<StructureConfig> sets){
    }

    default void registerStructureSets(CristelLibRegistry registry){
    }

}
