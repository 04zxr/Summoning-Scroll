package com.lightmildtea.summoningscroll.mixin;

import com.lightmildtea.summoningscroll.SummoningScroll;
import com.lightmildtea.summoningscroll.client.ScrollAnimationHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyVariable(
            method = "displayItemActivation(Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private ItemStack modifyActivationItem(ItemStack original) {
        SummoningScroll.LOGGER.info("displayItemActivation called! original: {}", original);

        if (!ScrollAnimationHandler.pendingScrollStack.isEmpty()) {
            ItemStack scroll = ScrollAnimationHandler.pendingScrollStack;
            ScrollAnimationHandler.pendingScrollStack = ItemStack.EMPTY;
            SummoningScroll.LOGGER.info("Returning scroll: {}", scroll);
            return scroll;
        }
        return original;
    }
}