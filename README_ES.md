# API AquaDrop

**Autor:** jume
AquaDrop es una API potente, ligera y altamente optimizada para la gestión de botines (Drops) en Hytale. Construida de forma nativa sobre la arquitectura ECS (Entity Component System) de Hytale, proporciona un sistema estandarizado para que otros mods registren caídas de botín personalizadas tanto para la destrucción de bloques como para el asesinato de entidades, gestionando todos los cálculos de probabilidad de forma segura y concurrente.

## Instalación para Propietarios de Servidor

1. Descarga el archivo `AquaDrop.jar` más reciente.
2. Colócalo dentro de la carpeta `mods/` de tu servidor Hytale.
3. Reinicia el servidor.

## Manual para Propietarios de Servidor (Sin necesidad de Programar)

Si no sabes programar, puedes usar AquaDrop fácilmente para crear reglas de botín personalizadas usando un simple archivo JSON.

1. Crea un archivo llamado `aquadrops.json` y colócalo en la raíz del empaquetado `.zip` de los Assets de tu Mod personalizado.
2. **De manera alternativa**, si no usas ZIP de assets, simplemente enciende tu servidor una vez con AquaDrop instalado. ¡El mod generará automáticamente un archivo base en la ruta `mods/AquaDrop/aquadrops.json` que puedes modificar a tu antojo!

El formato debe ser exactamente el siguiente:

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

*AquaDrop leerá automáticamente este archivo cuando el servidor inicie y registrará estas caídas de botín en el juego.*

## Manual para Desarrolladores (API en Java)

Para usar AquaDrop en tu mod, debes añadirlo como dependencia en tu herramienta de compilación (build tool) y acceder a su registro durante la fase de inicialización de tu mod.

### 1. Obteniendo la Instancia de la API

AquaDrop se registra a sí mismo a través de `JavaPlugin`. Puedes interactuar con él usando la inyección de dependencias estándar de Hytale (si tu patrón de carga de mods lo soporta) o directamente a través de su accesor Singleton:

```java
import com.aquadrop.AquaDrop;
import com.aquadrop.api.CustomDropRegistry;

// Dentro de la fase de inicialización o configuración de tu mod
CustomDropRegistry dropApi = AquaDrop.get().getRegistry();
```

> **Seguridad en el Orden de Carga**: El registro de AquaDrop se inicializa en el constructor del plugin, no en `setup()`. Esto significa que `AquaDrop.get().getRegistry()` está garantizado a devolver un registro válido y no nulo independientemente de qué plugin se inicialice primero. No necesitas declarar una dependencia ni preocuparte por el orden de carga de plugins.

### 2. Registrando un Drop de Bloque

Usa `registerBlockDrop` para definir qué ítem debe caer cuando se destruye un bloque específico.

```java
import com.aquadrop.api.models.DropConfig;

// Cuando un jugador rompe un "Furniture_Village_Coffin", hay un 5.5% de probabilidad de que caiga 1 "AquaAmulets_Magic_Fragment"
DropConfig gemDrop = new DropConfig(
    "Furniture_Village_Coffin",     // Source ID (ID del Bloque)
    "AquaAmulets_Magic_Fragment",   // Drop ID (ID del Ítem que se generará)
    5.5f,                           // Probabilidad (0.0f a 100.0f)
    1                               // Cantidad (ítems por evento de drop)
);

dropApi.registerBlockDrop(gemDrop);
```

### 3. Registrando un Drop de Entidad (Mob)

Usa `registerMobDrop` para definir qué ítem debe caer cuando una entidad específica muere por cualquier causa.

```java
// Cuando un "Skeleton_Burnt_Alchemist" muere, hay un 30.0% de probabilidad de que caigan 3 "AquaAmulets_Dark_Ring"
DropConfig ringDrop = new DropConfig(
    "Skeleton_Burnt_Alchemist",     // Source ID (ID del Tipo de Entidad)
    "AquaAmulets_Dark_Ring",        // Drop ID
    30.0f,                          // Probabilidad
    3                               // Cantidad
);

dropApi.registerMobDrop(ringDrop);
```

### Notas Importantes sobre Probabilidades

El Record `DropConfig` es extremadamente estricto. Si intentas registrar una probabilidad inferior a `0.0f` o superior a `100.0f`, el servidor lanzará una excepción `IllegalArgumentException` y detendrá ese proceso de registro específico en ese mismo momento para alertarte de que los datos están mal formados. Asegúrate siempre de que tus valores `float` se encuentran dentro de los límites.

## Características

- **Nativo ECS**: Construido directamente sobre los sistemas de la API del Servidor de Hytale: `EntityEventSystem` y `DeathSystems.OnDeathSystem`.
- **Alto Rendimiento**: Emplea `ConcurrentHashMap` para búsquedas instantáneas O(1) y `ThreadLocalRandom` para una evaluación de probabilidad segura en entornos multihilos.
- **Seguridad Fail-Fast**: Utiliza la inmutabilidad de los `Record` de Java 25 para validar estrictamente las probabilidades (0.0f - 100.0f) y cantidades (deben ser > 0) en el instante mismo del registro, evitando errores silenciosos.
- **Control de Cantidad de Ítems**: Cada regla de drop soporta una cantidad configurable, permitiendo que caigan múltiples ítems en un solo evento.
- **Registro Unificado**: Un único punto de acceso.
- **Menú de Administración In-Game**: Menú visual accesible vía `/aquadrop config` para añadir y recargar reglas de drop sin reiniciar el servidor.

## Licencia

Este proyecto está licenciado bajo la **GNU Affero General Public License Versión 3 (AGPLv3)**.
Los permisos de esta estricta licencia copyleft están condicionados a poner a disposición el código fuente completo de las obras licenciadas y sus modificaciones, lo que incluye obras mayores que utilicen la obra licenciada, bajo la misma licencia. Los avisos de derechos de autor y licencia deben conservarse. Los contribuyentes proporcionan una concesión expresa de derechos de patente. Cuando se utiliza una versión modificada para proporcionar un servicio a través de una red, el código fuente completo de la versión modificada debe estar disponible.
