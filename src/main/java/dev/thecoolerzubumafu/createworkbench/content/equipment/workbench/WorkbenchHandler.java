package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;

import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.foundation.networking.ISyncPersistentData.PersistentDataPacket;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Server (and client-side list) bookkeeping for Workbenches: a registry of loaded
 * Workbenches, the per-player binding data, the restock loop and the nearest query.
 * Mirrors Create's {@code ToolboxHandler}, but binds a (Workbench, Stored Toolbox,
 * Compartment) rather than a standalone Toolbox.
 */
public class WorkbenchHandler {

	public static final String DATA_KEY = "CreateWorkbenchData";
	public static final int BINDING_SLOTS = 9;

	static final int VALIDATION_TIMER = 20;

	public static final WorldAttached<WeakHashMap<BlockPos, WorkbenchBlockEntity>> workbenches =
		new WorldAttached<>(w -> new WeakHashMap<>());

	public static void onLoad(WorkbenchBlockEntity be) {
		if (be.getLevel() == null)
			return;
		workbenches.get(be.getLevel())
			.put(be.getBlockPos(), be);
	}

	public static void onUnload(WorkbenchBlockEntity be) {
		if (be.getLevel() == null)
			return;
		workbenches.get(be.getLevel())
			.remove(be.getBlockPos());
	}

	public static void entityTick(Entity entity, Level world) {
		if (world.isClientSide)
			return;
		if (!(entity instanceof ServerPlayer player))
			return;
		if (entity.tickCount % VALIDATION_TIMER != 0)
			return;

		CompoundTag compound = player.getPersistentData()
			.getCompound(DATA_KEY);
		if (compound.isEmpty())
			return;

		boolean sendData = false;
		for (int hotbarSlot = 0; hotbarSlot < BINDING_SLOTS; hotbarSlot++) {
			String key = String.valueOf(hotbarSlot);
			if (!compound.contains(key))
				continue;

			CompoundTag data = compound.getCompound(key);
			BlockPos pos = NBTHelper.readBlockPos(data, "Pos");
			int slot = data.getInt("Slot");
			int compartment = data.getInt("Compartment");

			if (!world.isLoaded(pos))
				continue;
			if (!(world.getBlockState(pos)
				.getBlock() instanceof WorkbenchBlock)) {
				compound.remove(key);
				sendData = true;
				continue;
			}
			if (!(world.getBlockEntity(pos) instanceof WorkbenchBlockEntity be) || !be.canAccess(player)) {
				compound.remove(key);
				sendData = true;
				continue;
			}

			if (restock(player, be, slot, compartment, hotbarSlot)) {
				compound.remove(key);
				sendData = true;
			}
		}

		if (sendData)
			syncData(player);
	}

	public static void playerLogin(Player player) {
		if (!(player instanceof ServerPlayer serverPlayer))
			return;
		CompoundTag compound = player.getPersistentData()
			.getCompound(DATA_KEY);
		if (!compound.isEmpty())
			syncData(serverPlayer);
	}

	/**
	 * Tops up or trims the bound hotbar slot from its bound Compartment. Returns true
	 * when the binding is no longer valid and should be dropped.
	 */
	public static boolean restock(Player player, WorkbenchBlockEntity be, int slot, int compartment, int hotbarSlot) {
		if (slot < 0 || slot >= WorkbenchStorage.CAPACITY || compartment < 0 || compartment >= 8)
			return true;
		StoredToolbox stored = be.storage()
			.get(slot);
		if (stored == null)
			return true;

		ToolboxInventory inventory = stored.inventory();
		Inventory playerInventory = player.getInventory();
		ItemStack playerStack = playerInventory.getItem(hotbarSlot);
		ItemStack reference = inventory.getStackInSlot(compartment * ToolboxInventory.STACKS_PER_COMPARTMENT);

		if (!reference.isEmpty() && !playerStack.isEmpty()
			&& !ToolboxInventory.canItemsShareCompartment(playerStack, reference))
			return true;

		int target = (reference.isEmpty() ? playerStack.getMaxStackSize() : reference.getMaxStackSize() + 1) / 2;
		int count = playerStack.getCount();
		boolean changed = false;

		if (count < target) {
			ItemStack extracted = inventory.takeFromCompartment(target - count, compartment, false);
			if (!extracted.isEmpty()) {
				ItemStack template = playerStack.isEmpty() ? extracted : playerStack;
				playerInventory.setItem(hotbarSlot, template.copyWithCount(count + extracted.getCount()));
				changed = true;
			}
		} else if (count > target) {
			int excess = count - target;
			ItemStack toDistribute = playerStack.copyWithCount(excess);
			int deposited = excess - inventory.distributeToCompartment(toDistribute, compartment, false)
				.getCount();
			if (deposited > 0) {
				playerInventory.setItem(hotbarSlot, playerStack.copyWithCount(count - deposited));
				changed = true;
			}
		}

		if (changed)
			be.notifyStorageChanged();
		return false;
	}

	/**
	 * Deposits every binding this player has to the given Workbench back into its
	 * Compartment and clears those bindings. Mirrors Create's {@code unequipTracked},
	 * called before a Workbench is picked up so the returned items travel with it.
	 * Returns true when at least one binding was removed.
	 */
	public static boolean unequipTracked(Player player, BlockPos pos) {
		CompoundTag compound = player.getPersistentData()
			.getCompound(DATA_KEY);
		boolean changed = false;
		for (int hotbarSlot = 0; hotbarSlot < BINDING_SLOTS; hotbarSlot++) {
			String key = String.valueOf(hotbarSlot);
			if (!compound.contains(key))
				continue;
			if (!NBTHelper.readBlockPos(compound.getCompound(key), "Pos")
				.equals(pos))
				continue;
			unequip(player, hotbarSlot, false);
			changed = true;
		}
		return changed;
	}

	/** Unequips every player in the level bound to this Workbench, syncing their data. */
	public static void unequipTracked(Level level, BlockPos pos) {
		if (level.isClientSide || !(level instanceof ServerLevel serverLevel))
			return;
		for (ServerPlayer player : serverLevel.players())
			if (unequipTracked(player, pos))
				syncData(player);
	}

	public static void unequip(Player player, int hotbarSlot, boolean keepItems) {
		CompoundTag compound = player.getPersistentData()
			.getCompound(DATA_KEY);
		String key = String.valueOf(hotbarSlot);
		if (!compound.contains(key))
			return;

		CompoundTag data = compound.getCompound(key);
		compound.remove(key);

		if (keepItems)
			return;

		BlockPos pos = NBTHelper.readBlockPos(data, "Pos");
		int slot = data.getInt("Slot");
		int compartment = data.getInt("Compartment");
		if (!(player.level()
			.getBlockEntity(pos) instanceof WorkbenchBlockEntity be))
			return;
		StoredToolbox stored = be.storage()
			.get(slot);
		if (stored == null || compartment < 0 || compartment >= 8)
			return;

		Inventory playerInventory = player.getInventory();
		ItemStack playerStack = playerInventory.getItem(hotbarSlot);
		ItemStack toInsert = ToolboxInventory.cleanItemNBT(playerStack.copy());
		ItemStack remainder = stored.inventory()
			.distributeToCompartment(toInsert, compartment, false);

		if (remainder.getCount() != toInsert.getCount()) {
			playerInventory.setItem(hotbarSlot, remainder);
			be.notifyStorageChanged();
		}
	}

	/** Inserts a stack into the Stored Toolboxes' defined Compartments, returning leftovers. */
	public static ItemStack deposit(WorkbenchBlockEntity be, ItemStack stack) {
		ItemStack remaining = stack;
		for (StoredToolbox stored : be.storage()
			.contents()
			.values()) {
			final ItemStack current = remaining;
			ItemStack[] result = { current };
			stored.inventory()
				.inLimitedMode(inventory -> result[0] = ItemHandlerHelper.insertItemStacked(inventory, current, false));
			remaining = result[0];
			if (remaining.isEmpty())
				break;
		}
		if (remaining.getCount() != stack.getCount())
			be.notifyStorageChanged();
		return remaining;
	}

	public static List<WorkbenchBlockEntity> getNearest(LevelAccessor world, Player player, int maxAmount) {
		Vec3 location = player.position();
		double maxRange = getMaxRange(player);
		double maxRangeSqr = maxRange * maxRange;
		List<WorkbenchBlockEntity> found = new ArrayList<>();
		for (WorkbenchBlockEntity be : workbenches.get(world)
			.values()) {
			if (be == null || be.isRemoved())
				continue;
			if (!be.canAccess(player))
				continue;
			if (distance(location, be.getBlockPos()) >= maxRangeSqr)
				continue;
			found.add(be);
		}
		found.sort(Comparator.comparingDouble(be -> distance(location, be.getBlockPos())));
		if (found.size() > maxAmount)
			return found.subList(0, maxAmount);
		return found;
	}

	public static boolean withinRange(Player player, WorkbenchBlockEntity be) {
		if (player.level() != be.getLevel())
			return false;
		double maxRange = getMaxRange(player);
		return distance(player.position(), be.getBlockPos()) < maxRange * maxRange;
	}

	public static double distance(Vec3 location, BlockPos pos) {
		return location.distanceToSqr(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
	}

	public static double getMaxRange(Player player) {
		return AllConfigs.server().equipment.toolboxRange.get()
			.doubleValue();
	}

	public static void syncData(ServerPlayer player) {
		CatnipServices.NETWORK.sendToClient(player, new PersistentDataPacket(player));
	}
}
