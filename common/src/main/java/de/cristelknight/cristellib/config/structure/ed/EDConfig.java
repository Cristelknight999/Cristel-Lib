package de.cristelknight.cristellib.config.structure.ed;

import java.util.Map;

public record EDConfig(Map<String, Boolean> setStructureInfo) {

    public boolean containsStructure(String structureName) {
        return setStructureInfo.containsKey(structureName);
    }

    public boolean isStructureDisabled(String structureName) {
        return !setStructureInfo.get(structureName);
    }

}