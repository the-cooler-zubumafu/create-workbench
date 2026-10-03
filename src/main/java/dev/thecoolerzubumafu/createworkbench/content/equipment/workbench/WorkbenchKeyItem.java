package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.UUID;

import dev.thecoolerzubumafu.createworkbench.AllDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A key carries a Lock ID; possession of a key whose Lock ID matches a Workbench's
 * grants Access to that Workbench.
 */
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
