package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;

import dev.thecoolerzubumafu.createworkbench.AllBlockEntities;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public class WorkbenchBlockEntity extends BlockEntity {

    private final WorkbenchStorage storage = new WorkbenchStorage();
    private final WorkbenchItemHandler itemHandler = new WorkbenchItemHandler(storage, this::notifyStorageChanged);
    private final WorkbenchLock lock = new WorkbenchLock();

    public final LerpedFloat lidOpen = LerpedFloat.linear()
            .startWithValue(0);
    public final LerpedFloat drawerLeftOpen = LerpedFloat.linear()
            .startWithValue(0);
    public final LerpedFloat drawerRightOpen = LerpedFloat.linear()
            .startWithValue(0);

    private int openCount;
    private int openCheck;

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.WORKBENCH_BLOCKENTITY.get(), pos, state);
    }

    /**
     * Chases the lid/drawer animation toward whether any player currently has this
     * Workbench's menu open. The server re-derives the open count on a slow timer so
     * menu closes on disconnect or unload can never leave it stuck open.
     */
    public void tick() {
        if (level == null)
            return;

        if (!level.isClientSide && ++openCheck % 10 == 0) {
            int previous = openCount;
            openCount = countOpenMenus();
            if (previous != openCount)
                sendOpenState();
        }

        lidOpen.chase(openCount > 0 ? 1 : 0, 0.25f, Chaser.EXP);
        drawerLeftOpen.chase(openCount > 0 ? 1 : 0, 0.22f, Chaser.LINEAR);
        drawerRightOpen.chase(openCount > 0 ? 1 : 0, 0.16f, Chaser.LINEAR);
        lidOpen.tickChaser();
        drawerLeftOpen.tickChaser();
        drawerRightOpen.tickChaser();
    }

    private int countOpenMenus() {
        int count = 0;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(8)))
            if (player.containerMenu instanceof WorkbenchMenu menu && menu.workbench() == this)
                count++;
        return count;
    }

    /** Called when a player opens the Workbench menu; opens the cart. */
    public void startOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator())
            return;
        if (openCount < 0)
            openCount = 0;
        openCount++;
        sendOpenState();
    }

    /** Called when a player closes the Workbench menu; retracts the cart when none remain. */
    public void stopOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator())
            return;
        openCount = Math.max(0, openCount - 1);
        sendOpenState();
    }

    private void sendOpenState() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public WorkbenchStorage storage() {
        return storage;
    }

    public WorkbenchItemHandler itemHandler() {
        return itemHandler;
    }

    public WorkbenchLock lock() {
        return lock;
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public boolean grantsAccess(UUID keyId) {
        return lock.grantsAccess(keyId);
    }

    public boolean hasMatchingKey(Player player) {
        ItemStack key = WorkbenchKeyItem.find(player);
        return !key.isEmpty() && grantsAccess(WorkbenchKeyItem.lockIdOf(key));
    }

    public boolean canAccess(Player player) {
        return !isLocked() || hasMatchingKey(player);
    }

    public void restoreLock(UUID lockId) {
        if (lockId == null)
            lock.unlock();
        else
            lock.lock(lockId);
        notifyStorageChanged();
    }

    /**
     * Sneak-right-click with a Workbench Key toggles the lock. Returns true when the
     * state flipped: an unlocked Workbench locks (issuing a fresh Lock ID to the key),
     * a locked one unlocks when the key matches.
     */
    public boolean toggleLock(ItemStack key) {
        if (!(key.getItem() instanceof WorkbenchKeyItem))
            return false;
        if (!lock.isLocked()) {
            UUID lockId = UUID.randomUUID();
            lock.lock(lockId);
            WorkbenchKeyItem.setLockId(key, lockId);
            notifyStorageChanged();
            return true;
        }
        if (lock.grantsAccess(WorkbenchKeyItem.lockIdOf(key))) {
            lock.unlock();
            notifyStorageChanged();
            return true;
        }
        return false;
    }

    public boolean insertToolbox(ItemStack item) {
        boolean inserted = storage.insert(item);
        if (inserted) {
            notifyStorageChanged();
            CreateWorkbench.LOGGER.debug("Workbench at {} stored a toolbox", worldPosition);
        }
        return inserted;
    }

    public void readContents(WorkbenchContents contents) {
        storage.setContents(contents);
        notifyStorageChanged();
        CreateWorkbench.LOGGER.debug("Workbench at {} restored {} toolboxes", worldPosition,
                contents.toolboxes().size());
    }

    /**
     * Marks the BE dirty and pushes its contents to nearby clients (used by restock,
     * contents edit and lock changes so rendering/bindings stay in sync).
     */
    public void notifyStorageChanged() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        WorkbenchHandler.onLoad(this);
    }

    @Override
    public void onChunkUnloaded() {
        WorkbenchHandler.onUnload(this);
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        WorkbenchHandler.onUnload(this);
        super.setRemoved();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void openMenu(ServerPlayer player) {
        startOpen(player);
        player.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new WorkbenchMenu(id, inventory, this,
                                ContainerLevelAccess.create(level, worldPosition)),
                        Component.translatable("block.createworkbench.workbench")),
                buffer -> buffer.writeBlockPos(worldPosition));
    }

    public WorkbenchContentsMenu createContentsMenu(int id, Inventory inventory, int slot) {
        StoredToolbox stored = storage.get(slot);
        if (stored == null)
            return null;
        return new WorkbenchContentsMenu(id, inventory, buildHolder(stored), this, slot, stored);
    }

    public void openStoredToolbox(ServerPlayer player, int slot) {
        StoredToolbox stored = storage.get(slot);
        if (stored == null)
            return;
        ToolboxBlockEntity holder = buildHolder(stored);
        CompoundTag tag = holder.saveWithoutMetadata(player.level()
                .registryAccess());
        player.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new WorkbenchContentsMenu(id, inventory, holder, this, slot, stored),
                        holder.getDisplayName()),
                buffer -> {
                    buffer.writeBlockPos(worldPosition);
                    buffer.writeNbt(tag);
                    buffer.writeVarInt(stored.color()
                            .getId());
                });
    }

    private ToolboxBlockEntity buildHolder(StoredToolbox stored) {
        ToolboxBlockEntity holder = new ToolboxBlockEntity(AllBlockEntityTypes.TOOLBOX.get(), worldPosition,
                AllBlocks.TOOLBOXES.get(stored.color())
                        .get()
                        .defaultBlockState());
        holder.setLevel(level);
        holder.readInventory(stored.inventory());
        if (stored.customName() != null)
            holder.setCustomName(stored.customName());
        return holder;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Storage", storage.serializeNBT(registries));
        tag.putInt("OpenCount", openCount);
        if (lock.isLocked())
            tag.putUUID("LockId", lock.lockId());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storage.deserializeNBT(registries, tag.getCompound("Storage"));
        openCount = tag.getInt("OpenCount");
        if (tag.hasUUID("LockId"))
            lock.lock(tag.getUUID("LockId"));
        else
            lock.unlock();
    }
}
