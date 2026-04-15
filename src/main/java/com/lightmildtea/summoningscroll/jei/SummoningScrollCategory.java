package com.lightmildtea.summoningscroll.jei;

import com.lightmildtea.summoningscroll.SummoningScroll;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SummoningScrollCategory implements IRecipeCategory<SummoningRecipe> {

    public static final RecipeType<SummoningRecipe> RECIPE_TYPE = RecipeType.create(
            SummoningScroll.MODID,
            "summoning",
            SummoningRecipe.class
    );

    private final IDrawable icon;

    public SummoningScrollCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(SummoningScroll.SUMMONING_SCROLL.get())
        );
    }

    @Override
    public RecipeType<SummoningRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Summoning Scroll");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 90;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SummoningRecipe recipe, IFocusGroup focuses) {
        // Main Hand - Scroll
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStack(new ItemStack(SummoningScroll.SUMMONING_SCROLL.get()));

        // Off Hand - Catalyst (show all tag items if from tag)
        var catalystSlot = builder.addSlot(RecipeIngredientRole.INPUT, 37, 1);
        if (recipe.hasTagItems()) {
            catalystSlot.addItemStacks(recipe.getTagItems()); // Cycles through all tag items
        } else {
            catalystSlot.addItemStack(recipe.getCatalyst());
        }

        // Result - Spawn Egg
        if (!recipe.getResult().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 73, 1)
                    .addItemStack(recipe.getResult());
        }
    }
}