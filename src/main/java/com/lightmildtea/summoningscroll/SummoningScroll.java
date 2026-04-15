package com.lightmildtea.summoningscroll;

import com.lightmildtea.summoningscroll.item.SummoningScrollItem;
import com.lightmildtea.summoningscroll.network.PlayScrollAnimationPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slf4j.Logger;

@Mod(SummoningScroll.MODID)
public class SummoningScroll {

    public static final String MODID = "summoningscroll";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> SUMMONING_SCROLL =
            ITEMS.register("summoning_scroll",
                    () -> new SummoningScrollItem(
                            new Item.Properties()
                                    .stacksTo(16)
                                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    )
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SUMMONING_TAB =
            CREATIVE_MODE_TABS.register("summoning_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.summoningscroll"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> SUMMONING_SCROLL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(SUMMONING_SCROLL.get());
                    }).build());

    public SummoningScroll(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        // ✅ Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Summoning Scroll mod loaded!");
        event.enqueueWork(SummonConfigLoader::reload);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                PlayScrollAnimationPacket.TYPE,
                PlayScrollAnimationPacket.STREAM_CODEC,
                PlayScrollAnimationPacket::handle
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        SummonConfigLoader.reload();
        LOGGER.info("Summoning Scroll server starting!");
    }
}