package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

public class WorkbenchKeybinds {
	public static final String CATEGORY = "key.categories.createworkbench";

	public static final KeyMapping OPEN_RADIAL = new KeyMapping(
		"key.createworkbench.open_radial", InputConstants.KEY_GRAVE, CATEGORY);
}
