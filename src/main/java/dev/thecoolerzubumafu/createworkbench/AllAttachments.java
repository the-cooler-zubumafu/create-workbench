package dev.thecoolerzubumafu.createworkbench;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AllAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CreateWorkbench.ID);

    public static final Supplier<AttachmentType<List<ItemStack>>> SOULBOUND_STASH =
            ATTACHMENT_TYPES.register(
                    "soulbound_stash",
                    () -> AttachmentType.<List<ItemStack>>builder(() -> new ArrayList<ItemStack>())
                            .serialize(ItemStack.CODEC.listOf())
                            .build()
            );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
