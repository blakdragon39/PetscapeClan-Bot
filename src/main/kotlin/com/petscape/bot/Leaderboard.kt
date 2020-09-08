package com.petscape.bot

import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

fun handleLeaderboardCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        val commandArgs = args.subList(1, args.size)
        when (args[0]) {
            else -> sendMessage(event.channel, "Unknown command ${args[0]}")
        }
    } else {
        sendLeaderboardCommands(event.channel)
    }
}

private fun sendLeaderboardCommands(channel: MessageChannel) {
    channel.sendMessage("""
        Available commands:
        - list **Clan Staff only**
        - standings
        - newgame [attach file with card] **Clan Staff only**
        - addsubmission 
    """.trimIndent()).queue()
}