package com.petscape.bot

import com.petscape.bot.models.GameType
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

fun handleBingoCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        val commandArgs = args.subList(1, args.size)
        when (args[0]) {
            "list" -> sendBingoGamesList(event)
            "setgame" -> setGame(event, commandArgs[0])
            "newgame" -> {
                val gameName = commandArgs.joinToString(" ")
                newGame(event, gameName)
            }
            "addcard" -> {
                val username = commandArgs.joinToString(" ")
                addCard(event, username)
            }
            "completesquare" -> {
                val square = commandArgs[0].toInt()
                val username = commandArgs.subList(1, args.size).joinToString(" ")
                completeSquare(event, username, square)
            }
            "winners" -> sendWinners(event.channel)
            "getcard" -> {
                val username = commandArgs.joinToString(" ")
                sendCard(event.channel, username)
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
        - newgame game name
        - addcard
        - completesquare
        - winners
        - getcard
    """.trimIndent()).queue()

    //todo newcustomgame
    //todo updatenotes
}

private fun sendBingoGamesList(event: MessageReceivedEvent) = runIfClanStaff(event) {
    val gameIds = api.getAllGames().execute().body()
    val games = gameIds
            ?.map { "${it.name} ID: ${it.id}" }
            ?.joinToString("\n") ?: "No games found"
    event.channel.sendMessage(games).queue()
}

private fun setGame(event: MessageReceivedEvent, gameId: String) = runIfClanStaff(event) {
    mainGameId = gameId
    event.channel.sendMessage("Game ID set").queue()
}

private fun newGame(event: MessageReceivedEvent, gameName: String) = runIfClanStaff(event) {
    val game = api.newBingoGame(gameName, GameType.BOSSES, freeSpace = true, cardsMatch = false).execute().body() //todo more options
    mainGameId = game?.id
    event.channel.sendMessage("Game $gameName created and started").queue()
}

private fun addCard(event: MessageReceivedEvent, username: String) = runIfClanStaff(event) {
    api.addCard(mainGameId!!, username).execute() //todo error checking...
    sendCard(event.channel, username)
}

private fun completeSquare(event: MessageReceivedEvent, username: String, square: Int) = runIfClanStaff(event) {
    //todo
}

private fun sendWinners(channel: MessageChannel) {
    //todo
}

private fun sendCard(channel: MessageChannel, username: String) {
    val response = api.getCardImage(mainGameId!!, username).execute().body() //todo error checking!!
    channel.sendFile(response?.byteStream()!!, "bingocard.png").queue()
}