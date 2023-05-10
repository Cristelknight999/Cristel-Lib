package net.cristellib.api;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.config.ConfigType;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

@CristelPlugin
public class BuiltInAPI implements CristelLibAPI {
    public static StructureConfig MINECRAFT_ED = StructureConfig.createWithDefaultConfigPath(CristelLib.MOD_ID, "minecraftED", ConfigType.ENABLE_DISABLE);
    public static StructureConfig MINECRAFT_P = StructureConfig.createWithDefaultConfigPath(CristelLib.MOD_ID, "minecraftP", ConfigType.PLACEMENT);

    @Override
    public void registerConfigs(Set<StructureConfig> sets) {
        sets.add(MINECRAFT_ED);
        sets.add(MINECRAFT_P);


        MINECRAFT_P.setHeader("""
                    This config file makes it possible to switch off any Minecraft structure.
                    To disable a structure, simply set the value of that structure to "false".
                    To change the rarity of a structure category, use the other file in the config.
                """);
        MINECRAFT_ED.setHeader("""
            This config file makes it possible to change the spacing, separation, salt (and frequency) of Minecraft's structure sets.
            	SPACING ---  controls how far a structure can be from others of its kind
            	SEPARATION --- controls how close to each other two structures of the same type can be.
            KEEP IN MIND THAT SPACING ALWAYS NEEDS TO BE HIGHER THAN SEPARATION.
            """);
    }

    @Override
    public void registerStructureSets(CristelLibRegistry registry) {
        registry.registerSetToConfig("t_and_t", new ResourceLocation("towns_and_towers", "towns"), MINECRAFT_ED, MINECRAFT_P);

        registry.registerSetToConfig("t_and_t", new ResourceLocation("towns_and_towers", "other"), MINECRAFT_ED, MINECRAFT_P);
        registry.registerSetToConfig("t_and_t", new ResourceLocation("towns_and_towers", "towers"), MINECRAFT_ED, MINECRAFT_P);
    }
}
