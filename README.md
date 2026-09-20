# Dream

Dream is an experimental Minecraft Forge mod about entering a persistent shared Dream world while sleeping.

Each player receives a permanent personal region inside the Dream dimension. The Gilded Pot connects an eligible bed to the Dream while preserving Minecraft’s normal sleeping rules.

> Dream is currently in early pre-alpha development. Core systems work, but survival progression, balancing, configuration, and several failure cases are still unfinished.

## Current Status

Sprint 4 is complete.

Currently implemented:

- Persistent shared Dream dimension
- Vanilla-compatible Dream entry through beds
- Permanent player-specific Dream regions
- Persistent Dream sessions
- Server-enforced region borders
- Animated client-side Dream borders
- Functional Gilded Pot activation and storage
- Multiplayer region separation and persistence

Sprint 5 will begin with development and debugging tools for viewing Dream-region allocation.

See [`plan`](plan) for the complete roadmap and current source-of-truth documentation.

## Requirements

| Component | Version |
|---|---:|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 |
| Kotlin | 1.9.22 |
| Kotlin for Forge | 4.10.0 |
| Java | 17 |

## Entering the Dream

Dream entry begins through an ordinary Minecraft bed.

For a bed to initiate a Dream:

1. A Gilded Pot must be within three blocks of that specific bed.
2. Minecraft must accept the player’s normal sleep attempt.
3. The player must not already have an active Dream session.

Vanilla sleep validation remains authoritative. Daytime sleep attempts, nearby hostile mobs, and other invalid sleep conditions still prevent Dream entry.

If no Gilded Pot is nearby, the bed behaves exactly like a normal Minecraft bed.

Beds do not work inside `dream:dream` and intentionally explode according to the Dream dimension’s configuration.

## The Dream Dimension

The mod uses one shared persistent dimension:

```text
dream:dream
```

The Dream dimension currently provides:

- Overworld-style terrain
- Normal biome generation
- Caves
- Ores
- Structures
- A deterministic seed distinct from the Overworld
- Persistent terrain
- A clock offset 12,000 ticks from the Overworld
- Server daylight behavior matching the inverted Dream clock

The Dream is not implemented as a separate dimension instance for every player. Instead, all players share one dimension while owning separate permanent regions within it.

## Personal Dream Regions

Every player is permanently assigned one region:

```text
25 x 25 chunks
400 x 400 blocks
```

Region ownership is keyed by player UUID and stored in the Dream dimension’s `SavedData`.

Regions are assigned using an outward-growing square spiral:

```text
1:  ( 0,  0)
2:  ( 1,  0)
3:  ( 1,  1)
4:  ( 0,  1)
5:  (-1,  1)
6:  (-1,  0)
7:  (-1, -1)
8:  ( 0, -1)
9:  ( 1, -1)
10: ( 2, -1)
```

Assignments survive:

- Logout and reconnect
- Save & Quit
- Integrated-server restart
- Full server restart

Terrain quality does not currently change region ownership. A player keeps the same grid cell even if its center initially generates in an ocean or another unsafe location.

### Personal Spawn

The center of the player’s region is used as the initial arrival location.

If the natural center is safe, the terrain is preserved. If it is unsafe, the current temporary solution creates a minimal 3 × 3 stone platform and clears enough space for the player.

The resulting spawn position is stored permanently.

This fallback platform is temporary development behavior, not the final solution for ocean-heavy or resource-starved regions.

## Dream Borders

Players are confined to their own regions while inside the Dream.

Server-side enforcement:

- Prevents crossing region boundaries
- Handles all four borders and corners
- Accounts for the player’s bounding box
- Cancels velocity through a crossed boundary
- Preserves movement parallel to the border
- Prevents breaking blocks in another player’s region
- Prevents placing blocks in another player’s region
- Prevents right-click interaction in another player’s region

The client renders nearby boundaries using an animated teal/cyan Dream shader based on the vanilla Nether portal texture.

The visual border:

- Fades with distance
- Is visible from either side
- Can be occluded by terrain
- Does not write to the depth buffer
- Does not replace server-side enforcement

## Dream Sessions

A `DreamSession` represents one temporary visit to the Dream.

It stores enough information to:

- Identify the player
- Track remaining Dream duration
- Prevent nested Dream sessions
- Return the player to the original bed
- Restore an interrupted session after reconnecting

The current development duration is:

```text
20,000 ticks
1,000 seconds
approximately 16 minutes 40 seconds
```

Dream time currently pauses while the player or server is offline.

When the timer expires, or `/dream exit` is used, the player returns to the stored origin bed position.

## Gilded Pot

The Gilded Pot is the physical Dream activation block:

```text
dream:gilded_pot
```

Current behavior:

- Activates eligible beds within three blocks
- Uses a custom model and 32 × 32 texture
- Has a custom collision and selection shape
- Uses decorated-pot-style sounds
- Is not consumed when a Dream begins
- Contains nine persistent storage slots
- Retains stored contents after closing and reopening
- Retains stored contents across world restarts
- Drops the pot and its stored contents separately when broken
- Supports comparator output based on inventory fullness

The Gilded Pot does not override vanilla sleep restrictions.

## Debug Commands

The following development commands currently exist:

| Command | Purpose |
|---|---|
| `/dream enter` | Enter the Dream using the normal Dream lifecycle |
| `/dream exit` | Wake from the current Dream |
| `/dream region` | Display the player’s permanent region information |
| `/dream region spawn` | Display or initialize the personal Dream spawn |
| `/dream region testspiral` | Preview upcoming spiral allocations without modifying SavedData |

These are development tools and do not yet have final public-server permission rules.

## Building from Source

Clone the repository and enter the project directory:

```bash
git clone https://github.com/duck-cpu/dream.git
cd dream
```

Build the mod with:

```bash
./gradlew clean build
```

The compiled JAR will be written to:

```text
build/libs/
```

Launch the development client with:

```bash
./gradlew runClient
```

The project also contains separate development configurations for multiplayer testing:

```bash
./gradlew runClientA
./gradlew runClientB
```

A development server can be launched with:

```bash
./gradlew runServer
```

## Project Structure

Important source areas include:

```text
src/main/kotlin/dev/yen/dream/
├── block/          Gilded Pot block and block entity
├── client/         Client registration and Dream-border rendering
├── command/        Development commands
├── event/          Forge event handlers
├── registry/       Forge block and block-entity registration
├── service/        Dream entry and wake lifecycle
├── session/        Temporary Dream-session state and persistence
└── world/region/   Region models, allocation, SavedData, and access control
```

Java mixins under `src/main/java/dev/yen/dream/mixin/` currently support Dream-specific world behavior such as time, seed, and sky brightness.

## Known Limitations

The following systems are incomplete or intentionally temporary:

- Player death behavior inside the Dream
- Safe recovery from corrupted persistent data
- Behavior when the original bed is destroyed
- Vanilla-style safe wake positioning
- Configurable Dream duration
- Survival acquisition and progression
- Ocean and resource viability guarantees
- Final public-server command permissions
- Final handling of same-night Dream re-entry
- Final interaction with multiplayer night progression

## Future Ideas

These are planned concepts, not currently implemented features.

### Gilded Pot Cracking

Concept suggested by Squiggs:

- Dying inside the Dream would damage the Gilded Pot that initiated the session.
- Repeated deaths would progressively crack the pot.
- A fully cracked pot would no longer permit Dream entry.

### Gilded Pot Repair

Concept suggested by Armpie:

- Damaged or fully cracked pots could be repaired in an anvil.
- Gold ingots would restore one or more crack stages.
- Stored contents should survive cracking and repair.

### Dream Modifiers

Items stored in the Gilded Pot may eventually modify the Dream experience associated with that pot.

Open design questions include:

- Whether modifiers affect the permanent personal region, a future dungeon, or both
- Whether items are consumed as offerings or retained as catalysts
- Whether contents are captured at Dream entry or checked continuously
- How multiple items combine
- How conflicting modifiers are resolved

### Procedural Dream Dungeon

The permanent personal Dream region is separate from the planned procedural dungeon system.

The current long-term direction is:

```text
Personal Dream Region
        ↓
Special Dream-world Item
        ↓
Portal or Dungeon Entrance
        ↓
Procedural Dream Dungeon
        ↓
Encounters, Loot, Progression, and Bosses
```

## License

Dream is currently distributed as **All Rights Reserved**.

Unless explicit permission is granted, the source code and assets may not be redistributed or reused outside the terms permitted by applicable law.
