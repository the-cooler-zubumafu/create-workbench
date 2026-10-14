package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.ArrayList;
import java.util.List;

import dev.thecoolerzubumafu.createworkbench.AllAttachments;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = CreateWorkbench.ID)
public final class HealthInsuranceHandler {
	public static final ResourceKey<Enchantment> HEALTH_INSURANCE =
		ResourceKey.create(Registries.ENCHANTMENT,
			ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "health_insurance"));

	private static boolean splitting;

	private HealthInsuranceHandler() {
	}

	public static boolean isHealthInsurance(Holder<Enchantment> holder) {
		return holder.unwrapKey()
			.map(HEALTH_INSURANCE::equals)
			.orElse(false);
	}

	public static boolean isProtected(ItemStack stack) {
		for (Holder<Enchantment> holder : stack.getEnchantments()
			.keySet())
			if (isHealthInsurance(holder))
				return true;
		return false;
	}

	public static boolean isHealthInsuranceBook(ItemStack stack) {
		if (!stack.is(Items.ENCHANTED_BOOK))
			return false;
		ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		for (Holder<Enchantment> holder : stored.keySet())
			if (isHealthInsurance(holder))
				return true;
		return false;
	}

	public static void applyHealthInsurance(ItemStack stack, HolderLookup.Provider registries) {
		registries.lookupOrThrow(Registries.ENCHANTMENT)
			.get(HEALTH_INSURANCE)
			.ifPresent(holder -> {
				ItemEnchantments.Mutable mutable =
					new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
				mutable.set(holder, 1);
				stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
			});
	}

	public static ItemStack healthInsuranceBook(HolderLookup.Provider registries) {
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		registries.lookupOrThrow(Registries.ENCHANTMENT)
			.get(HEALTH_INSURANCE)
			.ifPresent(holder -> {
				ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
				mutable.set(holder, 1);
				book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
			});
		return book;
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.isSpectator())
			return;
		List<ItemStack> saved = new ArrayList<>();
		var iterator = event.getDrops()
			.iterator();
		while (iterator.hasNext()) {
			ItemEntity drop = iterator.next();
			if (isProtected(drop.getItem())) {
				saved.add(drop.getItem()
					.copy());
				iterator.remove();
			}
		}
		if (!saved.isEmpty())
			player.setData(AllAttachments.SOULBOUND_STASH.get(), saved);
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		if (!event.isWasDeath())
			return;
		Player original = event.getOriginal();
		List<ItemStack> saved = original.getData(AllAttachments.SOULBOUND_STASH.get());
		if (saved.isEmpty())
			return;
		for (ItemStack stack : saved)
			event.getEntity()
				.getInventory()
				.placeItemBackInInventory(stack);
		original.setData(AllAttachments.SOULBOUND_STASH.get(), new ArrayList<>());
	}

	@SubscribeEvent
	public static void onAnvilUpdate(AnvilUpdateEvent event) {
		if (splitting)
			return;
		ItemStack left = event.getLeft();
		if (left.getCount() <= 1 || !(left.getItem() instanceof WorkbenchKeyItem))
			return;
		if (!isHealthInsuranceBook(event.getRight()))
			return;
		Player player = event.getPlayer();
		if (player.level().isClientSide || !(player.containerMenu instanceof AnvilMenu menu))
			return;

		splitting = true;
		try {
			ItemStack rest = left.copyWithCount(left.getCount() - 1);
			menu.getSlot(0)
				.set(left.copyWithCount(1));
			player.getInventory()
				.placeItemBackInInventory(rest);
		} finally {
			splitting = false;
		}

		ItemStack output = left.copyWithCount(1);
		applyHealthInsurance(output, player.level()
			.registryAccess());
		event.setOutput(output);
		event.setCost(1);
		event.setMaterialCost(1);
	}

	@SubscribeEvent
	public static void onVillagerTrades(VillagerTradesEvent event) {
		if (event.getType() != VillagerProfession.LIBRARIAN)
			return;
		event.getTrades()
			.get(1)
			.add(new BasicItemListing(30, healthInsuranceBook(event.getRegistryAccess()), 1, 30));
	}
}
