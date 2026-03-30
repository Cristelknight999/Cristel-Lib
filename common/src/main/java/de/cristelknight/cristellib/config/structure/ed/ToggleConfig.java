package de.cristelknight.cristellib.config.structure.ed;

import java.util.Map;

public record ToggleConfig(Map<String, Boolean> setStructureInfo) {

    public ToggleConfig(NestedEDConfig nestedEDConfig) {
        this(ToggleConfigTransformer.stringBooleanMap(nestedEDConfig, ""));
    }

    public boolean containsStructure(String structureName) {
        return setStructureInfo.containsKey(structureName);
    }

    public boolean isStructureDisabled(String structureName) {
        return !setStructureInfo.get(structureName);
    }

    public boolean hasDisabledStructure() {
        return setStructureInfo.containsValue(false);
    }

}