package dev.thecoolerzubumafu.createworkbench;

import java.util.function.Supplier;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateRegistries;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchMountedStorageType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers our contraption-mounted storage type into Create's built-in registry and
 * associates it with the Workbench block.
 */
public class AllMountedStorageTypes {

    public static final DeferredRegister<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPES =
            DeferredRegister.create(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, CreateWorkbench.ID);

    public static final Supplier<WorkbenchMountedStorageType> WORKBENCH =
            MOUNTED_ITEM_STORAGE_TYPES.register("workbench", WorkbenchMountedStorageType::new);

    public static void register(IEventBus eventBus) {
        MOUNTED_ITEM_STORAGE_TYPES.register(eventBus);
    }

    public static void associateBlocks() {
        MountedItemStorageType.REGISTRY.register(AllBlocks.WORKBENCH.get(), WORKBENCH.get());
    }
}
