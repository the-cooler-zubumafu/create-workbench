package dev.thecoolerzubumafu.createworkbench;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlock;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlockEntity;
import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchHandler;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorkbenchDisposeAllPacket(BlockPos workbenchPos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WorkbenchDisposeAllPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "workbench_dispose_all"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchDisposeAllPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, WorkbenchDisposeAllPacket::workbenchPos,
                    WorkbenchDisposeAllPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WorkbenchDisposeAllPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (!(player.level()
                    .getBlockEntity(packet.workbenchPos()) instanceof WorkbenchBlockEntity workbench))
                return;
            if (!WorkbenchHandler.withinRange(player, workbench))
                return;
            if (!workbench.canAccess(player)) {
                player.displayClientMessage(Component.translatable(WorkbenchBlock.LOCKED_KEY), true);
                return;
            }

            CompoundTag compound = player.getPersistentData()
                    .getCompound(WorkbenchHandler.DATA_KEY);
            boolean sendData = false;

            for (int i = 0; i < 36; i++) {
                String key = String.valueOf(i);
                if (compound.contains(key)
                        && NBTHelper.readBlockPos(compound.getCompound(key), "Pos").equals(packet.workbenchPos())) {
                    compound.remove(key);
                    sendData = true;
                }

                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty())
                    continue;
                ItemStack remainder = WorkbenchHandler.deposit(workbench, stack);
                if (remainder.getCount() != stack.getCount())
                    player.getInventory().setItem(i, remainder);
            }

            if (sendData)
                WorkbenchHandler.syncData(player);
        });
    }
}
