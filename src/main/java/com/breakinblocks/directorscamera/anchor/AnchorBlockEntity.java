package com.breakinblocks.directorscamera.anchor;

import com.breakinblocks.directorscamera.cutscene.CutsceneFrame;
import com.breakinblocks.directorscamera.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AnchorBlockEntity extends BlockEntity {
    private String anchorId = "";
    private String cutsceneId = "";
    private double triggerRadius;
    private boolean triggerOnce = true;
    private int cooldownTicks;
    private final Set<UUID> triggered = new HashSet<>();
    private final Map<UUID, Long> lastTriggered = new HashMap<>();

    public AnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANCHOR.get(), pos, state);
    }

    public String getAnchorId() {
        return anchorId;
    }

    public String getCutsceneId() {
        return cutsceneId;
    }

    public double getTriggerRadius() {
        return triggerRadius;
    }

    public boolean isTriggerOnce() {
        return triggerOnce;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public boolean hasTrigger() {
        return !cutsceneId.isEmpty() && triggerRadius > 0;
    }

    public Direction getFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(AnchorBlock.FACING) ? state.getValue(AnchorBlock.FACING) : Direction.SOUTH;
    }

    public float getYaw() {
        return getFacing().toYRot();
    }

    public CutsceneFrame getFrame() {
        BlockPos pos = getBlockPos();
        return new CutsceneFrame(new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), getYaw());
    }

    public void setAnchorId(String id) {
        String previous = anchorId;
        anchorId = id == null ? "" : id.trim();
        AnchorIndex.refresh(this, previous);
        sync();
    }

    public void setTrigger(String cutscene, double radius, boolean once, int cooldown) {
        cutsceneId = cutscene == null ? "" : cutscene.trim();
        triggerRadius = Math.max(0, radius);
        triggerOnce = once;
        cooldownTicks = Math.max(0, cooldown);
        AnchorIndex.refresh(this, anchorId);
        sync();
    }

    public void clearTrigger() {
        setTrigger("", 0, true, 0);
    }

    public void resetTriggered() {
        triggered.clear();
        lastTriggered.clear();
        setChanged();
    }

    public boolean canTrigger(UUID player, long gameTime) {
        if (triggerOnce && triggered.contains(player)) {
            return false;
        }
        Long last = lastTriggered.get(player);
        return last == null || gameTime - last >= cooldownTicks;
    }

    public void markTriggered(UUID player, long gameTime) {
        triggered.add(player);
        lastTriggered.put(player, gameTime);
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AnchorIndex.register(this);
    }

    @Override
    public void setRemoved() {
        AnchorIndex.unregister(this, anchorId);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("anchorId", anchorId);
        output.putString("cutsceneId", cutsceneId);
        output.putDouble("triggerRadius", triggerRadius);
        output.putBoolean("triggerOnce", triggerOnce);
        output.putInt("cooldownTicks", cooldownTicks);
        output.store("triggered", UUIDUtil.CODEC.listOf(), List.copyOf(triggered));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        anchorId = input.getStringOr("anchorId", "");
        cutsceneId = input.getStringOr("cutsceneId", "");
        triggerRadius = input.getDoubleOr("triggerRadius", 0);
        triggerOnce = input.getBooleanOr("triggerOnce", true);
        cooldownTicks = input.getIntOr("cooldownTicks", 0);
        triggered.clear();
        input.read("triggered", UUIDUtil.CODEC.listOf()).ifPresent(triggered::addAll);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}
