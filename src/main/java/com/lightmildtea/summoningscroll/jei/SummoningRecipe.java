package com.lightmildtea.summoningscroll.jei;

import com.lightmildtea.summoningscroll.SummonConfigLoader;
import com.lightmildtea.summoningscroll.SummoningScroll;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SummoningRecipe {

    // The catalyst item (offhand)
    private final ItemStack catalyst;

    // The spawn egg or representative item for the entity
    private final ItemStack result;

    // The entity type being summoned
    private final EntityType<?> entityType;

    public SummoningRecipe(ItemStack catalyst, ItemStack result, EntityType<?> entityType) {
        this.catalyst = catalyst;
        this.result = result;
        this.entityType = entityType;
    }

    public ItemStack getCatalyst() { return catalyst; }
    public ItemStack getResult() { return result; }
    public EntityType<?> getEntityType() { return entityType; }

    // Build all recipes from config
    public static List<SummoningRecipe> getAllRecipes() {
        List<SummoningRecipe> recipes = new ArrayList<>();

        for (Map.Entry<Item, EntityType<?>> entry : SummonConfigLoader.loadedSummonMap.entrySet()) {
            Item catalystItem = entry.getKey();
            EntityType<?> entityType = entry.getValue();

            ItemStack catalystStack = new ItemStack(catalystItem);

            // Try to find a spawn egg for this entity
            ItemStack resultStack = getSpawnEgg(entityType);

            recipes.add(new SummoningRecipe(catalystStack, resultStack, entityType));
        }

        SummoningScroll.LOGGER.info("[SummoningScroll] Registered {} JEI recipes.", recipes.size());
        return recipes;
    }

    // Find the spawn egg for an entity type
    private static ItemStack getSpawnEgg(EntityType<?> entityType) {
        SpawnEggItem egg = SpawnEggItem.byId(entityType);
        if (egg != null) {
            return new ItemStack(egg);
        }
        return ItemStack.EMPTY; // JEI output slot just stays empty
    }
}