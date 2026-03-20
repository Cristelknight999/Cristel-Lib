package de.cristelknight.cristellib.data.condition.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.data.condition.ICondition;
import de.cristelknight.cristellib.util.ModVersionComparator;

import java.util.Optional;

public record ModLoadedCondition(String modId, Optional<String> optionalVersion) implements ICondition<ModLoadedCondition> {

    public static final Codec<ModLoadedCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modId),
                    Codec.STRING.optionalFieldOf("version").forGetter(ModLoadedCondition::optionalVersion)
            ).apply(instance, ModLoadedCondition::new));

    @Override
    public boolean test() {
        if (optionalVersion.isEmpty())
            return ModLoadingUtil.isModLoaded(modId);

        String version = optionalVersion.get();
        for (ModVersionComparator comparator : ModVersionComparator.values()) {
            String sign = comparator.getSerialized();
            if (!version.startsWith(sign)) continue;

            return comparator.test(modId, version.replaceFirst(sign, ""));
        }
        Constants.LOGGER.warn("Couldn't compare \"version\": \"{}\" of \"mod\": \"{}\"", version, modId);
        return false;
    }

    @Override
    public Codec<ModLoadedCondition> getCodec() {
        return CODEC;
    }

}
