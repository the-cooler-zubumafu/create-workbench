package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

class WorkbenchContentsTest {

	@Test
	void codecRoundTripsStoredToolboxes() {
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
		UUID id = UUID.randomUUID();
		WorkbenchContents contents =
			new WorkbenchContents(Map.of(0, new StoredToolbox(inventory, DyeColor.LIME, id)));

		RegistryOps<com.google.gson.JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

		com.google.gson.JsonElement encoded = WorkbenchContents.CODEC.encodeStart(ops, contents)
			.getOrThrow();
		WorkbenchContents decoded = WorkbenchContents.CODEC.parse(ops, encoded)
			.getOrThrow();

		assertEquals(1, decoded.toolboxes()
			.size());
		StoredToolbox toolbox = decoded.toolboxes()
			.get(0);
		assertEquals(DyeColor.LIME, toolbox.color());
		assertEquals(id, toolbox.uuid());
		assertEquals(3, toolbox.inventory()
			.getStackInSlot(0)
			.getCount());
		assertTrue(toolbox.inventory()
			.getStackInSlot(0)
			.is(Items.DIAMOND));
	}

	@Test
	void codecRoundTripsHoles() {
		ToolboxInventory inventory = new ToolboxInventory(null);
		WorkbenchContents contents = new WorkbenchContents(Map.of(
			0, new StoredToolbox(inventory, DyeColor.RED, UUID.randomUUID()),
			2, new StoredToolbox(inventory, DyeColor.BLUE, UUID.randomUUID())));

		RegistryOps<com.google.gson.JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE,
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

		WorkbenchContents decoded = WorkbenchContents.CODEC
			.parse(ops, WorkbenchContents.CODEC.encodeStart(ops, contents)
				.getOrThrow())
			.getOrThrow();

		assertEquals(2, decoded.toolboxes()
			.size());
		assertTrue(decoded.toolboxes()
			.containsKey(0));
		assertFalse(decoded.toolboxes()
			.containsKey(1), "slot 1 is a hole");
		assertTrue(decoded.toolboxes()
			.containsKey(2));
	}
}
