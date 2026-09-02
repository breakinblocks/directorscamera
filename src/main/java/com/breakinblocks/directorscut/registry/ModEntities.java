package com.breakinblocks.directorscut.registry;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.camera.ClientCameraEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, DirectorsCut.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ClientCameraEntity>> CLIENT_CAMERA = ENTITY_TYPES.register("client_camera",
        () -> EntityType.Builder.<ClientCameraEntity>of(ClientCameraEntity::new, MobCategory.MISC)
            .updateInterval(1)
            .sized(0.2F, 0.2F)
            .eyeHeight(0.0F)
            .noSave()
            .noSummon()
            .build("client_camera"));

    @SubscribeEvent
    public static void addAttributes(EntityAttributeCreationEvent event) {
        event.put(CLIENT_CAMERA.get(), LivingEntity.createLivingAttributes().build());
    }
}
