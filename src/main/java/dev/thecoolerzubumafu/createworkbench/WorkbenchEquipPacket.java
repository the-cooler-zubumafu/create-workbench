package dev.thecoolerzubumafu.createworkbench;

import com.simibubi.create.content.equipment.toolbox.ItemReturnInvWrapper;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.StoredToolbox;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlock;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlockEntity;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchHandler;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorkbenchEquipPacket(BlockPos workbenchPos, int slot, int compartment, int hotbarSlot)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WorkbenchEquipPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "workbench_equip"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchEquipPacket> STREAM_CODEC =
            StreamCodec.composite(
                    CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), WorkbenchEquipPacket::workbenchPos,
                    ByteBufCodecs.VAR_INT, WorkbenchEquipPacket::slot,
                    ByteBufCodecs.VAR_INT, WorkbenchEquipPacket::compartment,
                    ByteBufCodecs.VAR_INT, WorkbenchEquipPacket::hotbarSlot,
                    WorkbenchEquipPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WorkbenchEquipPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (packet.workbenchPos() == null) {
                WorkbenchHandler.unequip(player, packet.hotbarSlot(), false);
                WorkbenchHandler.syncData(player);
                return;
            }
            if (!(player.level()
                    .getBlockEntity(packet.workbenchPos()) instanceof WorkbenchBlockEntity workbench))
                return;
            if (!WorkbenchHandler.withinRange(player, workbench)) {
                WorkbenchHandler.syncData(player);
                return;
            }
            if (!workbench.canAccess(player)) {
                player.displayClientMessage(Component.translatable(WorkbenchBlock.LOCKED_KEY), true);
                return;
            }
            WorkbenchHandler.unequip(player, packet.hotbarSlot(), false);

            if (packet.slot() < 0 || packet.slot() >= WorkbenchStorage.CAPACITY
                    || packet.compartment() < 0 || packet.compartment() >= 8) {
                WorkbenchHandler.syncData(player);
                return;
            }
            StoredToolbox stored = workbench.storage().get(packet.slot());
            if (stored == null)
                return;

            ItemStack playerStack = player.getInventory().getItem(packet.hotbarSlot());
            if (!playerStack.isEmpty()) {
                ItemStack reference = stored.inventory()
                        .getStackInSlot(packet.compartment() * ToolboxInventory.STACKS_PER_COMPARTMENT);
                if (!ToolboxInventory.canItemsShareCompartment(playerStack, reference)) {
                    stored.inventory().inLimitedMode(inventory -> {
                        ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, playerStack, false);
                        if (!remainder.isEmpty())
                            remainder = ItemHandlerHelper.insertItemStacked(
                                    new ItemReturnInvWrapper(player.getInventory()), remainder, false);
                        if (remainder.getCount() != playerStack.getCount())
                            player.getInventory().setItem(packet.hotbarSlot(), remainder);
                    });
                    workbench.notifyStorageChanged();
                }
            }

            CompoundTag compound = player.getPersistentData()
                    .getCompound(WorkbenchHandler.DATA_KEY);
            CompoundTag data = new CompoundTag();
            data.putInt("Slot", packet.slot());
            data.putInt("Compartment", packet.compartment());
            data.put("Pos", NbtUtils.writeBlockPos(packet.workbenchPos()));
            compound.put(String.valueOf(packet.hotbarSlot()), data);
            player.getPersistentData().put(WorkbenchHandler.DATA_KEY, compound);

            WorkbenchHandler.syncData(player);
        });
    }
}
