package dev.thecoolerzubumafu.createworkbench.gametest;

import dev.thecoolerzubumafu.createworkbench.AllBlocks;
import dev.thecoolerzubumafu.createworkbench.AllDataComponents;
import dev.thecoolerzubumafu.createworkbench.AllItems;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlockEntity;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchContents;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchContentsMenu;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchHandler;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchKeyItem;

import java.util.Map;
import java.util.UUID;

import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.StoredToolbox;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.ToolboxItems;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateWorkbench.ID)
@PrefixGameTestTemplate(false)
public class WorkbenchGameTests {

	@GameTest(template = "empty")
	public static void placedWorkbenchHasAnEmptyStorage(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());

		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		helper.assertTrue(be != null, "expected a workbench block entity");
		helper.assertTrue(be.storage()
			.size() == 0, "expected an empty storage");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void workbenchAcceptsAToolbox(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);

		boolean inserted = be.insertToolbox(com.simibubi.create.AllBlocks.TOOLBOXES.get(DyeColor.BLUE)
			.asStack());

		helper.assertTrue(inserted, "a toolbox should be accepted");
		helper.assertTrue(be.storage()
			.size() == 1, "storage should hold one toolbox");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void storedToolboxesPersist(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		be.insertToolbox(com.simibubi.create.AllBlocks.TOOLBOXES.get(DyeColor.ORANGE)
			.asStack());

		CompoundTag saved = be.saveWithoutMetadata(helper.getLevel()
			.registryAccess());
		WorkbenchBlockEntity restored = new WorkbenchBlockEntity(pos, be.getBlockState());
		restored.loadWithComponents(saved, helper.getLevel()
			.registryAccess());

		helper.assertTrue(restored.storage()
			.size() == 1, "stored toolboxes should survive a save/load round-trip");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void punchingAWorkbenchYieldsAnItemWithItsToolboxes(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		be.insertToolbox(com.simibubi.create.AllBlocks.TOOLBOXES.get(DyeColor.RED)
			.asStack());

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockState state = helper.getBlockState(pos);
		state.attack(helper.getLevel(), helper.absolutePos(pos), player);

		ItemStack held = player.getInventory()
			.getItem(0);
		helper.assertTrue(!held.isEmpty(), "expected a picked-up item");
		helper.assertTrue(held.is(AllBlocks.WORKBENCH.get()
			.asItem()), "expected the workbench item");
		WorkbenchContents contents = held.get(AllDataComponents.WORKBENCH_CONTENTS.get());
		helper.assertTrue(contents != null && contents.toolboxes()
			.size() == 1, "expected the stored toolbox to be on the item");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void placingAWorkbenchRestoresItsToolboxes(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 2));
		ItemStack item = new ItemStack(AllBlocks.WORKBENCH.get());
		item.set(AllDataComponents.WORKBENCH_CONTENTS.get(), new WorkbenchContents(
			Map.of(0, new StoredToolbox(inventory, DyeColor.CYAN, UUID.randomUUID()))));

		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		BlockState state = helper.getBlockState(pos);
		state.getBlock()
			.setPlacedBy(helper.getLevel(), helper.absolutePos(pos), state,
				helper.makeMockPlayer(GameType.SURVIVAL), item);

		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		helper.assertTrue(be.storage()
			.size() == 1, "expected the stored toolbox to be restored on placement");
		helper.assertTrue(be.storage()
			.get(0)
			.color() == DyeColor.CYAN, "expected the restored colour");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void workbenchMenuHasEightToolboxSlotsAndPlayerInventory(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		BlockState state = helper.getBlockState(pos);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		MenuProvider provider = state.getMenuProvider(helper.getLevel(), helper.absolutePos(pos));
		helper.assertTrue(provider != null, "expected the workbench to provide a menu");
		AbstractContainerMenu menu = provider.createMenu(0, player.getInventory(), player);

		helper.assertTrue(menu != null, "expected a menu instance");
		helper.assertTrue(menu.slots.size() == WorkbenchStorage.CAPACITY + 36,
			"expected 8 toolbox slots + 36 player slots, got " + menu.slots.size());
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void settingASlotStoresReplacesAndClearsWithoutLosingItems(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		BlockState state = helper.getBlockState(pos);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		MenuProvider provider = state.getMenuProvider(helper.getLevel(), helper.absolutePos(pos));
		AbstractContainerMenu menu = provider.createMenu(0, player.getInventory(), player);
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);

		ItemStack first = ToolboxItems.restore(
			new StoredToolbox(new ToolboxInventory(null), DyeColor.RED, UUID.randomUUID()));
		menu.getSlot(0)
			.set(first);
		helper.assertTrue(be.storage()
			.size() == 1, "placing through the slot should store the toolbox");

		ItemStack second = ToolboxItems.restore(
			new StoredToolbox(new ToolboxInventory(null), DyeColor.BLUE, UUID.randomUUID()));
		menu.getSlot(0)
			.set(second);
		helper.assertTrue(be.storage()
			.size() == 1, "replacing must not lose or duplicate the slot");
		helper.assertTrue(be.storage()
			.get(0)
			.color() == DyeColor.BLUE, "the slot should be replaced");

		menu.getSlot(0)
			.set(ItemStack.EMPTY);
		helper.assertTrue(be.storage()
			.size() == 0, "clearing the slot removes the toolbox");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void shiftClickStoresAndReturnsToolboxes(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		MenuProvider provider = helper.getBlockState(pos)
			.getMenuProvider(helper.getLevel(), helper.absolutePos(pos));
		AbstractContainerMenu menu = provider.createMenu(0, player.getInventory(), player);
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);

		ItemStack toolbox = ToolboxItems.restore(
			new StoredToolbox(new ToolboxInventory(null), DyeColor.RED, UUID.randomUUID()));
		menu.getSlot(8)
			.set(toolbox);

		menu.quickMoveStack(player, 8);
		helper.assertTrue(be.storage()
			.size() == 1, "shift-click should store the toolbox in the first free slot");
		helper.assertTrue(menu.getSlot(8)
			.getItem()
			.isEmpty(), "the player inventory slot should be emptied");

		menu.quickMoveStack(player, 0);
		helper.assertTrue(be.storage()
			.size() == 0, "shift-click should return the toolbox to the player inventory");
		boolean returned = false;
		for (int i = 8; i < menu.slots.size(); i++)
			if (!menu.getSlot(i)
				.getItem()
				.isEmpty())
				returned = true;
		helper.assertTrue(returned, "the toolbox should be back in the player inventory");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void openingAStoredToolboxExposesItsCompartments(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
		be.storage()
			.setAt(0, new StoredToolbox(inventory, DyeColor.LIME, UUID.randomUUID()));

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		WorkbenchContentsMenu menu = be.createContentsMenu(0, player.getInventory(), 0);

		helper.assertTrue(menu != null, "expected a contents menu for the stored toolbox");
		helper.assertTrue(menu.slots.size() == 68,
			"expected Create's native toolbox layout (32 compartment + 36 inventory slots), got " + menu.slots.size());
		helper.assertTrue(menu.getSlot(0)
			.getItem()
			.is(Items.DIAMOND), "compartment 0 should show the stored item");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void openingAStoredToolboxUsesItsName(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		Component name = Component.literal("My Tools");
		be.storage()
			.setAt(0, new StoredToolbox(new ToolboxInventory(null), DyeColor.LIME, UUID.randomUUID(), name));

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		WorkbenchContentsMenu menu = be.createContentsMenu(0, player.getInventory(), 0);

		helper.assertTrue(menu != null, "expected a contents menu");
		helper.assertTrue(menu.contentHolder.getDisplayName()
			.getString()
			.equals("My Tools"), "expected the toolbox name to title the screen");
		helper.assertTrue(menu.contentHolder.getBlockPos()
			.equals(helper.absolutePos(pos)), "expected the contents holder at the workbench position");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void lockingRequiresAMatchingKey(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);

		ItemStack key = new ItemStack(AllItems.WORKBENCH_KEY.get());
		helper.assertTrue(be.toggleLock(key), "sneak-right-click with a key should lock an unlocked workbench");
		helper.assertTrue(be.isLocked(), "the workbench should now be locked");
		helper.assertTrue(WorkbenchKeyItem.lockIdOf(key) != null, "the key should receive the new lock id");

		helper.assertTrue(!be.canAccess(helper.makeMockPlayer(GameType.SURVIVAL)),
			"a player without the key must be denied");

		Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
		holder.getInventory()
			.setItem(0, key);
		helper.assertTrue(be.canAccess(holder), "the key holder must be granted access");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void matchingKeyUnlocksAGain(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);

		ItemStack key = new ItemStack(AllItems.WORKBENCH_KEY.get());
		be.toggleLock(key);

		helper.assertTrue(be.toggleLock(key), "the matching key should unlock the workbench");
		helper.assertTrue(!be.isLocked(), "the workbench should be unlocked");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void aDifferentKeyCannotUnlock(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);

		ItemStack key = new ItemStack(AllItems.WORKBENCH_KEY.get());
		be.toggleLock(key);

		ItemStack otherKey = new ItemStack(AllItems.WORKBENCH_KEY.get());
		WorkbenchKeyItem.setLockId(otherKey, UUID.randomUUID());
		helper.assertTrue(!be.toggleLock(otherKey), "a non-matching key must not toggle the lock");
		helper.assertTrue(be.isLocked(), "the workbench should stay locked");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void lockPersistsAcrossReload(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		UUID lockId = UUID.randomUUID();
		be.restoreLock(lockId);

		CompoundTag saved = be.saveWithoutMetadata(helper.getLevel()
			.registryAccess());
		WorkbenchBlockEntity restored = new WorkbenchBlockEntity(pos, be.getBlockState());
		restored.loadWithComponents(saved, helper.getLevel()
			.registryAccess());

		helper.assertTrue(restored.isLocked(), "the lock should survive a save/load round-trip");
		helper.assertTrue(restored.grantsAccess(lockId), "the lock id should be retained");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void restockReplenishesTheBoundHotbarSlot(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 64));
		be.storage()
			.setAt(0, new StoredToolbox(inventory, DyeColor.BLUE, UUID.randomUUID()));

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory()
			.setItem(0, ItemStack.EMPTY);

		helper.assertTrue(!WorkbenchHandler.restock(player, be, 0, 0, 0),
			"a still-valid binding should stay bound");
		ItemStack hotbar = player.getInventory()
			.getItem(0);
		helper.assertTrue(hotbar.is(Items.DIAMOND) && hotbar.getCount() == 32,
			"expected the bound slot to be topped up to half a stack, got " + hotbar);
		helper.assertTrue(be.storage()
			.get(0)
			.inventory()
			.getStackInSlot(0)
			.getCount() == 32, "expected the compartment to be drained in step");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void restockDropsBindingsToMissingToolboxes(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		helper.assertTrue(WorkbenchHandler.restock(player, be, 3, 0, 0),
			"a binding to an empty Slot has nothing to restock and should be dropped");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void depositFillsDefinedCompartments(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 1));
		be.storage()
			.setAt(0, new StoredToolbox(inventory, DyeColor.BLUE, UUID.randomUUID()));

		ItemStack remainder = WorkbenchHandler.deposit(be, new ItemStack(Items.DIAMOND, 10));

		helper.assertTrue(remainder.isEmpty(), "the defined compartment should accept all ten diamonds");
		helper.assertTrue(be.storage()
			.get(0)
			.inventory()
			.getStackInSlot(0)
			.getCount() == 11, "the compartment should hold the deposited diamonds");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void nearestListsOnlyAccessibleWorkbenches(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		WorkbenchHandler.onLoad(be);

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos abs = helper.absolutePos(pos);
		player.setPos(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5);
		helper.assertTrue(WorkbenchHandler.getNearest(player.level(), player, 8)
			.contains(be), "an unlocked workbench should be listed");

		UUID lockId = UUID.randomUUID();
		be.restoreLock(lockId);
		helper.assertTrue(!WorkbenchHandler.getNearest(player.level(), player, 8)
			.contains(be), "a locked workbench is hidden from players without the key");

		ItemStack key = new ItemStack(AllItems.WORKBENCH_KEY.get());
		WorkbenchKeyItem.setLockId(key, lockId);
		player.getInventory()
			.setItem(0, key);
		helper.assertTrue(WorkbenchHandler.getNearest(player.level(), player, 8)
			.contains(be), "the key holder should see the locked workbench");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void breakingReturnsBoundItemsToTheirCompartment(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		helper.setBlock(pos, AllBlocks.WORKBENCH.get());
		WorkbenchBlockEntity be = (WorkbenchBlockEntity) helper.getBlockEntity(pos);
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 32));
		be.storage()
			.setAt(0, new StoredToolbox(inventory, DyeColor.BLUE, UUID.randomUUID()));

		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory()
			.setItem(0, new ItemStack(Items.DIAMOND, 32));
		BlockPos abs = helper.absolutePos(pos);
		CompoundTag data = new CompoundTag();
		data.putInt("Slot", 0);
		data.putInt("Compartment", 0);
		data.put("Pos", NbtUtils.writeBlockPos(abs));
		CompoundTag root = new CompoundTag();
		root.put("0", data);
		player.getPersistentData()
			.put(WorkbenchHandler.DATA_KEY, root);

		helper.assertTrue(WorkbenchHandler.unequipTracked(player, abs),
			"the bound player should be unequipped");
		helper.assertTrue(player.getInventory()
			.getItem(0)
			.isEmpty(), "the bound hotbar slot should be emptied");
		helper.assertTrue(be.storage()
			.get(0)
			.inventory()
			.takeFromCompartment(64, 0, true)
			.getCount() == 64, "the deposited stack should be back in the compartment");
		helper.assertTrue(player.getPersistentData()
			.getCompound(WorkbenchHandler.DATA_KEY)
			.isEmpty(), "the binding should be cleared");

		ItemStack clone = be.getBlockState()
			.getBlock()
			.getCloneItemStack(helper.getLevel(), abs, be.getBlockState());
		WorkbenchContents contents = clone.get(AllDataComponents.WORKBENCH_CONTENTS.get());
		helper.assertTrue(contents != null && contents.toolboxes()
			.get(0)
			.inventory()
			.takeFromCompartment(64, 0, true)
			.getCount() == 64, "the picked-up workbench should carry the returned items");
		helper.succeed();
	}
}
