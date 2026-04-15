package com.lightmildtea.summoningscroll.item;

import com.lightmildtea.summoningscroll.SummonConfigLoader;
import com.lightmildtea.summoningscroll.network.PlayScrollAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

public class SummoningScrollItem extends Item {

    private static final int CHARGE_DURATION = 72000;

    public SummoningScrollItem(Properties properties) {
        super(properties);
    }

    // ─────────────────────────────────────────────
    // Start Charging
    // ─────────────────────────────────────────────
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        // Check - Offhand is empty
        if (offhand.isEmpty()) {
            if (!level.isClientSide) {
                player.sendSystemMessage(
                        Component.literal("§cYou need a catalyst in your offhand!")
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        // Check - Offhand item
        if (!SummonConfigLoader.isValidCatalyst(offhand.getItem())) {
            if (!level.isClientSide) {
                player.sendSystemMessage(
                        Component.literal("§cThis is not a valid catalyst!")
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // ─────────────────────────────────────────────
    // Release to Summon
    // ─────────────────────────────────────────────
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer serverPlayer)) return;

        int timeUsed = this.getUseDuration(stack, entity) - timeLeft;
        if (timeUsed < 5) return;

        performSummon(serverPlayer, stack, level);
    }

    // ─────────────────────────────────────────────
    // Summon Logic
    // ─────────────────────────────────────────────
    private void performSummon(ServerPlayer player, ItemStack stack, Level level) {
        ItemStack offhandStack = player.getOffhandItem();

        if (offhandStack.isEmpty() || !SummonConfigLoader.isValidCatalyst(offhandStack.getItem())) {
            player.sendSystemMessage(Component.literal("§cCatalyst missing!"));
            return;
        }

        EntityType<?> entityType = SummonConfigLoader.getEntityType(offhandStack.getItem());
        if (entityType == null) return;

        BlockPos spawnPos = player.blockPosition().relative(player.getDirection());

        if (!(level instanceof ServerLevel serverLevel)) return;

        var spawnedEntity = entityType.spawn(
                serverLevel,
                null,
                null,
                spawnPos,
                MobSpawnType.MOB_SUMMONED,
                true,
                false
        );

        if (spawnedEntity != null) {
            triggerScrollAnimation(player, stack, serverLevel);

            offhandStack.shrink(1);
            stack.shrink(1);

            player.sendSystemMessage(
                    Component.literal("§aSummoned §e" + spawnedEntity.getName().getString() + "§a!")
            );
        }
    }

    // ─────────────────────────────────────────────
    // Animation + Sound
    // ─────────────────────────────────────────────
    private void triggerScrollAnimation(ServerPlayer player, ItemStack stack, ServerLevel level) {
        PacketDistributor.sendToPlayer(
                player,
                new PlayScrollAnimationPacket(stack.copy())
        );

        level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }

    // ─────────────────────────────────────────────
    // Duration & Animation Type
    // ─────────────────────────────────────────────
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHARGE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    // ─────────────────────────────────────────────
    // Tooltips
    // ─────────────────────────────────────────────
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(
                Component.literal("§7Hold Right-Click while holding a catalyst in your offhand to summon an entity.")
        );
    }
}