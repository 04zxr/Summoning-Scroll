package com.lightmildtea.summoningscroll.jei;

import com.lightmildtea.summoningscroll.SummoningScroll;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SummoningScroll.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new SummoningScrollCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Register all summon entries from config as JEI recipes
        registration.addRecipes(
                SummoningScrollCategory.RECIPE_TYPE,
                SummoningRecipe.getAllRecipes()
        );
    }
}