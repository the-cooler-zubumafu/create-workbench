package dev.thecoolerzubumafu.createworkbench;

import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchContentsScreen;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchHandlerClient;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchKeybinds;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchRenderer;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CreateWorkbench.ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CreateWorkbench.ID, value = Dist.CLIENT)
public class CreateWorkbenchClient {
    public CreateWorkbenchClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> WorkbenchHandlerClient.clientTick());
        NeoForge.EVENT_BUS.addListener(this::onInteractionKey);
    }

    private void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isPickBlock())
            return;
        if (WorkbenchHandlerClient.onPickItem()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AllMenuTypes.WORKBENCH.get(), WorkbenchScreen::new);
        event.<ToolboxMenu, WorkbenchContentsScreen>register(AllMenuTypes.WORKBENCH_CONTENTS.get(),
                WorkbenchContentsScreen::new);
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(WorkbenchKeybinds.OPEN_RADIAL);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AllBlockEntities.WORKBENCH_BLOCKENTITY.get(), WorkbenchRenderer::new);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "workbench_overlay"),
                WorkbenchHandlerClient.OVERLAY);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        CreateWorkbench.LOGGER.debug("HELLO FROM CLIENT SETUP");
        CreateWorkbench.LOGGER.debug("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
