package dev.thecoolerzubumafu.createworkbench;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AllCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            CreateWorkbench.MOD_ID
    );

    public static final Supplier<CreativeModeTab> CREATE_WORKBENCH_TAB = CREATIVE_MODE_TAB.register(
            "createworkbench_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(AllBlocks.WORKBENCH.get()))
                    .title(Component.translatable("creativetab.createworkbench.workbench"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(AllItems.WORKBENCH_KEY);
                        output.accept(AllBlocks.WORKBENCH);
                    })
                    .build()
    );


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
