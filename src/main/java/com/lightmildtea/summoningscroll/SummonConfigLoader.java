package com.lightmildtea.summoningscroll;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class SummonConfigLoader {

    // The loaded map that SummoningScrollItem will use
    public static Map<Item, EntityType<?>> loadedSummonMap = new HashMap<>();

    public static void reload() {
        loadedSummonMap.clear();

        for (String entry : Config.SUMMON_ENTRIES.get()) {
            try {
                String[] parts = entry.split("=");
                if (parts.length != 2) {
                    SummoningScroll.LOGGER.warn("[SummoningScroll] Invalid entry skipped: {}", entry);
                    continue;
                }

                String itemId = parts[0].trim();
                String entityId = parts[1].trim();

                // Look up item
                ResourceLocation itemLocation = ResourceLocation.parse(itemId);
                Item item = BuiltInRegistries.ITEM.get(itemLocation);

                if (item == null) {
                    SummoningScroll.LOGGER.warn("[SummoningScroll] Unknown item: {}", itemId);
                    continue;
                }

                // Look up entity
                ResourceLocation entityLocation = ResourceLocation.parse(entityId);
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityLocation);

                if (entityType == null) {
                    SummoningScroll.LOGGER.warn("[SummoningScroll] Unknown entity: {}", entityId);
                    continue;
                }

                loadedSummonMap.put(item, entityType);
                SummoningScroll.LOGGER.info("[SummoningScroll] Registered: {} -> {}", itemId, entityId);

            } catch (Exception e) {
                SummoningScroll.LOGGER.error("[SummoningScroll] Failed to parse entry: {} | Error: {}", entry, e.getMessage());
            }
        }

        SummoningScroll.LOGGER.info("[SummoningScroll] Loaded {} summon entries from config.", loadedSummonMap.size());
    }
}