package dev.thecoolerzubumafu.createworkbench;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchKeyItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateWorkbench.ID);

    public static final DeferredItem<WorkbenchKeyItem> WORKBENCH_KEY = ITEMS.register(
            "workbench_key",
            () -> new WorkbenchKeyItem(
                    new Item.Properties()
                            .stacksTo(1)
            )
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
