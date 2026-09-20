package dev.yen.dream.command

import com.mojang.brigadier.CommandDispatcher
import dev.yen.dream.progression.DreamProgressionCapability
import dev.yen.dream.progression.DreamProgressionRealm
import dev.yen.dream.progression.DreamProgressionService
import dev.yen.dream.service.DreamService
import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.world.region.DreamRegionAllocator
import dev.yen.dream.world.region.DreamRegionService
import dev.yen.dream.world.region.DreamWorldSavedData
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamCommands {
    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        register(
            event.dispatcher,
        )
    }

    private fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands
                .literal(
                    "dream",
                ).then(
                    Commands
                        .literal(
                            "enter",
                        ).executes { context ->
                            enterDream(
                                context.source.playerOrException,
                            )
                        },
                ).then(
                    Commands
                        .literal(
                            "exit",
                        ).executes { context ->
                            exitDream(
                                context.source.playerOrException,
                            )
                        },
                ).then(
                    Commands
                        .literal(
                            "progression",
                        ).then(
                            Commands
                                .literal(
                                    "status",
                                ).executes { context ->
                                    showProgressionStatus(
                                        context.source.playerOrException,
                                    )
                                },
                        ).then(
                            Commands
                                .literal(
                                    "swap",
                                ).executes { context ->
                                    swapProgression(
                                        context.source.playerOrException,
                                    )
                                },
                        ),
                ).then(
                    Commands
                        .literal(
                            "region",
                        ).executes { context ->
                            showRegion(
                                context.source.playerOrException,
                            )
                        }.then(
                            Commands
                                .literal(
                                    "spawn",
                                ).executes { context ->
                                    showSpawn(
                                        context.source.playerOrException,
                                    )
                                },
                        ).then(
                            Commands
                                .literal(
                                    "testspiral",
                                ).executes { context ->
                                    testSpiral(
                                        context.source.playerOrException,
                                    )
                                },
                        ),
                ),
        )
    }

    private fun enterDream(player: ServerPlayer): Int {
        val success =
            DreamService.enterDream(
                player,
                player.blockPosition(),
            )

        return if (success) {
            1
        } else {
            0
        }
    }

    private fun exitDream(player: ServerPlayer): Int =
        if (
            DreamService.wakeFromDream(
                player,
            )
        ) {
            1
        } else {
            0
        }

    private fun showProgressionStatus(player: ServerPlayer): Int {
        var found =
            false

        DreamProgressionCapability
            .get(
                player,
            ).ifPresent { progressionData ->
                found =
                    true

                player.sendSystemMessage(
                    Component.literal(
                        "Active progression: " +
                            progressionData.activeRealm.serializedName,
                    ),
                )

                player.sendSystemMessage(
                    Component.literal(
                        "Inactive state stored: " +
                            progressionData.hasInactiveState(),
                    ),
                )
            }

        if (!found) {
            player.sendSystemMessage(
                Component.literal(
                    "Dream progression capability is unavailable.",
                ),
            )

            return 0
        }

        return 1
    }

    private fun swapProgression(player: ServerPlayer): Int {
        var activeRealm: DreamProgressionRealm? =
            null

        DreamProgressionCapability
            .get(
                player,
            ).ifPresent { progressionData ->
                activeRealm =
                    progressionData.activeRealm
            }

        val currentRealm =
            activeRealm
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Dream progression capability is unavailable.",
                        ),
                    )

                    return 0
                }

        val targetRealm =
            when (currentRealm) {
                DreamProgressionRealm.WAKING -> {
                    DreamProgressionRealm.DREAM
                }

                DreamProgressionRealm.DREAM -> {
                    DreamProgressionRealm.WAKING
                }
            }

        val success =
            DreamProgressionService.swapTo(
                player,
                targetRealm,
            )

        player.sendSystemMessage(
            Component.literal(
                if (success) {
                    "Active progression: " +
                        targetRealm.serializedName
                } else {
                    "Progression swap failed. Check the server log."
                },
            ),
        )

        return if (success) {
            1
        } else {
            0
        }
    }

    private fun showRegion(player: ServerPlayer): Int {
        val dreamLevel =
            player.server.getLevel(
                DreamDimensions.DREAM,
            )
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Dream dimension is unavailable.",
                        ),
                    )

                    return 0
                }

        val region =
            DreamRegionService.getOrCreateRegion(
                player,
                dreamLevel,
            )

        player.sendSystemMessage(
            Component.literal(
                "Grid: (${region.gridX}, ${region.gridZ})",
            ),
        )

        player.sendSystemMessage(
            Component.literal(
                "Chunks: X ${region.minChunkX}..${region.maxChunkX}, " +
                    "Z ${region.minChunkZ}..${region.maxChunkZ}",
            ),
        )

        player.sendSystemMessage(
            Component.literal(
                "Blocks: X ${region.minBlockX}..${region.maxBlockX}, " +
                    "Z ${region.minBlockZ}..${region.maxBlockZ}",
            ),
        )

        player.sendSystemMessage(
            Component.literal(
                "Center: (${region.centerBlockX}, ${region.centerBlockZ})",
            ),
        )

        /*
         * Copy spawnPos into a local value so Kotlin can safely
         * smart-cast it after the null check.
         */
        val spawnPos =
            region.spawnPos

        player.sendSystemMessage(
            Component.literal(
                if (spawnPos != null) {
                    "Spawn: ${spawnPos.toShortString()}"
                } else {
                    "Spawn: not assigned"
                },
            ),
        )

        return 1
    }

    private fun showSpawn(player: ServerPlayer): Int {
        val dreamLevel =
            player.server.getLevel(
                DreamDimensions.DREAM,
            )
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Dream dimension is unavailable.",
                        ),
                    )

                    return 0
                }

        /*
         * This either returns the player's already-persisted spawn
         * or finds and permanently stores one for the first time.
         */
        val spawnPos =
            DreamRegionService.getOrCreateSpawn(
                player,
                dreamLevel,
            )
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Could not find a safe Dream spawn.",
                        ),
                    )

                    return 0
                }

        player.sendSystemMessage(
            Component.literal(
                "Dream spawn: ${spawnPos.toShortString()}",
            ),
        )

        return 1
    }

    private fun testSpiral(player: ServerPlayer): Int {
        val dreamLevel =
            player.server.getLevel(
                DreamDimensions.DREAM,
            )
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Dream dimension is unavailable.",
                        ),
                    )

                    return 0
                }

        val worldData =
            DreamWorldSavedData.get(
                dreamLevel,
            )

        /*
         * Preview the next 10 assignments using a temporary copy
         * of the currently occupied Dream-region coordinates.
         *
         * This does NOT assign any real players, modify SavedData,
         * or write anything to disk.
         */
        val preview =
            DreamRegionAllocator.previewNextRegions(
                worldData,
                10,
            )

        player.sendSystemMessage(
            Component.literal(
                "Next 10 Dream regions:",
            ),
        )

        preview.forEachIndexed { index, region ->
            player.sendSystemMessage(
                Component.literal(
                    "${index + 1}: " +
                        "(${region.gridX}, ${region.gridZ})",
                ),
            )
        }

        return 1
    }
}
