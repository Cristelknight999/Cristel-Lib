package de.cristelknight.cristellib.config.client.structure;

import de.cristelknight.cristellib.StructureConfig;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record ClientStructureConfig(
        StructureConfig structureConfig,
        Map<Identifier, ClientPlacementConfig> clientPlacementConfigs,
        Map<Identifier, ClientEDConfig> clientEDConfigs
) {}