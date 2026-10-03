package dev.thecoolerzubumafu.createworkbench;

import java.util.function.Supplier;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchContents;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(CreateWorkbench.ID);

    public static final Supplier<DataComponentType<WorkbenchContents>> WORKBENCH_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "workbench_contents",
                    builder -> builder
                            .persistent(WorkbenchContents.CODEC)
                            .networkSynchronized(WorkbenchContents.STREAM_CODEC)
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

}
