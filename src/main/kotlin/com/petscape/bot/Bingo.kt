package com.petscape.bot

import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.Request

fun runIfClanStaff(event: MessageReceivedEvent, function: () -> Unit) {
    if (event.member.roles.any { it.name == "Clan Staff" }) {
        function()
    }
}

fun handleBingoCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "list" -> sendBingoGamesList(event)
            "setgame" -> setGame(event, args[0])
        }
    } else {
        sendBingoCommands(event.channel)
    }
}

private fun sendBingoCommands(channel: MessageChannel) {
    channel.sendMessage("""
        Available commands:
        - list
    """.trimIndent()).queue()
}

private fun sendBingoGamesList(event: MessageReceivedEvent) = runIfClanStaff(event) {
    val request = Request.Builder()
            .url("http://localhost:8080/bingo/all")
            .header("Authorization", credentials)
            .build()
    val response = client.newCall(request).execute()

    event.channel.sendMessage(response.body()?.string()).queue()
}

private fun setGame(event: MessageReceivedEvent, gameId: String) = runIfClanStaff(event) {
    mainGameId = gameId
    event.channel.sendMessage("Game ID set").queue()
}