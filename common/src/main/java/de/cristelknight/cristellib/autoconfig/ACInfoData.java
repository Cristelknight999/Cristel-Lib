package de.cristelknight.cristellib.autoconfig;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.Constants;

import java.util.List;
import java.util.Map;

public record ACInfoData(boolean disableAC, boolean disableACScreen, String autoConfigPath) {

    private static boolean frozen = false;
    private static ImmutableMap<String, ACInfoData> currentData;

    public static ImmutableMap<String, ACInfoData> getCurrentData() {
        return currentData;
    }

    public static void setCurrentData(Map<String, ACInfoData> data) {
        if(frozen) throw new RuntimeException(Constants.getWithPrefix("Cannot set Auto Config data twice! It's already frozen."));
        currentData = ImmutableMap.copyOf(data);
        frozen = true;
    }

    public static final Codec<ACInfoData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("disableAC").forGetter(ACInfoData::disableAC),
                    Codec.BOOL.fieldOf("disableACScreen").forGetter(ACInfoData::disableACScreen),
                    Codec.STRING.optionalFieldOf("autoConfigPath", "").forGetter(ACInfoData::autoConfigPath)
            ).apply(builder, ACInfoData::new)
    );

    public static List<String> getBlackListedMods() {
        return currentData.entrySet().stream()
                .filter(entry -> entry.getValue().disableAC)
                .map(Map.Entry::getKey)
                .toList();
    }

    public static List<String> getClientBlackListedMods() {
        return currentData.entrySet().stream()
                .filter(entry -> entry.getValue().disableACScreen)
                .map(Map.Entry::getKey)
                .toList();
    }
}
