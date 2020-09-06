package com.petscape.bot

import com.petscape.bot.exceptions.GameNotSetException
import com.petscape.bot.models.BingoSettings
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException

fun handleBingoCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        //todo validate arguments
        val commandArgs = args.subList(1, args.size)
        when (args[0]) {
            "list" -> sendBingoGamesList(event)
            "setgame" -> setGame(event, commandArgs[0])
            "newgame" -> {
                val settings = BingoSettings(commandArgs, event.message) //todo exceptions
                if (settings.attachment != null) {
                    newCustomGame(event, settings)
                } else {
                    newGame(event, settings)
                }
            }
            "addcard" -> {
                val username = commandArgs.joinToString(" ")
                addCard(event, username)
            }
            "completesquare" -> {
                val square = commandArgs[0].toInt()
                val username = commandArgs.subList(1, commandArgs.size).joinToString(" ")
                completeSquare(event, username, square)
            }
            "winners" -> sendWinners(event.channel)
            "getcard" -> {
                val username = commandArgs.joinToString(" ")
                sendCard(event.channel, username)
            }
            else -> {
                //todo send error about wrong command
            }
        }
    } else {
        sendBingoCommands(event.channel)
    }
}

private fun runIfClanStaff(event: MessageReceivedEvent, function: () -> Unit) {
    if (event.member.roles.any { it.name == "Clan Staff" }) {
        function()
    } else {
        sendMessage(event.channel, "Permission not granted")
    }
}

private fun sendBingoCommands(channel: MessageChannel) {
    channel.sendMessage("""
        Available commands:
        - list **Clan Staff only**
        - setgame [gameId] **Clan Staff only**
        - newgame [bosses, items, combined] [freespace] [cardsmatch] [game name] **Clan Staff only**
        - newgame [attach file with card] **Clan Staff only**
        - addcard [username] **Clan Staff only**
        - completesquare [square ID 1-25] [username] **Clan Staff only**
        - winners
        - getcard [username]
    """.trimIndent()).queue()

    //todo newcustomgame
    //todo updatenotes
}

private fun sendBingoGamesList(event: MessageReceivedEvent) = runIfClanStaff(event) {
    try {
        val response = api.getAllGames().execute()

        if (response.isSuccessful) {
            val gameIds = response.body()
            val message = if (gameIds == null || gameIds.isEmpty()) {
                "No games found"
            } else {
                gameIds.map { "${it.name} ID: ${it.id}" }
                        .joinToString("\n")
            }
            sendMessage(event.channel, message)
        } else {
            sendError(event.channel, response.errorBody())
        }
    } catch (e: IOException) {
        sendError(event.channel, e)
    }
}

private fun setGame(event: MessageReceivedEvent, gameId: String) = runIfClanStaff(event) {
    mainGameId = gameId
    sendMessage(event.channel, "Game ID set")
}

private fun newGame(event: MessageReceivedEvent, settings: BingoSettings) = runIfClanStaff(event) {
    try {
        val response = api.newBingoGame(settings.gameName, settings.type, settings.freeSpace, settings.cardsMatch).execute()

        if (response.isSuccessful) {
            val game = response.body()
            mainGameId = game?.id
            event.channel.sendMessage("Game ${settings.gameName} created and started").queue()
        } else {
            sendError(event.channel, response.errorBody())
        }
    } catch (e: IOException) {
        sendError(event.channel, e)
    }
}

private fun newCustomGame(event: MessageReceivedEvent, settings: BingoSettings) = runIfClanStaff(event) {
    if (settings.attachment != null) {
        val file = File("attachment.temp")
        val success = settings.attachment.download(file)

        if (!success) {
            sendMessage(event.channel, "Unable to read attached card")
            return@runIfClanStaff
        } else {
            try {
                val contents = file.readText()
                val requestBody = RequestBody.create(MediaType.parse("text/json"), contents)
                val response = api.newCustomGame(settings.gameName, requestBody).execute()

                if (response.isSuccessful) {
                    val game = response.body()
                    mainGameId = game?.id
                    sendMessage(event.channel, "Game ${settings.gameName} created and started")
                } else {
                    sendError(event.channel, response.errorBody())
                }
            } catch (e: IOException) {
                sendError(event.channel, e)
            }
        }

        file.delete()
    }
}

private fun addCard(event: MessageReceivedEvent, username: String) = runIfClanStaff(event) {
    try {
        val response = api.addCard(mainGameId ?: throw GameNotSetException(), username).execute()

        if (response.isSuccessful) {
            sendCard(event.channel, username)
        } else {
            sendError(event.channel, response.errorBody())
        }
    } catch (e: IOException) {
        sendError(event.channel, e)
    } catch (e: GameNotSetException) {
        sendError(event.channel, e)
    }
}

private fun completeSquare(event: MessageReceivedEvent, username: String, square: Int) = runIfClanStaff(event) {
    try {
        val card = api.getCard(mainGameId ?: throw GameNotSetException(), username).execute().body()
                ?: return@runIfClanStaff sendMessage(event.channel, "Card not found for player $username")

        val squareId = card.squares[square - 1].id

        api.completeSquare(mainGameId ?: throw GameNotSetException(), card.id, squareId).execute()
        sendCard(event.channel, username)
    } catch (e: IOException) {
        sendError(event.channel, e)
    } catch (e: GameNotSetException) {
        sendError(event.channel, e)
    }
}

private fun sendWinners(channel: MessageChannel) {
    try {
        val response = api.getWinners(mainGameId ?: throw GameNotSetException()).execute()

        if (response.isSuccessful) {
            val winningCards = response.body()

            if (winningCards?.isEmpty() == true) {
                sendMessage(channel, "There are no winners yet")
            } else {
                var message = ""
                winningCards?.forEachIndexed { index, card ->
                    message += "${index + 1}. ${card.username}"
                }
                sendMessage(channel, message)
            }
        } else {
            sendError(channel, response.errorBody())
        }
    } catch (e: IOException) {
        sendError(channel, e)
    } catch (e: GameNotSetException) {
        sendError(channel, e)
    }
}

private fun sendCard(channel: MessageChannel, username: String) {
    try {
        val response = api.getCardImage(mainGameId ?: throw GameNotSetException(), username).execute()

        if (response.isSuccessful) {
            val image = response.body()
            channel.sendFile(image?.byteStream()!!, "bingocard.png").queue()
        } else {
            sendError(channel, response.errorBody())
        }
    } catch (e: IOException) {
        sendError(channel, e)
    } catch (e: GameNotSetException) {
        sendError(channel, e)
    }
}

private fun sendError(channel: MessageChannel, e: Exception) {
    e.printStackTrace()
    sendMessage(channel, e.message)
}

private fun sendError(channel: MessageChannel, error: ResponseBody?) {
    sendMessage(channel, error?.string())
}

private fun sendMessage(channel: MessageChannel, message: String?) {
    if (message != null) {
        channel.sendMessage(message).queue()
    } else {
        channel.sendMessage("A server error occurred").queue()
    }
}