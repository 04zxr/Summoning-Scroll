package com.lightmildtea.summoningscroll;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SummonConfigLoader {

    // Item ID -> EntityType (for direct item matches)
    private static volatile Map<Item, EntityType<?>> loadedSummonMap = Collections.emptyMap();

    // TagKey -> EntityType (for tag matches)
    private static volatile Map<TagKey<Item>, EntityType<?>> loadedTagSummonMap = Collections.emptyMap();

    public static Map<Item, EntityType<?>> getLoadedSummonMap() {
        return loadedSummonMap;
    }

    public static Map<TagKey<Item>, EntityType<?>> getLoadedTagSummonMap() {
        return loadedTagSummonMap;
    }

    public static void reload() {
        Map<Item, EntityType<?>> newItemMap = new HashMap<>();
        Map<TagKey<Item>, EntityType<?>> newTagMap = new HashMap<>();

        for (String entry : Config.SUMMON_ENTRIES.get()) {
            try {
                String[] parts = entry.split("=", 2);
                if (parts.length != 2) {
                    SummoningScroll.LOGGER.warn("Invalid summon entry skipped: {}", entry);
                    continue;
                }

                String catalystId = parts[0].trim();
                String entityId = parts[1].trim();

                // Look up entity type
                ResourceLocation entityLocation = ResourceLocation.tryParse(entityId);
                if (entityLocation == null) {
                    SummoningScroll.LOGGER.warn("Invalid entity id in entry: {}", entry);
                    continue;
                }

                Optional<EntityType<?>> entityOpt = BuiltInRegistries.ENTITY_TYPE.getOptional(entityLocation);
                if (entityOpt.isEmpty()) {
                    SummoningScroll.LOGGER.warn("Unknown entity: {}", entityId);
                    continue;
                }
                EntityType<?> entityType = entityOpt.get();

                // Check if catalyst is a tag (starts with #)
                if (catalystId.startsWith("#")) {
                    String tagId = catalystId.substring(1); // Remove the #
                    ResourceLocation tagLocation = ResourceLocation.tryParse(tagId);
                    if (tagLocation == null) {
                        SummoningScroll.LOGGER.warn("Invalid tag id in entry: {}", entry);
                        continue;
                    }
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);

                    newTagMap.put(tagKey, entityType);

                } else {
                    // Regular item
                    ResourceLocation itemLocation = ResourceLocation.tryParse(catalystId);
                    if (itemLocation == null) {
                        SummoningScroll.LOGGER.warn("Invalid item id in entry: {}", entry);
                        continue;
                    }

                    Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(itemLocation);
                    if (itemOpt.isEmpty()) {
                        SummoningScroll.LOGGER.warn("Unknown item: {}", catalystId);
                        continue;
                    }

                    newItemMap.put(itemOpt.get(), entityType);
                }

            } catch (Exception e) {
                SummoningScroll.LOGGER.error("Failed to parse summon entry: {}", entry, e);
            }
        }

        loadedSummonMap = Collections.unmodifiableMap(newItemMap);
        loadedTagSummonMap = Collections.unmodifiableMap(newTagMap);

        logReloadSummaryIfChanged();
    }

    private static volatile int lastItemCount = -1;
    private static volatile int lastTagCount = -1;

    private static void logReloadSummaryIfChanged() {
        int itemCount = loadedSummonMap.size();
        int tagCount = loadedTagSummonMap.size();

        if (itemCount == lastItemCount && tagCount == lastTagCount) return;
        lastItemCount = itemCount;
        lastTagCount = tagCount;

        SummoningScroll.LOGGER.info("Loaded {} item entries and {} tag entries from config.", itemCount, tagCount);
    }

    private static final Pattern INLINE_ENTRIES_PATTERN =
            Pattern.compile("(?m)^(\\s*)entries\\s*=\\s*\\[[^\\]]*\\]\\s*$");

    public static boolean formatConfigFile() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(SummoningScroll.MODID + "-common.toml");
        if (!Files.exists(configPath)) return false;

        try {
            String text = Files.readString(configPath, StandardCharsets.UTF_8);

            Matcher matcher = INLINE_ENTRIES_PATTERN.matcher(text);
            if (!matcher.find()) return false; // already multiline (or different format)

            List<? extends String> entries = Config.SUMMON_ENTRIES.get();
            String indent = matcher.group(1);
            String elementIndent = indent + "\t";

            StringBuilder replacement = new StringBuilder();
            replacement.append(indent).append("entries = [\n");
            for (int i = 0; i < entries.size(); i++) {
                String entry = entries.get(i);
                replacement
                        .append(elementIndent)
                        .append('"')
                        .append(escapeTomlString(entry))
                        .append('"');
                if (i != entries.size() - 1) replacement.append(',');
                replacement.append('\n');
            }
            replacement.append(indent).append(']');

            String rewritten = matcher.replaceFirst(Matcher.quoteReplacement(replacement.toString()));
            if (rewritten.equals(text)) return false;

            Files.writeString(configPath, rewritten, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            SummoningScroll.LOGGER.debug("Failed to format config file", e);
            return false;
        }
    }

    private static String escapeTomlString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Check if an item matches any registered catalyst (item or tag)
    public static boolean isValidCatalyst(Item item) {
        Map<Item, EntityType<?>> itemMap = loadedSummonMap;
        Map<TagKey<Item>, EntityType<?>> tagMap = loadedTagSummonMap;

        // Check direct item match first
        if (itemMap.containsKey(item)) return true;

        // Check tag matches
        for (TagKey<Item> tag : tagMap.keySet()) {
            if (item.builtInRegistryHolder().is(tag)) return true;
        }

        return false;
    }

    // Get entity type for an item (checks both item and tag maps)
    public static EntityType<?> getEntityType(Item item) {
        Map<Item, EntityType<?>> itemMap = loadedSummonMap;
        Map<TagKey<Item>, EntityType<?>> tagMap = loadedTagSummonMap;

        // Check direct item match first
        EntityType<?> direct = itemMap.get(item);
        if (direct != null) {
            return direct;
        }

        // Check tag matches
        for (Map.Entry<TagKey<Item>, EntityType<?>> entry : tagMap.entrySet()) {
            if (item.builtInRegistryHolder().is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}
