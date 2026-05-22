package de.cristelknight.cristellib.config.client.structure;

import de.cristelknight.cristellib.StructureConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ClientStructureConfig(
        StructureConfig structureConfig,
        Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs,
        Map<ResourceLocation, ClientEDConfig> clientEDConfigs
) {}