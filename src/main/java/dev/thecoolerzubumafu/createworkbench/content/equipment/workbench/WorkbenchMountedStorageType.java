package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WorkbenchMountedStorageType extends MountedItemStorageType<WorkbenchMountedStorage> {

	public WorkbenchMountedStorageType() {
		super(WorkbenchMountedStorage.CODEC);
	}

	@Override
	@Nullable
	public WorkbenchMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		if (!(be instanceof WorkbenchBlockEntity workbench))
			return null;
		return new WorkbenchMountedStorage(new WorkbenchContents(workbench.storage()
			.contents()));
	}
}
