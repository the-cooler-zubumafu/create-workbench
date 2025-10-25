package dev.thecoolerzubumafu.createworkbench;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AllBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateWorkbench.MOD_ID);

    public static final DeferredBlock<Block> WORKBENCH = registerBlock(
            Blocks.WORKBENCH.value,
            () -> new Block(
                    BlockBehaviour.Properties
                            .of()
                            .instabreak()
                            .sound(SoundType.CHERRY_WOOD)
            )
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        registerBlockItem(name, deferredBlock);
        return deferredBlock;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        AllItems.ITEMS.register(
                name,
                () -> new BlockItem(
                        block.get(),
                        new Item.Properties()
                                .stacksTo(1)
                )
        );
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    enum Blocks {
        WORKBENCH("workbench");

        private final String value;

        Blocks(String value) {
            this.value = value;
        }
    }

}
