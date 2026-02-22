package com.aquadrop.core.systems;

import com.aquadrop.api.CustomDropRegistry;
import com.aquadrop.api.models.DropConfig;
import com.aquadrop.core.services.ProbabilityService;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDropSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final CustomDropRegistry registry;
    private final ProbabilityService probabilityService;

    public BlockDropSystem(CustomDropRegistry registry, ProbabilityService probabilityService) {
        super(BreakBlockEvent.class);
        this.registry = registry;
        this.probabilityService = probabilityService;
    }

    @Override
    public void handle(int index,
            @Nonnull ArchetypeChunk<EntityStore> archetype,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent event) {

        Vector3i pos = event.getTargetBlock();
        if (pos == null)
            return;

        World world = commandBuffer.getExternalData().getWorld();
        if (world == null)
            return;

        BlockType blockType = world.getBlockType(pos.x, pos.y, pos.z);
        String blockId = blockType != null ? blockType.getId() : null;

        if (blockId == null)
            return;

        List<DropConfig> drops = registry.getBlockDrops(blockId);
        if (drops.isEmpty())
            return;

        for (DropConfig config : drops) {
            if (probabilityService.shouldDrop(config.probability())) {
                spawnDropToWorld(config.dropId(), pos, commandBuffer);
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    private void spawnDropToWorld(String dropId, Vector3i pos, CommandBuffer<EntityStore> commandBuffer) {
        try {
            Constructor<?> ctor = ItemStack.class.getConstructor(String.class, int.class);
            ItemStack dropItem = (ItemStack) ctor.newInstance(dropId, 1);

            Vector3d dropPos = new Vector3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
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
