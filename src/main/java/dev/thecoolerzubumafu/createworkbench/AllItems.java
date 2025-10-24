package dev.thecoolerzubumafu.createworkbench;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateWorkbench.MOD_ID);

    public static final DeferredItem<Item> WORKBENCH_KEY = ITEMS.register(
            Items.WORKBENCH_KEY.key,
            () -> new Item(new Item.Properties())
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    enum Items {
        WORKBENCH_KEY("workbench_key");

        private final String key;

        Items(String key) {
            this.key = key;
        }
    }

}
