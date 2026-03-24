package de.cristelknight.cristellib.config.client.structure;

import de.cristelknight.cristellib.StructureConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

import java.util.Map;

@Environment(EnvType.CLIENT)
public record ClientStructureConfig(
        StructureConfig structureConfig,
        Map<Identifier, ClientPlacementConfig> clientPlacementConfigs,
        Map<Identifier, ClientEDConfig> clientEDConfigs
) {}