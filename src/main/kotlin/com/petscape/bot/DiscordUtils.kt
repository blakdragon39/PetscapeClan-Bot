package com.petscape.bot

import net.dv8tion.jda.api.entities.MessageChannel

fun sendMessage(channel: MessageChannel, message: String?) {
    if (message.isNullOrEmpty().not()) {
        channel.sendMessage(message!!).queue()
    } else {
        channel.sendMessage("A server error occurred").queue()
    }
}
