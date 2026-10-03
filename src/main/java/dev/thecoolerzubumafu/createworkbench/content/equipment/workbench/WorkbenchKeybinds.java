package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

/**
 * Own keybind for the Workbench Radial (Grave by default), kept separate from Create's
 * Toolbelt key so the PoC does not need a mixin.
 */
public class WorkbenchKeybinds {

	public static final String CATEGORY = "key.categories.createworkbench";

	public static final KeyMapping OPEN_RADIAL = new KeyMapping(
		"key.createworkbench.open_radial", InputConstants.KEY_GRAVE, CATEGORY);
}
