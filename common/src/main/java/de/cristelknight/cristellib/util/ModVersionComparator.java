package de.cristelknight.cristellib.util;

import de.cristelknight.cristellib.ModLoadingUtil;

public enum ModVersionComparator {
    // order is important to match the longest substring (e.g. try >= before >)
    GREATER_EQUAL(">=") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compare(modId, version)
                    .map(compareResult -> compareResult >= 0)
                    .orElse(false);
        }
    },
    LESS_EQUAL("<=") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compare(modId, version)
                    .map(compareResult -> compareResult <= 0)
                    .orElse(false);
        }
    },
    GREATER(">") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compare(modId, version)
                    .map(compareResult -> compareResult > 0)
                    .orElse(false);  // If Optional is empty, return false
        }
    },
    LESS("<") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compare(modId, version)
                    .map(compareResult -> compareResult < 0)
                    .orElse(false);
        }
    },
    EQUAL("=") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compare(modId, version)
                    .map(compareResult -> compareResult == 0)
                    .orElse(false);
        }
    }/*,
    SAME_TO_NEXT_MINOR("~") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compareMinor(modId, version);
        }
    },
    SAME_TO_NEXT_MAJOR("^") {
        public boolean test(String modId, String version) {
            return ModLoadingUtil.compareMajor(modId, version);
        }
    }*/;

    private final String serialized;

    ModVersionComparator(String serialized) {
        this.serialized = serialized;
    }

    public final String getSerialized() {
        return serialized;
    }

    public abstract boolean test(String modId, String version);
}