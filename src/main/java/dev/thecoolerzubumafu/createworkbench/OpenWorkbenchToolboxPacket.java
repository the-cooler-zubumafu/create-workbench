package dev.thecoolerzubumafu.createworkbench;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenWorkbenchToolboxPacket(BlockPos pos, int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenWorkbenchToolboxPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "open_workbench_toolbox"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWorkbenchToolboxPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenWorkbenchToolboxPacket::pos,
                    ByteBufCodecs.VAR_INT, OpenWorkbenchToolboxPacket::slot,
                    OpenWorkbenchToolboxPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenWorkbenchToolboxPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (!(player.level()
                    .getBlockEntity(packet.pos()) instanceof WorkbenchBlockEntity workbench))
                return;
            workbench.openStoredToolbox(player, packet.slot());
        });
    }
}
