package dev.thecoolerzubumafu.createworkbench;

import java.util.function.Supplier;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, CreateWorkbench.ID);

    public static final Supplier<MenuType<WorkbenchMenu>> WORKBENCH = MENU_TYPES.register(
            "workbench",
            () -> IMenuTypeExtension.create(WorkbenchMenu::new)
    );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

}
