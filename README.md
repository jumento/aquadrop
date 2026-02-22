# AquaDrop API

**Author:** jume

AquaDrop is a powerful, lightweight, and highly optimized Drop Management API for Hytale. Built natively on Hytale's Entity Component System (ECS) architecture, it provides a standardized way for other mods to register custom loot drops for both block breaking and mob kills, handling all probability calculations securely and concurrently.

## Installation for Server Owners

1. Download the latest `AquaDrop.jar`.
2. Place it inside the `mods/` directory of your Hytale Server.
3. Restart the server.

## Manual for Server Owners (No Programming Required)

If you don't know how to code, you can easily use AquaDrop to create custom loot rules using a simple JSON file.

1. Create a file named `aquadrops.json` and place it in the root of your custom Mod's Asset `.zip` package.
2. **Alternatively**, if you don't use asset ZIPs, just start the server once with AquaDrop installed. The mod will automatically generate a base configuration file at `mods/AquaDrop/aquadrops.json`, which you can freely edit!

The format must be exactly like this:

```json
{
  "BlockDrops": [
    {
      "SourceId": "Furniture_Village_Coffin",
      "DropId": "AquaAmulets_Magic_Fragment",
      "Probability": 5.5,
      "Quantity": 1
    }
  ],
  "MobDrops": [
    {
      "SourceId": "Skeleton_Burnt_Alchemist",
      "DropId": "AquaAmulets_Dark_Ring",
      "Probability": 30.0,
      "Quantity": 3
    }
  ]
}
```

*AquaDrop will automatically read this file when the server starts and register these drops into the game.*

## Manual for Developers (Java API)

To use AquaDrop in your mod, you must add it as a dependency in your build tool and access its registry during your mod's initialization phase.

### 1. Acquiring the API Instance

AquaDrop registers itself via `JavaPlugin`. You can either interface with it through standard Hytale dependency injection (if supported by your mod loader pattern) or directly via its singleton accessor:

```java
import com.aquadrop.AquaDrop;
import com.aquadrop.api.CustomDropRegistry;

// Inside your mod's setup or initialization phase
CustomDropRegistry dropApi = AquaDrop.get().getRegistry();
```

### 2. Registering a Block Drop

Use `registerBlockDrop` to define what item should drop when a specific block is destroyed.

```java
import com.aquadrop.api.models.DropConfig;

// When a player breaks a "Furniture_Village_Coffin", there is a 5.5% chance to drop 1 "AquaAmulets_Magic_Fragment"
DropConfig gemDrop = new DropConfig(
    "Furniture_Village_Coffin",     // Source ID (Block ID)
    "AquaAmulets_Magic_Fragment",   // Drop ID (Item to be spawned)
    5.5f,                           // Probability (0.0f to 100.0f)
    1                               // Quantity (items per drop event)
);

dropApi.registerBlockDrop(gemDrop);
```

### 3. Registering a Mob Drop

Use `registerMobDrop` to define what item should drop when a specific entity is killed by any means.

```java
// When a "Skeleton_Burnt_Alchemist" dies, there is a 30.0% chance to drop 3 "AquaAmulets_Dark_Ring"
DropConfig ringDrop = new DropConfig(
    "Skeleton_Burnt_Alchemist",     // Source ID (Entity Type ID)
    "AquaAmulets_Dark_Ring",        // Drop ID
    30.0f,                          // Probability
    3                               // Quantity
);

dropApi.registerMobDrop(ringDrop);
```

### Important Notes on Probabilities

The `DropConfig` record is extremely strict. If you attempt to register a probability below `0.0f` or above `100.0f`, the server will throw an `IllegalArgumentException` and halt that specific registration process to alert you of the malformed data immediately. Always ensure your float values are within bounds.

## Features

- **ECS Native**: Built directly on top of Hytale's Server API `EntityEventSystem` and `DeathSystems.OnDeathSystem`.
- **High Performance**: Uses `ConcurrentHashMap` for instant O(1) lookups and `ThreadLocalRandom` for thread-safe probability evaluation.
- **Fail-Fast Safety**: Utilizes Java 25 `Record` immutability to strictly validate probabilities (0.0f - 100.0f) and quantities (must be > 0) at the very moment of registration, preventing silent errors.
- **Item Quantity Control**: Each drop rule supports a configurable quantity, allowing multiple items to be dropped in a single event.
- **Unified Registry**: A single point of access.
- **In-Game Admin UI**: Visual menu accessible via `/aquadrop config` to add and hot-reload drop rules without restarting the server.

## License

This project is licensed under the **GNU Affero General Public License Version 3 (AGPLv3)**.
Permissions of this strong copyleft license are conditioned on making available complete source code of licensed works and modifications, which include larger works using a licensed work, under the same license. Copyright and license notices must be preserved. Contributors provide an express grant of patent rights. When a modified version is used to provide a service over a network, the complete source code of the modified version must be made available.
