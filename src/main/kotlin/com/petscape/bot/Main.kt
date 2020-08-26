package com.petscape.bot

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.io.File

val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

fun main(args: Array<String>) {
    val configFile = args.firstOrNull()?.let {
        File(it).readText()
    } ?: throw Exception("config file required")

    val config = moshi.adapter(Config::class.java).fromJson(configFile) ?: throw Exception("config required")

    val jda = JDABuilder(AccountType.BOT)
    jda.setToken(config.token)

    jda.addEventListener(object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent?) {
            event?.channel?.sendMessage("received")?.queue()
        }
    })

    jda.build()
}

private class Config(val token: String)