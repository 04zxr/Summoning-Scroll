package com.lightmildtea.summoningscroll;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class SummonConfigLoader {

    // Item ID -> EntityType (for direct item matches)
    public static Map<Item, EntityType<?>> loadedSummonMap = new HashMap<>();

    // TagKey -> EntityType (for tag matches)
    public static Map<TagKey<Item>, EntityType<?>> loadedTagSummonMap = new HashMap<>();

    public static void reload() {
        loadedSummonMap.clear();
        loadedTagSummonMap.clear();

        for (String entry : Config.SUMMON_ENTRIES.get()) {
            try {
                String[] parts = entry.split("=");
                if (parts.length != 2) {
                    SummoningScroll.LOGGER.warn("[SummoningScroll] Invalid entry skipped: {}", entry);
                    continue;
                }

                String catalystId = parts[0].trim();
                String entityId = parts[1].trim();

                // Look up entity type
                ResourceLocation entityLocation = ResourceLocation.parse(entityId);
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityLocation);

                if (entityType == null) {
                    SummoningScroll.LOGGER.warn("[SummoningScroll] Unknown entity: {}", entityId);
                    continue;
                }

                // Check if catalyst is a tag (starts with #)
                if (catalystId.startsWith("#")) {
                    String tagId = catalystId.substring(1); // Remove the #
                    ResourceLocation tagLocation = ResourceLocation.parse(tagId);
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);

                    loadedTagSummonMap.put(tagKey, entityType);
                    SummoningScroll.LOGGER.info("[SummoningScroll] Registered tag: {} -> {}", catalystId, entityId);

                } else {
                    // Regular item
                    ResourceLocation itemLocation = ResourceLocation.parse(catalystId);
                    Item item = BuiltInRegistries.ITEM.get(itemLocation);

                    if (item == null) {
                        SummoningScroll.LOGGER.warn("[SummoningScroll] Unknown item: {}", catalystId);
                        continue;
                    }

                    loadedSummonMap.put(item, entityType);
                    SummoningScroll.LOGGER.info("[SummoningScroll] Registered item: {} -> {}", catalystId, entityId);
                }

            } catch (Exception e) {
                SummoningScroll.LOGGER.error("[SummoningScroll] Failed to parse entry: {} | Error: {}", entry, e.getMessage());
            }
        }

        SummoningScroll.LOGGER.info("[SummoningScroll] Loaded {} item entries and {} tag entries from config.",
                loadedSummonMap.size(), loadedTagSummonMap.size());
    }

    // Check if an item matches any registered catalyst (item or tag)
    public static boolean isValidCatalyst(Item item) {
        // Check direct item match first
        if (loadedSummonMap.containsKey(item)) return true;

        // Check tag matches
        for (TagKey<Item> tag : loadedTagSummonMap.keySet()) {
            if (item.builtInRegistryHolder().is(tag)) return true;
        }

        return false;
    }

    // Get entity type for an item (checks both item and tag maps)
    public static EntityType<?> getEntityType(Item item) {
        // Check direct item match first
        if (loadedSummonMap.containsKey(item)) {
            return loadedSummonMap.get(item);
        }

        // Check tag matches
        for (Map.Entry<TagKey<Item>, EntityType<?>> entry : loadedTagSummonMap.entrySet()) {
            if (item.builtInRegistryHolder().is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}