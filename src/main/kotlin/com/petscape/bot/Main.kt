package com.petscape.bot

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.io.File

val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

val client = OkHttpClient()
lateinit var credentials: String

var mainGameId: String? = null

fun main(args: Array<String>) {
    val configFile = args.firstOrNull()?.let {
        File(it).readText()
    } ?: throw Exception("config file required")

    val config = moshi.adapter(Config::class.java).fromJson(configFile) ?: throw Exception("config required")

    credentials = Credentials.basic(config.username, config.password)

    val jda = JDABuilder(AccountType.BOT)
    jda.setToken(config.token)

    jda.addEventListener(object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            val parts = event.message?.contentRaw?.split(" ") ?: emptyList()
            if (event.author?.isBot == false && parts.isNotEmpty()) {
                when (parts[0]) {
                    "!bingo" -> handleBingoCommand(event, parts.subList(1, parts.size))
                }
            }
        }
    })

    jda.build()
}

private class Config(val token: String, val username: String, val password: String)
