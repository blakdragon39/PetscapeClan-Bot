package com.petscape.bot

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OsrsAPI {
    @GET("index_lite.ws")
    fun getHiscores(@Query("player") username: String): Call<ResponseBody>
}