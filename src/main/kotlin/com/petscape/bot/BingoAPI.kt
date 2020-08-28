package com.petscape.bot

import com.petscape.bot.models.BingoCard
import com.petscape.bot.models.BingoGame
import com.petscape.bot.models.BingoGameId
import com.petscape.bot.models.GameType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BingoAPI {

    @GET("bingo/all")
    fun getAllGames(): Call<List<BingoGameId>>

    @POST("bingo/new_game")
    fun newBingoGame(
            @Query("name") name: String,
            @Query("type") type: GameType,
            @Query("free_space") freeSpace: Boolean,
            @Query("cards_match") cardsMatch: Boolean
    ): Call<BingoGame>

    //todo custom game

    @POST("bingo/add_card")
    fun addCard(
            @Query("id") id: String,
            @Query("username") username: String
    ): Call<BingoCard>

    @POST("bingo/complete_square")
    fun completeSquare(
            @Query("game_id") gameId: String,
            @Query("card_id") cardId: String,
            @Query("square_id") squareId: String
    ): Call<BingoCard>

    //todo notes?

    @GET("bingo/winners")
    fun getWinners(@Query("game_id") gameId: String): Call<List<BingoCard>>

    @GET("bingo/get_card")
    fun getCard(
            @Query("game_id") gameId: String,
            @Query("username") username: String
    ): Call<BingoCard>

    @GET("bingo/get_card_image")
    fun getCardImage(
            @Query("game_id") gameId: String,
            @Query("username") username: String
    ): Call<ResponseBody>
}