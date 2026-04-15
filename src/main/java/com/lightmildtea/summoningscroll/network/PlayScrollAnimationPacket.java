package com.lightmildtea.summoningscroll.network;

import com.lightmildtea.summoningscroll.SummoningScroll;
import com.lightmildtea.summoningscroll.client.ScrollAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayScrollAnimationPacket(ItemStack scroll) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(SummoningScroll.MODID, "play_scroll_animation");

    public static final Type<PlayScrollAnimationPacket> TYPE =
            new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayScrollAnimationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    PlayScrollAnimationPacket::scroll,
                    PlayScrollAnimationPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Handle on client side
    public static void handle(PlayScrollAnimationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            SummoningScroll.LOGGER.info("Packet received! Calling displayItemActivation directly");

            // Store scroll for the mixin to swap
            ScrollAnimationHandler.pendingScrollStack = packet.scroll().copy();

            // Call displayItemActivation directly instead of handleEntityEvent
            mc.gameRenderer.displayItemActivation(packet.scroll().copy());
        });
    }
}