# Dream

A Minecraft Forge mod centered around entering a persistent Dream world while sleeping.

## Target

- Minecraft 1.20.1
- Forge 47.4.10
- Kotlin 1.9.22
- Kotlin for Forge 4.10.0
- Java 17

## Status

Early development / pre-alpha.

Sprint 4 is currently in progress. The Dream dimension foundation is working, and development is moving into persistent player-specific Dream regions.

## Current Functionality

- Dream entry only after vanilla Minecraft accepts the player's sleep attempt
- Nearby Dream Anchor enables Dream entry for a specific bed
- No Dream Anchor means completely normal vanilla sleeping
- Custom persistent `dream:dream` dimension
- Normal Overworld-style terrain, biomes, caves, ores, and structures
- Dream world uses a deterministic seed distinct from the Overworld
- Dream time is offset by 12,000 ticks from the Overworld
- Dream daylight behavior follows the inverted clock, including daylight-sensitive mobs
- Beds intentionally explode inside the Dream dimension
- Timed per-player Dream sessions
- Dream sessions persist through Save & Quit / server restart
- Remaining Dream duration resumes after reconnecting
- Players return to the bed they originally slept in
- Debug `/dream enter` and `/dream exit` commands

## Current Sprint Direction

The next major system is persistent personal Dream territory inside the shared Dream dimension.

Planned region model:

- one shared `dream:dream` dimension
- one permanent 25 x 25 chunk region per player
- UUID-to-region ownership stored persistently
- contiguous grid allocation using an outward-growing / spiral pattern
- safe persistent personal Dream spawn
- logical region borders rather than permanent physical walls
- architecture that can later support cross-border item transfer, Forge Energy, gates, and authorized travel

The Gilded Pot will replace the current Dream Anchor presentation later in Sprint 4 once its final model and assets are ready.

See `plan` for the full development roadmap.
