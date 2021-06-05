package com.petscape.bot

import com.petscape.bot.commands.handleAchievementsCommand
import com.petscape.bot.commands.handleClanMemberCommand
import com.petscape.bot.commands.handleHelpCommand
import com.petscape.bot.commands.handlePetsCommand
import com.petscape.bot.models.AchievementType
import com.petscape.bot.models.PetType
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val url = "http://localhost:8080"

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(LocalDateAdapter())
    .add(LocalDateTimeAdapter())
    .add(PetType::class.java, EnumJsonAdapter.create(PetType::class.java).withUnknownFallback(PetType.Unknown))
    .add(AchievementType::class.java, EnumJsonAdapter.create(AchievementType::class.java).withUnknownFallback(AchievementType.Unknown))
    .build()

lateinit var petscapeApi: PetscapeAPI
lateinit var osrsApi: OsrsAPI

var mainGameId: String? = null

fun main(args: Array<String>) {
    val configFile = args.firstOrNull()?.let {
        File(it).readText()
    } ?: throw Exception("config file required")

    val config = moshi.adapter(Config::class.java).fromJson(configFile) ?: throw Exception("config required")
    val credentials = Credentials.basic(config.username, config.password)

    val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

    val authClient = client.addInterceptor(AuthHeaderInterceptor(credentials))

    petscapeApi = Retrofit.Builder()
            .baseUrl(url)
            .client(authClient.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PetscapeAPI::class.java)

    osrsApi = Retrofit.Builder()
            .baseUrl("https://secure.runescape.com/m=hiscore_oldschool/")
            .client(client.build())
            .build()
            .create(OsrsAPI::class.java)

    val jdaBuilder = JDABuilder.createDefault(config.token)
    jdaBuilder.addEventListeners(object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            val message = event.message.contentRaw
            val parts = message.split(" ")
            if (!event.author.isBot && parts.isNotEmpty()) {
                when {
                    message.toLowerCase().startsWith("ps!help") -> handleHelpCommand(event)
                    message.toLowerCase().startsWith("ps!clanmember") -> handleClanMemberCommand(event, parts.subList(1, parts.size))
                    message.toLowerCase().startsWith("ps!pets") -> handlePetsCommand(event, parts.subList(1, parts.size))
                    message.toLowerCase().startsWith("ps!achievements") -> handleAchievementsCommand(event, parts.subList(1, parts.size))
//                    message.startsWith("!bingo") -> handleBingoCommand(event, parts.subList(1, parts.size))
//                    message.startsWith("!kc") -> handleKillCountCommand(event, parts.subList(1, parts.size))
//                    message.startsWith("!createpoll") -> handleCreatePollCommand(event)
                }
            }
        }
    })

    val jda = jdaBuilder.build().awaitReady()
    jda.presence.setPresence(Activity.playing("ps!help"), false)
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

class LocalDateAdapter {
    @ToJson fun toJson(localDate: LocalDate): String {
        return localDate.toString()
    }

    @FromJson fun fromJson(json: String): LocalDate {
        return LocalDate.parse(json)
    }
}

class LocalDateTimeAdapter {
    @ToJson fun toJson(localDateTime: LocalDateTime): String {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    }

    @FromJson fun fromJson(json: String): LocalDateTime {
        return LocalDateTime.parse(json, DateTimeFormatter.ISO_DATE_TIME)
    }
}
