package com.petscape.bot

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

const val url = "http://localhost:8080"

val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

lateinit var bingoApi: BingoAPI
lateinit var leaderboardApi: LeaderboardAPI

var bingoGameId: String? = null
var leaderboardGameId: String? = null

fun main(args: Array<String>) {
    val configFile = args.firstOrNull()?.let {
        File(it).readText()
    } ?: throw Exception("config file required")

    val config = moshi.adapter(Config::class.java).fromJson(configFile) ?: throw Exception("config required")
    val credentials = Credentials.basic(config.username, config.password)

    val client = OkHttpClient.Builder()
            .addInterceptor(AuthHeaderInterceptor(credentials))
            .build()

    bingoApi = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BingoAPI::class.java)

    leaderboardApi = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LeaderboardAPI::class.java)

    val jda = JDABuilder(AccountType.BOT)

    jda.setToken(config.token)
    jda.addEventListener(object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            val parts = event.message?.contentRaw?.split(" ") ?: emptyList()
            if (event.author?.isBot == false && parts.isNotEmpty()) {
                when (parts[0]) {
                    "!bingo" -> handleBingoCommand(event, parts.subList(1, parts.size))
                    "!leaderboard" -> handleLeaderboardCommand(event, parts.subList(1, parts.size))
                }
            }
        }
    })

    jda.build()
}

private class Config(val token: String, val username: String, val password: String)

private class AuthHeaderInterceptor(private val credentials: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .addHeader("Authorization", credentials)
                .build()
        return chain.proceed(request)
    }
}