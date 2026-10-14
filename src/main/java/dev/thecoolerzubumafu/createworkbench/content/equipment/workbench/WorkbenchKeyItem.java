package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.UUID;

import dev.thecoolerzubumafu.createworkbench.AllDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

public class WorkbenchKeyItem extends Item {
	public WorkbenchKeyItem(Properties properties) {
		super(properties);
	}

	public static UUID lockIdOf(ItemStack stack) {
		return stack.get(AllDataComponents.WORKBENCH_LOCK.get());
	}

	public static void setLockId(ItemStack stack, UUID lockId) {
		stack.set(AllDataComponents.WORKBENCH_LOCK.get(), lockId);
	}

	public static void clearLockId(ItemStack stack) {
		stack.remove(AllDataComponents.WORKBENCH_LOCK.get());
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return lockIdOf(stack) == null ? 16 : 1;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return lockIdOf(stack) != null;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return true;
	}

	public static ItemStack find(Player player) {
		for (ItemStack stack : player.getInventory().items)
			if (stack.getItem() instanceof WorkbenchKeyItem && lockIdOf(stack) != null)
				return stack;
		for (ItemStack stack : player.getInventory().offhand)
			if (stack.getItem() instanceof WorkbenchKeyItem && lockIdOf(stack) != null)
				return stack;
		return ItemStack.EMPTY;
	}
}
