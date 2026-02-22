package com.aquadrop.core.registry;

import com.aquadrop.api.models.DropConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class LocalConfigLoader {

    private final CustomDropRegistryImpl registry;
    private final com.aquadrop.AquaDrop plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_DIR = new File("mods/AquaDrop");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "aquadrops.json");
    private static final String CURRENT_CONFIG_VERSION = "1.0.0";

    public LocalConfigLoader(CustomDropRegistryImpl registry, com.aquadrop.AquaDrop plugin) {
        this.registry = registry;
        this.plugin = plugin;
    }

    public void loadOrGenerateConfig() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        if (!CONFIG_FILE.exists()) {
            generateDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            if (root == null) {
                plugin.getLogger().at(Level.WARNING).log("Local aquadrops.json is empty. Skipping...");
                return;
            }

            // Version control check
            if (!root.has("ConfigVersion") || !root.get("ConfigVersion").getAsString().equals(CURRENT_CONFIG_VERSION)) {
                plugin.getLogger().at(Level.INFO).log("Old or missing ConfigVersion detected. Creating a backup...");
                createBackup();
                root.addProperty("ConfigVersion", CURRENT_CONFIG_VERSION);
                try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                    gson.toJson(root, writer);
                }
            }

            if (root.has("BlockDrops")) {
                parseDrops(root.getAsJsonArray("BlockDrops"), true);
            }

            if (root.has("MobDrops")) {
                parseDrops(root.getAsJsonArray("MobDrops"), false);
            }

            plugin.getLogger().at(Level.INFO)
                    .log("Rules loaded successfully from local file (mods/AquaDrop/aquadrops.json).");

        } catch (Exception e) {
            plugin.getLogger().at(Level.SEVERE)
                    .log("Error reading local aquadrops.json configuration: " + e.getMessage());
        }
    }

    private void createBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupDir = new File(CONFIG_DIR, "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            File backupFile = new File(backupDir, "aquadrops_backup_" + timestamp + ".json");
            Files.copy(CONFIG_FILE.toPath(), backupFile.toPath());
            plugin.getLogger().at(Level.INFO).log("Configuration backup created at: " + backupFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE).log("Failed to create configuration backup: " + e.getMessage());
        }
    }

    private void generateDefaultConfig() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("ConfigVersion", CURRENT_CONFIG_VERSION);

            JsonArray blockDrops = new JsonArray();
            JsonArray mobDrops = new JsonArray();

            JsonObject sampleBlock = new JsonObject();
            sampleBlock.addProperty("SourceId", "Example_Block_ID");
            sampleBlock.addProperty("DropId", "Example_Item_Drop_ID");
            sampleBlock.addProperty("Probability", 100.0f);
            blockDrops.add(sampleBlock);

            JsonObject sampleMob = new JsonObject();
            sampleMob.addProperty("SourceId", "Example_Mob_ID");
            sampleMob.addProperty("DropId", "Example_Item_Drop_ID");
            sampleMob.addProperty("Probability", 25.5f);
            mobDrops.add(sampleMob);

            root.add("BlockDrops", blockDrops);
            root.add("MobDrops", mobDrops);

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                gson.toJson(root, writer);
            }

            plugin.getLogger().at(Level.INFO)
                    .log("Default aquadrops.json generated at " + CONFIG_FILE.getAbsolutePath());

        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE)
                    .log("Could not generate default local aquadrops.json: " + e.getMessage());
        }
    }

    private void parseDrops(JsonArray items, boolean isBlockDrop) {
        for (JsonElement element : items) {
            try {
                JsonObject drop = element.getAsJsonObject();
                String sourceId = drop.get("SourceId").getAsString();
                String dropId = drop.get("DropId").getAsString();
                float probability = drop.get("Probability").getAsFloat();

                // Skip dummy examples
                if (sourceId.equals("Example_Block_ID") || sourceId.equals("Example_Mob_ID")) {
                    continue;
                }

                DropConfig config = new DropConfig(sourceId, dropId, probability);

                if (isBlockDrop) {
                    registry.registerBlockDrop(config);
                } else {
                    registry.registerMobDrop(config);
                }
            } catch (Exception e) {
                plugin.getLogger().at(Level.WARNING).log("Ignored error while mapping a local JSON Drop.");
            }
        }
    }

    /**
     * Dynamically adds or updates a drop directly from the Admin UI to the json
     * file.
     */
    public void addAndSaveDropConfig(String sourceId, String dropId, float probability, boolean isBlock) {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        JsonObject root = null;
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                root = gson.fromJson(reader, JsonObject.class);
            } catch (Exception ignored) {
            }
        }

        if (root == null) {
            root = new JsonObject();
            root.addProperty("ConfigVersion", CURRENT_CONFIG_VERSION);
            root.add("BlockDrops", new JsonArray());
            root.add("MobDrops", new JsonArray());
        } else {
            if (!root.has("ConfigVersion")) {
                root.addProperty("ConfigVersion", CURRENT_CONFIG_VERSION);
            }
            if (!root.has("BlockDrops")) {
                root.add("BlockDrops", new JsonArray());
            }
            if (!root.has("MobDrops")) {
                root.add("MobDrops", new JsonArray());
            }
        }

        JsonArray targetArray = isBlock ? root.getAsJsonArray("BlockDrops") : root.getAsJsonArray("MobDrops");

        boolean updated = false;
        // Check if exists
        for (JsonElement element : targetArray) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("SourceId") && obj.has("DropId")) {
                if (obj.get("SourceId").getAsString().equals(sourceId)
                        && obj.get("DropId").getAsString().equals(dropId)) {
                    obj.addProperty("Probability", probability);
                    updated = true;
                    break;
                }
            }
        }

        if (!updated) {
            JsonObject newDrop = new JsonObject();
            newDrop.addProperty("SourceId", sourceId);
            newDrop.addProperty("DropId", dropId);
            newDrop.addProperty("Probability", probability);
            targetArray.add(newDrop);
        }

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            plugin.getLogger().at(Level.SEVERE).log("Error saving the new Drop to disk: " + e.getMessage());
        }

        // Live Reload instantaneo
        plugin.reloadDrops();
    }
}
