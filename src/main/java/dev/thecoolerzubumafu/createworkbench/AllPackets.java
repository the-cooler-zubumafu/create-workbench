package dev.thecoolerzubumafu.createworkbench;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class AllPackets {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                OpenWorkbenchToolboxPacket.TYPE,
                OpenWorkbenchToolboxPacket.STREAM_CODEC,
                OpenWorkbenchToolboxPacket::handle
        );
        registrar.playToServer(
                OpenWorkbenchMenuPacket.TYPE,
                OpenWorkbenchMenuPacket.STREAM_CODEC,
                OpenWorkbenchMenuPacket::handle
        );
        registrar.playToServer(
                WorkbenchEquipPacket.TYPE,
                WorkbenchEquipPacket.STREAM_CODEC,
                WorkbenchEquipPacket::handle
        );
        registrar.playToServer(
                WorkbenchDisposeAllPacket.TYPE,
                WorkbenchDisposeAllPacket.STREAM_CODEC,
                WorkbenchDisposeAllPacket::handle
        );
    }

}
