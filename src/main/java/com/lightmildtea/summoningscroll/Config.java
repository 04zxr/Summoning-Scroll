package com.lightmildtea.summoningscroll;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Each entry is a string in format "modid:item=modid:entity"
    // Example: "minecraft:bone=minecraft:skeleton"
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SUMMON_ENTRIES;

    static {
        BUILDER.comment("Summoning Scroll Configuration");
        BUILDER.push("summons");

        SUMMON_ENTRIES = BUILDER
                .comment(
                        "List of catalyst -> entity mappings.",
                        "Format: \"modid:item=modid:entity\"",
                        "Example: \"minecraft:bone=minecraft:skeleton\""
                )
                .defineListAllowEmpty(
                        "entries",
                        List.of(
                                "minecraft:nether_star=minecraft:wither",
                                "minecraft:echo_shard=minecraft:warden"
                        ),
                        Config::validateEntry
                );

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    // Validates each entry is in correct format
    private static boolean validateEntry(Object entry) {
        if (!(entry instanceof String str)) return false;
        if (!str.contains("=")) return false;

        String[] parts = str.split("=");
        if (parts.length != 2) return false;

        // Both parts must contain a colon (modid:name format)
        return parts[0].contains(":") && parts[1].contains(":");
    }
}