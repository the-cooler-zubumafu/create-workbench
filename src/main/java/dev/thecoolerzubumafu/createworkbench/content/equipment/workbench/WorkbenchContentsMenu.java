package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;

import dev.thecoolerzubumafu.createworkbench.AllMenuTypes;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

public class WorkbenchContentsMenu extends ToolboxMenu {

	private final WorkbenchBlockEntity workbench;
	private final StoredToolbox stored;
	private final int slot;

	public WorkbenchContentsMenu(int id, Inventory inventory, ToolboxBlockEntity holder,
	                             WorkbenchBlockEntity workbench, int slot, StoredToolbox stored) {
		super(AllMenuTypes.WORKBENCH_CONTENTS.get(), id, inventory, holder);
		this.workbench = workbench;
		this.slot = slot;
		this.stored = stored;
	}

	public WorkbenchContentsMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
		super(AllMenuTypes.WORKBENCH_CONTENTS.get(), id, inventory, data);
		this.workbench = null;
		this.slot = -1;
		this.stored = null;
		contentHolder.setLevel(inventory.player.level());
	}

	@Override
	protected ToolboxBlockEntity createOnClient(RegistryFriendlyByteBuf data) {
		BlockPos pos = data.readBlockPos();
		CompoundTag nbt = data.readNbt();
		DyeColor color = DyeColor.byId(data.readVarInt());
		ToolboxBlockEntity holder = new ToolboxBlockEntity(AllBlockEntityTypes.TOOLBOX.get(), pos,
			AllBlocks.TOOLBOXES.get(color)
				.get()
				.defaultBlockState());
		if (nbt != null)
			holder.readClient(nbt, data.registryAccess());
		return holder;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (workbench == null || stored == null || player.level().isClientSide)
			return;
		CompoundTag tag = contentHolder.saveWithoutMetadata(player.level().registryAccess());
		stored.inventory()
			.deserializeNBT(player.level().registryAccess(), tag.getCompound("Inventory"));
		workbench.setChanged();
		CreateWorkbench.LOGGER.debug("Workbench contents closed for slot {} at {}", slot, workbench.getBlockPos());
	}
}
