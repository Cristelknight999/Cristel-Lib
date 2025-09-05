package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.StructureConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@Environment(EnvType.CLIENT)
public record ClientStructureConfig(StructureConfig structureConfig, Map<ResourceLocation, ClientPlacementConfig> clientPlacementConfigs, Map<ResourceLocation, ClientEDConfig> clientEDConfigs) {

}