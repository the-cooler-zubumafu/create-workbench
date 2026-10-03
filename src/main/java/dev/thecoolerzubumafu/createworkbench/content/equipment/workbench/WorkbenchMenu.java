package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.thecoolerzubumafu.createworkbench.AllBlocks;
import dev.thecoolerzubumafu.createworkbench.AllMenuTypes;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class WorkbenchMenu extends AbstractContainerMenu {

	private final WorkbenchBlockEntity workbench;
	private final ContainerLevelAccess access;
	private final BlockPos workbenchPos;

	public WorkbenchMenu(int id, Inventory playerInventory, WorkbenchBlockEntity workbench, ContainerLevelAccess access) {
		super(AllMenuTypes.WORKBENCH.get(), id);
		this.workbench = workbench;
		this.access = access;
		this.workbenchPos = workbench.getBlockPos();
		addSlots(playerInventory, workbench.itemHandler());
		CreateWorkbench.LOGGER.debug("Workbench menu opened at {}", workbench.getBlockPos());
	}

	public WorkbenchMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf data) {
		super(AllMenuTypes.WORKBENCH.get(), id);
		this.workbench = null;
		this.access = ContainerLevelAccess.NULL;
		this.workbenchPos = data.readBlockPos();
		addSlots(playerInventory, new ItemStackHandler(WorkbenchStorage.CAPACITY));
	}

	public BlockPos workbenchPos() {
		return workbenchPos;
	}

	private void addSlots(Inventory playerInventory, IItemHandler handler) {
		int[] xOffsets = {79, 112, 145, 151, 145, 112, 79, 73};
		int[] yOffsets = {37, 31, 37, 70, 103, 109, 103, 70};
		for (int slot = 0; slot < WorkbenchStorage.CAPACITY; slot++)
			addSlot(new SlotItemHandler(handler, slot, xOffsets[slot], yOffsets[slot]));

		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 9; col++)
				addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 165 + row * 18));

		for (int col = 0; col < 9; col++)
			addSlot(new Slot(playerInventory, col, 8 + col * 18, 223));
	}

	@Override
	public boolean stillValid(Player player) {
		if (workbench == null)
			return true;
		return AbstractContainerMenu.stillValid(access, player, AllBlocks.WORKBENCH.get());
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem())
			return ItemStack.EMPTY;
		ItemStack stack = slot.getItem();
		ItemStack moved = stack.copy();
		if (index < WorkbenchStorage.CAPACITY) {
			if (!moveItemStackTo(stack, WorkbenchStorage.CAPACITY, slots.size(), true))
				return ItemStack.EMPTY;
		} else {
			if (!moveItemStackTo(stack, 0, WorkbenchStorage.CAPACITY, false))
				return ItemStack.EMPTY;
		}
		if (stack.isEmpty())
			slot.set(ItemStack.EMPTY);
		else
			slot.setChanged();
		return moved;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (workbench != null)
			CreateWorkbench.LOGGER.debug("Workbench menu closed at {}", workbench.getBlockPos());
	}
}
