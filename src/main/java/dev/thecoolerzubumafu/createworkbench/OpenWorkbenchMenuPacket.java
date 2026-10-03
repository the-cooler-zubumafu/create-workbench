package dev.thecoolerzubumafu.createworkbench;

import dev.thecoolerzubumafu.createworkbench.content.equipment.workbench.WorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenWorkbenchMenuPacket(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenWorkbenchMenuPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(CreateWorkbench.ID, "open_workbench_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWorkbenchMenuPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenWorkbenchMenuPacket::pos,
                    OpenWorkbenchMenuPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenWorkbenchMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (!(player.level()
                    .getBlockEntity(packet.pos()) instanceof WorkbenchBlockEntity workbench))
                return;
            workbench.openMenu(player);
        });
    }
}
