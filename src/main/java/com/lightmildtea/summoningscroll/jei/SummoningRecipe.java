package com.lightmildtea.summoningscroll.jei;

import com.lightmildtea.summoningscroll.SummonConfigLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
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

    // All items if catalyst comes from a tag
    private List<ItemStack> tagItems = new ArrayList<>();

    public SummoningRecipe(ItemStack catalyst, ItemStack result, EntityType<?> entityType) {
        this.catalyst = catalyst;
        this.result = result;
        this.entityType = entityType;
    }

    public ItemStack getCatalyst() { return catalyst; }
    public ItemStack getResult() { return result; }
    public EntityType<?> getEntityType() { return entityType; }
    public List<ItemStack> getTagItems() { return tagItems; }
    public void setTagItems(List<ItemStack> items) { this.tagItems = items; }
    public boolean hasTagItems() { return !tagItems.isEmpty(); }

    // Build all recipes from config
    public static List<SummoningRecipe> getAllRecipes() {
        List<SummoningRecipe> recipes = new ArrayList<>();

        // Direct item entries
        for (Map.Entry<Item, EntityType<?>> entry : SummonConfigLoader.loadedSummonMap.entrySet()) {
            Item catalystItem = entry.getKey();
            EntityType<?> entityType = entry.getValue();

            ItemStack catalystStack = new ItemStack(catalystItem);
            ItemStack resultStack = getSpawnEgg(entityType);

            recipes.add(new SummoningRecipe(catalystStack, resultStack, entityType));
        }

        // Tag entries - expand all items in the tag
        for (Map.Entry<TagKey<Item>, EntityType<?>> entry : SummonConfigLoader.loadedTagSummonMap.entrySet()) {
            TagKey<Item> tag = entry.getKey();
            EntityType<?> entityType = entry.getValue();
            ItemStack resultStack = getSpawnEgg(entityType);

            // Get all items in the tag
            List<ItemStack> tagItems = new ArrayList<>();
            BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> {
                tagItems.add(new ItemStack(holder.value()));
            });

            if (!tagItems.isEmpty()) {
                // Use first item as representative catalyst
                // JEI will show all items in the tag as cycling ingredients
                SummoningRecipe recipe = new SummoningRecipe(tagItems.get(0), resultStack, entityType);
                recipe.setTagItems(tagItems);
                recipes.add(recipe);
            }
        }

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