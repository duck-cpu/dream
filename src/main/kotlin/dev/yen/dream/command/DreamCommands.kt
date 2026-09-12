package dev.yen.dream.command

import com.mojang.brigadier.CommandDispatcher
import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.session.DreamSession
import dev.yen.dream.session.DreamSessionManager
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamCommands {

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        register(event.dispatcher)
    }

    private fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("dream")
                .then(
                    Commands.literal("enter")
                        .executes { context ->
                            enterDream(context.source.playerOrException)
                        }
                )
                .then(
                    Commands.literal("exit")
                        .executes { context ->
                            exitDream(context.source.playerOrException)
                        }
                )
        )
    }

    private fun enterDream(player: ServerPlayer): Int {
        val dreamLevel = player.server.getLevel(DreamDimensions.DREAM)

        if (dreamLevel == null) {
            player.sendSystemMessage(
                Component.literal("Dream dimension could not be found.")
            )

            return 0
        }

        player.teleportTo(
            dreamLevel,
            0.5,
            80.0,
            0.5,
            player.yRot,
            player.xRot
        )

        player.sendSystemMessage(
            Component.literal("You enter the dream...")
        )

        return 1
    }
    private fun exitDream(player: ServerPlayer): Int {
        val overworld = player.server.overworld()

        player.teleportTo(
            overworld,
            0.5,
            100.0,
            0.5,
            player.yRot,
            player.xRot
        )

        player.sendSystemMessage(
            Component.literal("You wake up.")
        )

        return 1
    }

   }

