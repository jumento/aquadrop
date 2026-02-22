package com.aquadrop.core.systems;

import com.aquadrop.api.CustomDropRegistry;
import com.aquadrop.api.models.DropConfig;
import com.aquadrop.core.services.ProbabilityService;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class MobDropSystem extends DeathSystems.OnDeathSystem {

    private final CustomDropRegistry registry;
    private final ProbabilityService probabilityService;

    public MobDropSystem(CustomDropRegistry registry, ProbabilityService probabilityService) {
        this.registry = registry;
        this.probabilityService = probabilityService;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(DeathComponent.getComponentType(), NPCEntity.getComponentType());
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent deathComp,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        NPCEntity npc = store.getComponent(ref, java.util.Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npc == null)
            return;

        String typeId = npc.getNPCTypeId();
        if (typeId == null)
            return;

        List<DropConfig> drops = registry.getMobDrops(typeId);
        if (drops.isEmpty())
            return;

        for (DropConfig config : drops) {
            if (probabilityService.shouldDrop(config.probability())) {
                dropItem(ref, config.dropId(), store, commandBuffer);
            }
        }
    }

    private void dropItem(Ref<EntityStore> ref, String dropId, Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer) {
        try {
            TransformComponent transform = store.getComponent(ref,
                    java.util.Objects.requireNonNull(TransformComponent.getComponentType()));
            if (transform == null)
                return;

            Vector3d pos = transform.getPosition();
            Vector3d dropPos = new Vector3d(pos.x, pos.y + 0.5, pos.z);

            Constructor<?> ctor = ItemStack.class.getConstructor(String.class, int.class);
            ItemStack dropItem = (ItemStack) ctor.newInstance(dropId, 1);

            Holder<EntityStore>[] holders = ItemComponent.generateItemDrops(commandBuffer,
                    Collections.singletonList(dropItem), dropPos,
                    new Vector3f(0f, 0.1f, 0f));

            if (holders != null) {
                for (Holder<EntityStore> h : holders) {
                    commandBuffer.addEntity(h, AddReason.SPAWN);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
