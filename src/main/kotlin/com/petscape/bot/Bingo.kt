package com.petscape.bot

import com.petscape.bot.exceptions.GameNotSetException
import com.petscape.bot.exceptions.InvalidGameTypeException
import com.petscape.bot.models.bingo.BingoSettings
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException

fun handleBingoCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        val commandArgs = args.subList(1, args.size)
        when (args[0]) {
            "list" -> sendBingoGamesList(event)
            "setgame" -> {
                try {
                    setGame(event, commandArgs[0])
                } catch (e: IndexOutOfBoundsException) {
                    sendMessage(event.channel, "No game ID supplied")
                }
            }
            "newgame" -> {
                try {
                    val settings = BingoSettings(commandArgs, event.message)
                    if (settings.attachment != null) {
                        newCustomGame(event, settings)
                    } else {
                        newGame(event, settings)
                    }
                } catch (e: InvalidGameTypeException) {
                    sendMessage(event.channel, "Invalid game type supplied")
                } catch (e: IndexOutOfBoundsException) {
                    sendMessage(event.channel, "Too few argument supplied")
                }
            }
            "addcard" -> {
                val username = commandArgs.joinToString(" ")
                if (username.isBlank()) {
                    sendMessage(event.channel, "No username supplied")
                } else {
                    addCard(event, username)
                }
            }
            "completesquare" -> {
                try {
                    val square = commandArgs[0].toInt()
                    val username = commandArgs.subList(1, commandArgs.size).joinToString(" ")
                    completeSquare(event, username, square)
                } catch (e: NumberFormatException) {
                    sendMessage(event.channel, "Invalid square number supplied")
                } catch (e:IndexOutOfBoundsException) {
                    sendMessage(event.channel, "Provide a square number from 1 to 25")
                }
            }
            "players" -> sendPlayers(event.channel)
            "winners" -> sendWinners(event.channel)
            "getcard" -> {
                val username = commandArgs.joinToString(" ")
                if (username.isBlank()) {
                    sendMessage(event.channel, "No username supplied")
                } else {
                    sendCard(event.channel, username)
                }
            }
            else -> sendMessage(event.channel, "Unknown command ${args[0]}")
        }
    } else {
        sendBingoCommands(event.channel)
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

    //todo updatenotes?
}

private fun sendBingoGamesList(event: MessageReceivedEvent) = runIfClanStaff(event) {
    try {
        val response = bingoApi.getAllGames().execute()

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
    bingoGameId = gameId
    sendMessage(event.channel, "Game ID set")
}

private fun newGame(event: MessageReceivedEvent, settings: BingoSettings) = runIfClanStaff(event) {
    try {
        val response = bingoApi.newBingoGame(settings.gameName, settings.type, settings.freeSpace, settings.cardsMatch).execute()

        if (response.isSuccessful) {
            val game = response.body()
            bingoGameId = game?.id
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
                val response = bingoApi.newCustomGame(settings.gameName, requestBody).execute()

                if (response.isSuccessful) {
                    val game = response.body()
                    bingoGameId = game?.id
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
        val response = bingoApi.addCard(bingoGameId ?: throw GameNotSetException(), username).execute()

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
        val card = bingoApi.getCard(bingoGameId ?: throw GameNotSetException(), username).execute().body()
                ?: return@runIfClanStaff sendMessage(event.channel, "Card not found for player $username")

        val squareId = card.squares[square - 1].id

        bingoApi.completeSquare(bingoGameId ?: throw GameNotSetException(), card.id, squareId).execute()
        sendCard(event.channel, username)
    } catch (e: IOException) {
        sendError(event.channel, e)
    } catch (e: GameNotSetException) {
        sendError(event.channel, e)
    }
}

private fun sendPlayers(channel: MessageChannel) {
    try {
        val response = bingoApi.getPlayers(bingoGameId ?: throw GameNotSetException()).execute()

        if (response.isSuccessful) {
            val players = response.body()

            if (players?.isEmpty() == true) {
                sendMessage(channel, "No players yet")
            } else {
                var message = ""
                players?.sortedBy { it.toLowerCase() }?.forEach {
                    message += "$it\n"
                }
                sendMessage(channel, message)
            }
        } else {
            sendError(channel, response.errorBody())
        }
    } catch(e: IOException) {
        sendError(channel, e)
    } catch (e: GameNotSetException) {
        sendError(channel, e)
    }
}

private fun sendWinners(channel: MessageChannel) {
    try {
        val response = bingoApi.getWinners(bingoGameId ?: throw GameNotSetException()).execute()

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
        val response = bingoApi.getCardImage(bingoGameId ?: throw GameNotSetException(), username).execute()

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