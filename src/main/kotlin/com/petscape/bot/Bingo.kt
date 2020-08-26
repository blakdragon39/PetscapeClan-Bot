package com.petscape.bot

import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.Request

fun handleBingoCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "list" -> sendBingoGamesList(event.channel)
            "setgame" -> setGame(args[0], event.channel)
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

//todo restrict to clan staff
private fun sendBingoGamesList(channel: MessageChannel) {
    val request = Request.Builder()
            .url("http://localhost:8080/bingo/all")
            .header("Authorization", credentials)
            .build()
    val response = client.newCall(request).execute()

    channel.sendMessage(response.body()?.string()).queue()
}

//todo restrict to clan staff
private fun setGame(gameId: String, channel: MessageChannel) {
    mainGameId = gameId
    channel.sendMessage("Game ID set").queue()
}