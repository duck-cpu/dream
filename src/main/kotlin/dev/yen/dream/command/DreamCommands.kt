package dev.yen.dream.command

import com.mojang.brigadier.CommandDispatcher
import dev.yen.dream.service.DreamService
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
            Commands
                .literal("dream")
                .then(
                    Commands
                        .literal("enter")
                        .executes { context ->
                            enterDream(context.source.playerOrException)
                        },
                ).then(
                    Commands
                        .literal("exit")
                        .executes { context ->
                            exitDream(context.source.playerOrException)
                        },
                ),
        )
    }

    private fun enterDream(player: ServerPlayer): Int {
        val success =
            DreamService.enterDream(
                player,
                player.blockPosition(),
            )

        return if (success) 1 else 0
    }

    private fun exitDream(player: ServerPlayer): Int =
        if (DreamService.wakeFromDream(player)) {
            1
        } else {
            0
        }
}
