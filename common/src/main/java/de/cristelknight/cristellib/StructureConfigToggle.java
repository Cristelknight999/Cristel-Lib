package de.cristelknight.cristellib;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.structure.ReadStructureSets;
import de.cristelknight.cristellib.config.structure.toggle.ToggleConfig;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import net.minecraft.resources.Identifier;

import de.cristelknight.cristellib.util.FileHelper;

import java.nio.file.Path;
import java.util.*;

public class StructureConfigToggle extends StructureConfig {

    // default values
    private final Supplier<Map<Identifier, List<Identifier>>> structuresForED;

    // current values (structure_set + Config)
    private Map<Identifier, ToggleConfig> enableDisableConfig = null;

    StructureConfigToggle(Path path) {
        this(path, null, new HashMap<>(), new ArrayList<>());
    }

    StructureConfigToggle(String name, String path, String header, Map<String, String> comments, List<StructureSetData> structureSetHolders) {
        this(FileHelper.janksonPathFromString(path, name), header, comments, structureSetHolders);
    }

    StructureConfigToggle(Path path, String header, Map<String, String> comments, List<StructureSetData> structureSetHolders) {
        super(path, header, comments, structureSetHolders);
        this.structuresForED = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSetHolders));
    }

    @Override
    public boolean addChanges(String modId, Identifier setLocation) {
        ToggleConfig setConfig = enableDisableConfig.get(setLocation);
        if(!setConfig.hasDisabledStructure())
            return false;

        JsonElement structureSetElement = getStructureSet(setLocation, modId);
        if (!(structureSetElement instanceof JsonObject structureSet)) {
            Constants.LOG.warn("Set for {} {} is not a JsonObject, skipping...", modId, setLocation);
            return true;
        }

        removeStructureInSets(structureSet, setConfig, setLocation);
        CristelLib.CONFIG_PACK.addStructureSet(setLocation, structureSet);
        return true;
    }

    private void removeStructureInSets(JsonObject structureSet, ToggleConfig setConfig, Identifier setLocation) {
        JsonArray array = structureSet.get("structures").getAsJsonArray();
        Iterator<JsonElement> structureIterator = array.iterator();
        while (structureIterator.hasNext()) {
            JsonElement structure = structureIterator.next();
            String structureName = toDefaultString(Objects.requireNonNull(Identifier.tryParse(structure.getAsJsonObject().get("structure").getAsString())));

            if (!setConfig.containsStructure(structureName)) {
                Constants.LOG.error("{} is not included in: {} for config with path: {}", structureName, setLocation, getPath());
                continue;
            }

            if (setConfig.isStructureDisabled(structureName))
                structureIterator.remove();
        }
    }

    @Override
    public void writeConfig(boolean override) {
        if (!override && getPath().toFile().exists()) return;

        ConfigManager.createToggleConfig(this);
    }

    @Override
    public void readConfig(boolean override) {
        if (enableDisableConfig == null || override)
            enableDisableConfig = ConfigManager.readToggleConfig(this);
    }

    public Map<Identifier, List<Identifier>> getDefaultStructureToggles() {
        return structuresForED.get();
    }

    public Map<Identifier, ToggleConfig> getToggleConfigs() {
        return enableDisableConfig;
    }

    public Set<Identifier> getConfigKeys() {
        return enableDisableConfig.keySet();
    }

    public void updateEDConfig(Identifier key, ToggleConfig config) {
        this.enableDisableConfig.put(key, config);
    }

    @Override
    public void resetConfigs() {
        enableDisableConfig = null;
    }

    @Override
    public ConfigType getType() {
        return ConfigType.TOGGLE;
    }
}