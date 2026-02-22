package com.aquadrop;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.aquadrop.api.CustomDropRegistry;
import com.aquadrop.core.registry.CustomDropRegistryImpl;
import com.aquadrop.core.services.ProbabilityService;
import com.aquadrop.core.services.StandardProbability;
import com.aquadrop.core.systems.BlockDropSystem;
import com.aquadrop.core.systems.MobDropSystem;

import java.util.logging.Level;

/**
 * Main plugin class for the AquaDrop API/Mod.
 */
public final class AquaDrop extends JavaPlugin {
    private static AquaDrop instance;

    private CustomDropRegistry registry;
    private ProbabilityService probabilityService;

    public AquaDrop(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();

        // Initialize Core Services
        this.registry = new CustomDropRegistryImpl();
        this.probabilityService = new StandardProbability();

        // Load configs from external Asset Zips (.json)
        new com.aquadrop.core.registry.ZipConfigLoader(
                (CustomDropRegistryImpl) this.registry,
                this).loadFromJsonConfig();

        // Load configs from physical disk file (mods/AquaDrop/aquadrops.json)
        new com.aquadrop.core.registry.LocalConfigLoader(
                (CustomDropRegistryImpl) this.registry,
                this).loadOrGenerateConfig();

        // Register ECS Event Systems using Hytale's Component/System API
        getEntityStoreRegistry().registerSystem(new BlockDropSystem(this.registry, this.probabilityService));
        getEntityStoreRegistry().registerSystem(new MobDropSystem(this.registry, this.probabilityService));

        // Register Global Commands
        getCommandRegistry().registerCommand(new com.aquadrop.commands.AquaDropCommand());

        getLogger().at(Level.INFO)
                .log("AquaDrop Custom Drop API has been successfully initialized and connected to ECS.");
    }

    /**
     * Limpia la memoria RAM de ruleset y vuelve a forzar la lectura de todos los
     * JSONS en disco
     * Se puede llamar dinamicamente.
     */
    public void reloadDrops() {
        if (this.registry instanceof CustomDropRegistryImpl) {
            ((CustomDropRegistryImpl) this.registry).clear();
            new com.aquadrop.core.registry.ZipConfigLoader(
                    (CustomDropRegistryImpl) this.registry, this).loadFromJsonConfig();
            new com.aquadrop.core.registry.LocalConfigLoader(
                    (CustomDropRegistryImpl) this.registry, this).loadOrGenerateConfig();
            getLogger().at(Level.INFO).log("AquaDrop: Action -> Live Reload executed successfully.");
        }
    }

    public static AquaDrop get() {
        return instance;
    }

    /**
     * Provide access to the custom registry for other mods.
     */
    public CustomDropRegistry getRegistry() {
        return registry;
    }
}
