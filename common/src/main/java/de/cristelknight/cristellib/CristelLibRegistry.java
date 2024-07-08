package de.cristelknight.cristellib;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class CristelLibRegistry {

    protected CristelLibRegistry(){}

    protected static ImmutableMap<String, Set<StructureConfig>> configs = ImmutableMap.of();

    public static ImmutableMap<String, Set<StructureConfig>> getConfigs(){
        return configs;
    }

    public void registerSetToConfig(String modid, ResourceLocation set, StructureConfig... configs){
        for(StructureConfig config : configs) config.addSet(new Pair<>(modid, set));
    }

}
