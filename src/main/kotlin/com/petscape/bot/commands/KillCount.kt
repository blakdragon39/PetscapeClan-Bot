package com.petscape.bot.commands

import com.petscape.bot.osrsApi
import com.petscape.bot.sendMessage
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

fun handleKillCountCommand(event: MessageReceivedEvent, args: List<String>) {
    if (args.isNotEmpty()) {
        val username = args.joinToString(" ")
        sendKillCount(event.channel, username)
    } else {
        sendHelpMessage(event.channel)
    }
}

private fun sendKillCount(channel: MessageChannel, username: String) {
    try {
        val scores = getHighScores(username) ?: run {
            sendMessage(channel, "Hiscores not found for $username")
            return
        }

        val kc = petScapeBossKc(scores)
        sendMessage(channel, "$username KC: $kc")
    } catch (e: Exception) {
        e.printStackTrace()
        sendMessage(channel, e.message)
    }
}

private fun sendHelpMessage(channel: MessageChannel) {
    channel.sendMessage("""
        Counts your boss kill count for Petscape rankup purposes, including multiplying Chambers of Xeric kill count by 2, and Theatre of Blood kill count by 4
    """.trimIndent()).queue()
}

private fun getHighScores(name: String): Highscores? {
    val response = osrsApi.getHiscores(name).execute()
    val data = response.body()?.string() ?: return null
    return Highscores(data)
}

private fun petScapeBossKc(scores: Highscores): Int {
    val bosses = Index.values().filter { Tag.boss in it.tags } - arrayOf(Index.cox, Index.coxcm, Index.tob)
    return bosses.sumBy { scores.score(it) } + (scores.score(Index.cox) * 2) + (scores.score(Index.coxcm) * 4) + (scores.score(Index.tob) * 4)
}