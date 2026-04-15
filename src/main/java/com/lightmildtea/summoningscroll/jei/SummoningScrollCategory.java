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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class SummoningScrollCategory implements IRecipeCategory<SummoningRecipe> {

    // Recipe type identifier
    public static final RecipeType<SummoningRecipe> RECIPE_TYPE = RecipeType.create(
            SummoningScroll.MODID,
            "summoning",
            SummoningRecipe.class
    );

    private final IDrawable background;
    private final IDrawable icon;

    public SummoningScrollCategory(IGuiHelper guiHelper) {
        // Simple blank background sized for our layout
        // Slot(catalyst) + Arrow + Slot(result) = about 90x18
        this.background = guiHelper.createBlankDrawable(90, 18);

        // Use the scroll as the category icon
        this.icon = guiHelper.createDrawableItemStack(
                new net.minecraft.world.item.ItemStack(SummoningScroll.SUMMONING_SCROLL.get())
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SummoningRecipe recipe, IFocusGroup focuses) {

        // Slot 1 - Scroll (always required)
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStack(new net.minecraft.world.item.ItemStack(
                        SummoningScroll.SUMMONING_SCROLL.get()
                ));

        // Slot 2 - Catalyst (offhand item)
        builder.addSlot(RecipeIngredientRole.INPUT, 37, 1)
                .addItemStack(recipe.getCatalyst());

        // Slot 3 - Result (spawn egg or empty)
        if (!recipe.getResult().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 73, 1)
                    .addItemStack(recipe.getResult());
        }
    }
}