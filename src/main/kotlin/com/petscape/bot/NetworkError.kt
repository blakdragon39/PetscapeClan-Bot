package com.petscape.bot

import okhttp3.ResponseBody

class NetworkError(val message: String)

fun ResponseBody.toNetworkError(): NetworkError? {
    return moshi.adapter(NetworkError::class.java).fromJson(this.string())
}
