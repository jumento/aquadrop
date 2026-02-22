package com.aquadrop.core.registry;

import com.aquadrop.api.CustomDropRegistry;
import com.aquadrop.api.models.DropConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomDropRegistryImpl implements CustomDropRegistry {
    // Usamos ConcurrentHashMap previendo comportamientos multihilos del motor ECS
    // de Hytale
    private final Map<String, List<DropConfig>> blockDrops = new ConcurrentHashMap<>();
    private final Map<String, List<DropConfig>> mobDrops = new ConcurrentHashMap<>();

    @Override
    public void registerBlockDrop(DropConfig config) {
        blockDrops.computeIfAbsent(config.sourceId(), k -> new ArrayList<>()).add(config);
    }

    @Override
    public void registerMobDrop(DropConfig config) {
        mobDrops.computeIfAbsent(config.sourceId(), k -> new ArrayList<>()).add(config);
    }

    @Override
    public List<DropConfig> getBlockDrops(String blockId) {
        return blockDrops.getOrDefault(blockId, List.of());
    }

    @Override
    public List<DropConfig> getMobDrops(String mobId) {
        return mobDrops.getOrDefault(mobId, List.of());
    }

    /**
     * Limpia todos los registros en memoria. Útil para Hot-Reload
     */
    public void clear() {
        blockDrops.clear();
        mobDrops.clear();
    }
}
