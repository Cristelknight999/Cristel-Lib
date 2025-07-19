package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.StructureConfig;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;

import java.util.Map;

public class ClientStructureConfig {

    private final StructureConfig structureConfig;

    private final Map<String, ClientPlacementConfig> clientPlacementConfigs;

    private final Map<String, BooleanListEntry> clientEDConfigs;

    public ClientStructureConfig(StructureConfig structureConfig, Map<String, ClientPlacementConfig> clientPlacementConfigs, Map<String, BooleanListEntry> clientEDConfigs) {
        this.structureConfig = structureConfig;
        this.clientPlacementConfigs = clientPlacementConfigs;
        this.clientEDConfigs = clientEDConfigs;
    }

    public StructureConfig getStructureConfig() {
        return structureConfig;
    }

    public Map<String, ClientPlacementConfig> getPlacementConfigs() {
        return clientPlacementConfigs;
    }

    public Map<String, BooleanListEntry> getEDConfigs() {
        return clientEDConfigs;
    }
}
