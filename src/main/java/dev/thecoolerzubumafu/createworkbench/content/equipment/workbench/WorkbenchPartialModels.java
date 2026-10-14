package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.resources.ResourceLocation;

public class WorkbenchPartialModels {
    public static final PartialModel LID_LEFT = block("workbench_lid_left");
    public static final PartialModel LID_RIGHT = block("workbench_lid_right");
    public static final PartialModel DRAWER = block("workbench_drawer");

    private static PartialModel block(String path) {
        return PartialModel.of(
                ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "block/" + path));
    }
}
