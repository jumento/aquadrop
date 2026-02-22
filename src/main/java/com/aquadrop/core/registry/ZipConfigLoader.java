package com.aquadrop.core.registry;

import com.aquadrop.api.models.DropConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipConfigLoader {

    private final CustomDropRegistryImpl registry;
    private final com.aquadrop.AquaDrop plugin;
    private final Gson gson = new Gson();

    public ZipConfigLoader(CustomDropRegistryImpl registry, com.aquadrop.AquaDrop plugin) {
        this.registry = registry;
        this.plugin = plugin;
    }

    /**
     * Busca y evalúa si en la carpeta de recursos de AssetLoad/Zip
     * existen propiedades para mapear drops en `aquadrops.json`.
     */
    public void loadFromJsonConfig() {
        File modsDir = new File("mods");

        try {
            // Intentar detectar físicamente el directorio de origen desde donde se está
            // cargando el jar de AquaDrop
            // Esto es vital porque en Singleplayer Hytale no usa ./mods/ sino
            // AppData/Roaming/.../Mods/
            File jarPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarPath.exists() && jarPath.getParentFile() != null) {
                modsDir = jarPath.getParentFile();
            }
        } catch (Exception ignored) {
            // Usará "mods" como fallback en servidores dedicados.
        }

        if (!modsDir.exists() || !modsDir.isDirectory()) {
            plugin.getLogger().at(Level.WARNING)
                    .log("The mods directory (" + modsDir.getAbsolutePath()
                            + ") does not exist. External zips will not be scanned.");
            return;
        }

        File[] zipFiles = modsDir
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar"));

        if (zipFiles == null || zipFiles.length == 0) {
            return;
        }

        for (File zipFile : zipFiles) {
            try (ZipFile zip = new ZipFile(zipFile)) {

                // Buscar iterativamente si el zip contiene nuestro archivo de configuración en
                // la raíz.
                ZipEntry entry = zip.getEntry("aquadrops.json");

                if (entry != null && !entry.isDirectory()) {
                    try (InputStream is = zip.getInputStream(entry);
                            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                        JsonObject root = gson.fromJson(reader, JsonObject.class);

                        if (root.has("BlockDrops")) {
                            parseDrops(root.getAsJsonArray("BlockDrops"), true);
                        }

                        if (root.has("MobDrops")) {
                            parseDrops(root.getAsJsonArray("MobDrops"), false);
                        }

                        plugin.getLogger().at(Level.INFO)
                                .log("Successfully loaded JSON rules from pack: " + zipFile.getName());
                    } catch (Exception parseEx) {
                        plugin.getLogger().at(Level.SEVERE).log("Error reading aquadrops.json inside "
                                + zipFile.getName() + ": " + parseEx.getMessage());
                    }
                }
            } catch (Exception e) {
                // Ignorar archivos que no sean verdaderos Archivos Zip válidos a nivel de
                // sistema.
            }
        }
    }

    private void parseDrops(JsonArray items, boolean isBlockDrop) {
        for (JsonElement element : items) {
            try {
                JsonObject drop = element.getAsJsonObject();
                String sourceId = drop.get("SourceId").getAsString();
                String dropId = drop.get("DropId").getAsString();
                float probability = drop.get("Probability").getAsFloat();
                int quantity = drop.has("Quantity") ? drop.get("Quantity").getAsInt() : 1;

                DropConfig config = new DropConfig(sourceId, dropId, probability, quantity);

                if (isBlockDrop) {
                    registry.registerBlockDrop(config);
                } else {
                    registry.registerMobDrop(config);
                }
            } catch (Exception e) {
                plugin.getLogger().at(Level.WARNING).log(
                        "Ignored error mapping a JSON Drop: Make sure it contains SourceId, DropId, and a positive float for Probability (0.0-100.0).");
            }
        }
    }
}
