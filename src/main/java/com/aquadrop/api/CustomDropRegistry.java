package com.aquadrop.api;

import com.aquadrop.api.models.DropConfig;
import java.util.List;

public interface CustomDropRegistry {
    /**
     * Registra el inicio de un drop al romperse un bloque.
     */
    void registerBlockDrop(DropConfig config);

    /**
     * Registra un drop al morir una entidad o mob.
     */
    void registerMobDrop(DropConfig config);

    /**
     * Obtiene los drops registrados para un bloque específico.
     */
    List<DropConfig> getBlockDrops(String blockId);

    /**
     * Obtiene los drops registrados para una entidad específica.
     */
    List<DropConfig> getMobDrops(String mobId);
}
