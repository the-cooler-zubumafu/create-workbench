package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.resources.ResourceLocation;

/**
 * The moving pieces of the Workbench, rendered by {@link WorkbenchRenderer} on top
 * of the static body block model: the two sliding top lids and the two pull-out
 * drawer halves (each animated independently).
 */
public class WorkbenchPartialModels {

    public static final PartialModel LID_LEFT = block("workbench_lid_left");
    public static final PartialModel LID_RIGHT = block("workbench_lid_right");
    public static final PartialModel DRAWER_LEFT = block("workbench_drawer_left");
    public static final PartialModel DRAWER_RIGHT = block("workbench_drawer_right");

    private static PartialModel block(String path) {
        return PartialModel.of(
                ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "block/" + path));
    }
}
