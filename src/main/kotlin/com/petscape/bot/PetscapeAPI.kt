package com.petscape.bot

import com.petscape.bot.models.*
import com.petscape.bot.models.requests.SquareRequest
import com.petscape.bot.models.requests.UsernameRequest
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PetscapeAPI {

    @GET("api/clanMembers/runescapeName")
    fun getClanMember(@Query("runescapeName") runescapeName: String): Call<ClanMember>

    @PUT("api/clanMembers/{id}/update")
    fun pingClanMember(@Path("id") clanMemberId: String): Call<ClanMember>

    @GET("/api/bingo")
    fun getAllGames(): Call<List<BingoGameId>>

//    @POST("bingo/new_game")
//    fun newBingoGame(
//            @Query("name") name: String,
//            @Query("type") type: GameType,
//            @Query("free_space") freeSpace: Boolean,
//            @Query("cards_match") cardsMatch: Boolean
//    ): Call<BingoGame>

    @POST("api/bingo/new_custom_game")
    fun newCustomGame(@Body body: RequestBody): Call<BingoGame>

    @POST("api/bingo/{id}")
    fun addCard(
        @Path("id") gameId: String,
        @Body request: UsernameRequest
    ): Call<BingoCard>

    @POST("api/bingo/{id}/complete")
    fun completeSquare(
        @Path("id") gameId: String,
        @Body request: SquareRequest
    ): Call<BingoCard>

    @POST("api/bingo/{id}/uncomplete")
    fun uncompleteSquare(
        @Path("id") gameId: String,
        @Body request: SquareRequest
    ): Call<BingoCard>

    @GET("api/bingo/{id}/players")
    fun getPlayers(@Path("id") gameId: String): Call<List<String>>

    @GET("api/bingo/{id}/winners")
    fun getWinners(@Path("id") gameId: String): Call<List<BingoCard>>

    @GET("api/bingo/{id}/{username}")
    fun getCard(
            @Path("id") gameId: String,
            @Path("username") username: String
    ): Call<BingoCard>

    @GET("api/bingo/{id}/{username}/image")
    fun getCardImage(
        @Path("id") gameId: String,
        @Path("username") username: String
    ): Call<ResponseBody>
}
