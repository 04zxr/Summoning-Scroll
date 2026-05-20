package com.lightmildtea.summoningscroll;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        SummoningScroll.LOGGER.info("Loaded {} item entries and {} tag entries from config.",
                loadedSummonMap.size(), loadedTagSummonMap.size());
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
