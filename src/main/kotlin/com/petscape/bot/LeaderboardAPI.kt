package com.petscape.bot

import com.petscape.bot.models.GameId
import com.petscape.bot.models.leaderboard.LeaderboardGame
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LeaderboardAPI {

    @GET
    fun getAllLeaderboards(): Call<List<GameId>>

    @GET
    fun getLeaderboard(gameId: String): Call<LeaderboardGame>

    @POST
    fun newLeaderboard(
            @Query("name") name: String,
            @Body body: RequestBody
    ): Call<LeaderboardGame>

    @POST
    fun addSubmission(
            @Query("game_id") gameId: String,
            @Query("username") username: String,
            @Query("boss") boss: String,
            @Query("item") item: String,
            @Query("proof") proof: String
    ): Call<LeaderboardGame>
}