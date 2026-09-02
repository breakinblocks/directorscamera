package com.breakinblocks.directorscamera.camera;

import com.breakinblocks.directorscamera.registry.ModEntities;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ClientCameraEntity extends LivingEntity {
    @Nullable
    private Player viewer;

    public ClientCameraEntity(EntityType<ClientCameraEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public ClientCameraEntity(Level level, @Nullable Player viewer) {
        this(ModEntities.CLIENT_CAMERA.get(), level);
        this.viewer = viewer;
    }

    public void setViewer(@Nullable Player viewer) {
        this.viewer = viewer;
    }

    public void moveTo(Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
        rememberPreviousPosition();
    }

    public void rememberPreviousPosition() {
        xo = getX();
        yo = getY();
        zo = getZ();
        xOld = getX();
        yOld = getY();
        zOld = getZ();
    }

    @Override
    public void tick() {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.LEFT;
    }

    @Override
    public boolean hasEffect(Holder<MobEffect> effect) {
        return viewer != null && viewer.hasEffect(effect);
    }

    @Nullable
    @Override
    public MobEffectInstance getEffect(Holder<MobEffect> effect) {
        return viewer == null ? null : viewer.getEffect(effect);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return false;
    }
}
