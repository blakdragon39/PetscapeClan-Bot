package com.petscape.bot

import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

fun handleBingoCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        when (args[0]) {
            "list" -> sendBingoGamesList(event)
            "setgame" -> setGame(event, args[0])
            "newgame" -> {
                val gameName = args.joinToString(" ")
                newGame(event, gameName)
            }
            
        }
    } else {
        sendBingoCommands(event.channel)
    }
}

private fun runIfClanStaff(event: MessageReceivedEvent, function: () -> Unit) {
    if (event.member.roles.any { it.name == "Clan Staff" }) {
        function()
    }
}

private fun sendBingoCommands(channel: MessageChannel) {
    channel.sendMessage("""
        Available commands:
        - list
        - setgame
        - newgame
        - addcard TODO implement
        - completesquare TODO implement
        - update notes TODO implement
        - winners TODO implement
        - getcard TODO implement
    """.trimIndent()).queue()

    //todo newcustomgame
}

private fun sendBingoGamesList(event: MessageReceivedEvent) = runIfClanStaff(event) {
    val response = api.getAllGames().execute().body()
    val games = response
            ?.map { "${it.name} ID: ${it.id}" }
            ?.joinToString("\n") ?: "No games found"
    event.channel.sendMessage(games).queue()
}

private fun setGame(event: MessageReceivedEvent, gameId: String) = runIfClanStaff(event) {
    mainGameId = gameId
    event.channel.sendMessage("Game ID set").queue()
}

private fun newGame(event: MessageReceivedEvent, gameName: String) = runIfClanStaff(event) {
    //todo
}