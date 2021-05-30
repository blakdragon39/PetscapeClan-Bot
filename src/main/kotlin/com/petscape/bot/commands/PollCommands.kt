package com.petscape.bot.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

fun handleCreatePollCommand(event: MessageReceivedEvent) {
    event.message.emotes.forEach { emote ->
        event.message.addReaction(emote).queue()
    }
}