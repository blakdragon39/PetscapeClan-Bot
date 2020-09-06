package com.petscape.bot.models

import com.petscape.bot.exceptions.InvalidGameTypeException
import net.dv8tion.jda.core.entities.Message

class BingoSettings(args: List<String>, message: Message) {

    val type: GameType
    val freeSpace: Boolean
    val cardsMatch: Boolean
    val gameName: String
    val attachment: Message.Attachment? = message.attachments.firstOrNull()

    init {
        if (attachment == null) {
            var argIndex = 0
            var arg = args[argIndex]

            type = GameType.values().firstOrNull { it.toString().toLowerCase() == arg.toLowerCase() }
                    ?: throw InvalidGameTypeException(arg)

            argIndex += 1
            arg = args[argIndex]

            if (arg.toLowerCase() == "freespace") {
                freeSpace = true
                argIndex += 1
                arg = args[argIndex]
            } else {
                freeSpace = false
            }

            if (arg.toLowerCase() == "cardsmatch") {
                cardsMatch = true
                argIndex += 1
            } else {
                cardsMatch = false
            }

            gameName = args.subList(argIndex, args.size).joinToString(" ")
        } else {
            type = GameType.OTHER
            freeSpace = false
            cardsMatch = false
            gameName = args.joinToString(" ")
        }
    }
}