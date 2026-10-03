package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_OFF;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_HOTBAR_ON;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_SELECTED_OFF;
import static com.simibubi.create.foundation.gui.AllGuiTextures.TOOLBELT_SELECTED_ON;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import dev.thecoolerzubumafu.createworkbench.WorkbenchEquipPacket;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Client glue for the Workbench Radial: opens it on the keybind, draws the hotbar
 * binding overlay and implements pick-block compatibility.
 */
public class WorkbenchHandlerClient {

	public static final LayeredDraw.Layer OVERLAY = WorkbenchHandlerClient::renderOverlay;

	static int COOLDOWN = 0;

	public static void clientTick() {
		if (COOLDOWN > 0 && !WorkbenchKeybinds.OPEN_RADIAL.isDown())
			COOLDOWN--;
		while (WorkbenchKeybinds.OPEN_RADIAL.consumeClick()) {
			if (COOLDOWN > 0)
				continue;
			onKeyInput();
		}
	}

	public static boolean onPickItem() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null)
			return false;
		Level level = player.level();
		HitResult hitResult = mc.hitResult;
		if (hitResult == null || hitResult.getType() == HitResult.Type.MISS)
			return false;
		if (player.isCreative())
			return false;

		ItemStack result = ItemStack.EMPTY;
		List<WorkbenchRadialMenu.Entry> entries = buildEntries(player);
		if (entries.isEmpty())
			return false;

		if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
			if (level.getBlockState(pos)
				.isAir())
				return false;
			result = level.getBlockState(pos)
				.getCloneItemStack(hitResult, level, pos, player);
		} else if (hitResult.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult) hitResult).getEntity();
			result = entity.getPickedResult(hitResult);
		}

		if (result.isEmpty())
			return false;

		for (WorkbenchRadialMenu.Entry entry : entries) {
			ToolboxInventory inventory = entry.stored()
				.inventory();
			for (int compartment = 0; compartment < 8; compartment++) {
				ItemStack inSlot = inventory.takeFromCompartment(1, compartment, true);
				if (inSlot.isEmpty() || inSlot.getItem() != result.getItem())
					continue;
				if (!ItemStack.matches(inSlot, result))
					continue;
				CatnipServices.NETWORK.sendToServer(
					new WorkbenchEquipPacket(entry.workbench()
						.getBlockPos(), entry.slot(), compartment, player.getInventory().selected));
				return true;
			}
		}
		return false;
	}

	public static void onKeyInput() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;
		LocalPlayer player = mc.player;
		if (player == null)
			return;

		List<WorkbenchRadialMenu.Entry> entries = buildEntries(player);
		CompoundTag compound = player.getPersistentData()
			.getCompound(WorkbenchHandler.DATA_KEY);
		String slotKey = String.valueOf(player.getInventory().selected);

		if (compound.contains(slotKey)) {
			CompoundTag data = compound.getCompound(slotKey);
			BlockPos pos = NBTHelper.readBlockPos(data, "Pos");
			int slot = data.getInt("Slot");
			WorkbenchRadialMenu.Entry current = find(entries, pos, slot);
			if (current != null) {
				WorkbenchRadialMenu screen =
					new WorkbenchRadialMenu(entries, WorkbenchRadialMenu.State.SELECT_ITEM_UNEQUIP, current);
				screen.prevSlot(data.getInt("Compartment"));
				ScreenOpener.open(screen);
				return;
			}
			ScreenOpener.open(new WorkbenchRadialMenu(List.of(), WorkbenchRadialMenu.State.DETACH, null));
			return;
		}

		if (entries.isEmpty())
			return;
		if (entries.size() == 1)
			ScreenOpener.open(new WorkbenchRadialMenu(entries, WorkbenchRadialMenu.State.SELECT_ITEM, entries.get(0)));
		else
			ScreenOpener.open(new WorkbenchRadialMenu(entries, WorkbenchRadialMenu.State.SELECT_BOX, null));
	}

	private static WorkbenchRadialMenu.Entry find(List<WorkbenchRadialMenu.Entry> entries, BlockPos pos, int slot) {
		for (WorkbenchRadialMenu.Entry entry : entries)
			if (entry.slot() == slot && entry.workbench()
				.getBlockPos()
				.equals(pos))
				return entry;
		return null;
	}

	public static List<WorkbenchRadialMenu.Entry> buildEntries(Player player) {
		List<WorkbenchRadialMenu.Entry> entries = new ArrayList<>();
		for (WorkbenchBlockEntity be : WorkbenchHandler.getNearest(player.level(), player, 8))
			for (int slot = 0; slot < WorkbenchStorage.CAPACITY; slot++) {
				StoredToolbox stored = be.storage()
					.get(slot);
				if (stored != null)
					entries.add(new WorkbenchRadialMenu.Entry(be, slot, stored));
			}
		return entries;
	}

	public static void renderOverlay(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;
		Player player = mc.player;
		if (player == null)
			return;

		CompoundTag compound = player.getPersistentData()
			.getCompound(WorkbenchHandler.DATA_KEY);
		if (compound.isEmpty())
			return;

		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		int x = width / 2 - 90;
		int y = height - 23;
		RenderSystem.enableDepthTest();

		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		double max = WorkbenchHandler.getMaxRange(player);
		for (int slot = 0; slot < WorkbenchHandler.BINDING_SLOTS; slot++) {
			String key = String.valueOf(slot);
			if (!compound.contains(key))
				continue;
			BlockPos pos = NBTHelper.readBlockPos(compound.getCompound(key), "Pos");
			boolean selected = player.getInventory().selected == slot;
			int offset = selected ? 1 : 0;
			AllGuiTextures texture = WorkbenchHandler.distance(player.position(), pos) < max * max
				? selected ? TOOLBELT_SELECTED_ON : TOOLBELT_HOTBAR_ON
				: selected ? TOOLBELT_SELECTED_OFF : TOOLBELT_HOTBAR_OFF;
			texture.render(graphics, x + 20 * slot - offset, y + offset);
		}
		poseStack.popPose();
	}
}
