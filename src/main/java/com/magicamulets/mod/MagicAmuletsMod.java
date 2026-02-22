package com.magicamulets.mod;

import com.aquadrop.api.CustomDropRegistry;
import com.aquadrop.api.models.DropConfig;

public class MagicAmuletsMod {

    /**
     * Esto representa el método de inicio que tu mod de terceros usaría
     * recibiendo por algún método de inyección de Hytale la dependencia a AquaDrop.
     */
    public void onServerStart(CustomDropRegistry aquadropApi) {

        // 1. Drop por Romper: Romper un "Furniture_Village_Coffin" nos de un
        // "magic_fragment" con un tímido 5.5%
        DropConfig gemDrop = new DropConfig(
                "Furniture_Village_Coffin",
                "AquaAmulets_Magic_Fragment",
                5.5f);
        aquadropApi.registerBlockDrop(gemDrop);

        // 2. Drop por Matar: Los "Skeleton_Burnt_Alchemist" dejan caer un "dark_ring"
        // al morir en un 30%
        DropConfig ringDrop = new DropConfig(
                "Skeleton_Burnt_Alchemist",
                "AquaAmulets_Dark_Ring",
                30.0f);
        aquadropApi.registerMobDrop(ringDrop);

        System.out.println("Magic Amulets ha registrado correctamente sus dependencias de loot.");

        // 3. Fallo intencional: Probabilidades incorrectas romperán limpiamente
        // avisando al Dev "fast"
        // Descomentarlo causaría una detención limpia al momento del registo gracias a
        // los Records de Java.
        // aquadropApi.registerMobDrop(new DropConfig("hytale:zombie", "bad_item",
        // 150.0f));
    }
}
