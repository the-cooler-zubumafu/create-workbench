package dev.thecoolerzubumafu.createworkbench;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AllBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            CreateWorkbench.ID
    );

    public static final Supplier<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH_BLOCKENTITY = BLOCK_ENTITIES.register(
            "workbench_blockentity",
            () -> BlockEntityType.Builder.of(
                    WorkbenchBlockEntity::new,
                    AllBlocks.WORKBENCH.get()
            ).build(null)
    );

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                WORKBENCH_BLOCKENTITY.get(),
                (be, context) -> be.isLocked() ? null : be.itemHandler()
        );
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
