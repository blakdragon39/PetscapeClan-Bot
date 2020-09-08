package com.petscape.bot

import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.ResponseBody

fun runIfClanStaff(event: MessageReceivedEvent, function: () -> Unit) {
    if (event.member.roles.any { it.name == "Clan Staff" }) {
        function()
    } else {
        sendMessage(event.channel, "Permission not granted")
    }
}

fun sendError(channel: MessageChannel, e: Exception) {
    e.printStackTrace()
    sendMessage(channel, e.message)
}

fun sendError(channel: MessageChannel, error: ResponseBody?) {
    sendMessage(channel, error?.string())
}

fun sendMessage(channel: MessageChannel, message: String?) {
    if (message != null) {
        channel.sendMessage(message).queue()
    } else {
        channel.sendMessage("A server error occurred").queue()
    }
}