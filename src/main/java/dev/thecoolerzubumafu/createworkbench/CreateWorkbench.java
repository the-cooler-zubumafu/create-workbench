package dev.thecoolerzubumafu.createworkbench;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

@Mod(CreateWorkbench.ID)
public class CreateWorkbench {

    public static final String ID = "createworkbench";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateWorkbench(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        AllCreativeModeTabs.register(modEventBus);
        AllItems.register(modEventBus);
        AllBlocks.register(modEventBus);
        AllBlockEntities.register(modEventBus);
        AllDataComponents.register(modEventBus);
        AllMenuTypes.register(modEventBus);
        AllMountedStorageTypes.register(modEventBus);
        modEventBus.addListener(AllPackets::register);
        modEventBus.addListener(AllBlockEntities::registerCapabilities);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AllMountedStorageTypes::associateBlocks);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.CREATE_WORKBENCH_TAB) {
            event.accept(AllItems.WORKBENCH_KEY);
            event.accept(AllBlocks.WORKBENCH);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        WorkbenchHandler.entityTick(event.getEntity(), event.getEntity().level());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        WorkbenchHandler.playerLogin(event.getEntity());
    }
}
