package com.petscape.bot.commands

import com.petscape.bot.models.AchievementType
import com.petscape.bot.models.ClanMember
import com.petscape.bot.models.PetType
import com.petscape.bot.petscapeApi
import com.petscape.bot.sendMessage
import com.petscape.bot.toNetworkError
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.min

const val MAX_POINTS_BOSSES = 30
const val MAX_POINTS_TIME = 24

fun handleHelpCommand(event: MessageReceivedEvent) {
    sendMessage(event.channel, """
        Commands:
        ps!clanmember [name]
        ps!pets [name]
        ps!achievements [name]
        ps!bingo
        ps!createpoll
    """.trimIndent())
}

fun handleClanMemberCommand(event: MessageReceivedEvent, args: List<String>) {
    getClanMember(event, args.toRunescapeName())?.let { clanMember ->
        val bossKcPoints = min(clanMember.bossKc / 1000, MAX_POINTS_BOSSES)
        val timePoints = min(
            ChronoUnit.MONTHS.between(clanMember.joinDate, LocalDate.now()).toInt(),
            MAX_POINTS_TIME
        )
        val petPoints = clanMember.pets.size
        val achievementPoints = clanMember.achievements.size

        val message = """
            **${clanMember.runescapeName}:** ${clanMember.points} Points
            Rank: ${clanMember.rank.label}
            Join Date: ${clanMember.joinDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))} ($timePoints points)
            Boss KC: ${clanMember.bossKc} ($bossKcPoints points)
            Pets: $petPoints
            Achievements: $achievementPoints
        """.trimIndent()

        sendMessage(event.channel, message)
    }
}

fun handlePetsCommand(event: MessageReceivedEvent, args: List<String>) {
    getClanMember(event, args.toRunescapeName())?.let { clanMember ->
        val header = "**Pets** (${clanMember.pets.size}/${PetType.validPets().size})"
        val pets = clanMember.pets.sortedBy { it.type.ordinal }
        val petsMessage = pets.joinToString("\n") { it.label }
        val message = "$header\n$petsMessage"
        sendMessage(event.channel, message)
    }
}

fun handleAchievementsCommand(event: MessageReceivedEvent, args: List<String>) {
    getClanMember(event, args.toRunescapeName())?.let { clanMember ->
        val header = "**Achievements** (${clanMember.achievements.size}/${AchievementType.validAchievements().size})"

        val achievements = clanMember.achievements.sortedBy { it.type.ordinal }
        val achievementsMessage = achievements.joinToString("\n") { it.label }
        val message = "$header\n$achievementsMessage"
        sendMessage(event.channel, message)
    }
}

fun List<String>.toRunescapeName() = joinToString(" ")

private fun getClanMember(event: MessageReceivedEvent, runescapeName: String): ClanMember? {
    try {
        var response = petscapeApi.getClanMember(runescapeName).execute()

        return if (response.isSuccessful) {
            response = petscapeApi.pingClanMember(response.body()!!.id!!).execute()

            if (response.isSuccessful) {
                response.body()!!
            } else {
                sendMessage(event.channel, response.errorBody()?.toNetworkError()?.message)
                return null
            }
        } else {
            sendMessage(event.channel, response.errorBody()?.toNetworkError()?.message)
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        sendMessage(event.channel, "Error: ${e.message}")
        return null
    }
}
