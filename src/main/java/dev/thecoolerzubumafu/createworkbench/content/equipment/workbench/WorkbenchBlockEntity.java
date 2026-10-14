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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WorkbenchBlockEntity extends BlockEntity {
    private final WorkbenchStorage storage = new WorkbenchStorage();
    private final WorkbenchItemHandler itemHandler = new WorkbenchItemHandler(storage, this::notifyStorageChanged);
    private final WorkbenchLock lock = new WorkbenchLock();

    public final LerpedFloat lidOpen = LerpedFloat.linear()
            .startWithValue(0);
    public final LerpedFloat drawerOpen = LerpedFloat.linear()
            .startWithValue(0);

    private int openCount;
    private int openCheck;

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.WORKBENCH_BLOCKENTITY.get(), pos, state);
    }

    public void tick() {
        if (level == null)
            return;

        if (!level.isClientSide && ++openCheck % 10 == 0) {
            int previous = openCount;
            openCount = countOpenMenus();
            if (previous != openCount)
                sendOpenState();
        }

        if (level.isClientSide)
            tickAudio();

        lidOpen.chase(openCount > 0 ? 1 : 0, openCount > 0 ? 0.25f : 0.5f, Chaser.EXP);
        drawerOpen.chase(openCount > 0 ? 1 : 0, openCount > 0 ? 0.19f : 0.38f, Chaser.LINEAR);
        lidOpen.tickChaser();
        drawerOpen.tickChaser();
    }

    private void tickAudio() {
        Vec3 vec = Vec3.atCenterOf(worldPosition);
        if (lidOpen.settled()) {
            if (openCount > 0 && lidOpen.getChaseTarget() == 0) {
                level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.25F,
                        level.random.nextFloat() * 0.1F + 1.2F, true);
                level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.1F,
                        level.random.nextFloat() * 0.1F + 1.1F, true);
            }
            if (openCount == 0 && lidOpen.getChaseTarget() == 1)
                level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.1F,
                        level.random.nextFloat() * 0.1F + 1.1F, true);
        } else if (openCount == 0 && lidOpen.getChaseTarget() == 0 && lidOpen.getValue(0) > 1 / 16f
                && lidOpen.getValue(1) < 1 / 16f)
            level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.25F,
                    level.random.nextFloat() * 0.1F + 1.2F, true);
    }

    private int countOpenMenus() {
        int count = 0;
        for (Player player : level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(8)))
            if (player.containerMenu instanceof WorkbenchMenu menu && menu.workbench() == this)
                count++;
        return count;
    }

    public void startOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator())
            return;
        if (openCount < 0)
            openCount = 0;
        openCount++;
        sendOpenState();
    }

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
