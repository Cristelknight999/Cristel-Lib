package de.cristelknight.cristellib;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.config.structure.ReadStructureSets;
import de.cristelknight.cristellib.config.structure.ed.ToggleConfig;
import de.cristelknight.cristellib.data.codec.StructureSetData;
import net.minecraft.resources.Identifier;

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

    StructureConfigToggle(Path path, String header, Map<String, String> comments, List<StructureSetData> structureSetHolders) {
        super(path, header, comments, structureSetHolders);
        this.structuresForED = Suppliers.memoize(() -> ReadStructureSets.readSetsAndAddStructures(structureSetHolders));
    }

    @Override
    public boolean add(String modId, Identifier setLocation) {
        ToggleConfig setConfig = enableDisableConfig.get(setLocation);
        if(setConfig.hasDisabledStructure())
            return false;

        JsonElement structureSetElement = getStructureSet(setLocation, modId);
        if (!(structureSetElement instanceof JsonObject structureSet)) {
            Constants.LOG.warn("Set for {} {} is not a JsonObject, skipping...", modId, setLocation);
            return true;
        }

        removeStructureInSets(structureSet, setConfig, setLocation);

        return true;
    }

    @Override
    public void readConfig(boolean override) {
        if (enableDisableConfig == null || override)
            enableDisableConfig = ConfigManager.readToggleConfig(this);
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



    public Map<Identifier, List<Identifier>> getDefaultStructures() {
        return structuresForED.get();
    }

    public Map<Identifier, ToggleConfig> getEnableDisableConfig() {
        return enableDisableConfig;
    }

    public void updateEDConfig(Identifier key, ToggleConfig config) {
        this.enableDisableConfig.put(key, config);
    }
}