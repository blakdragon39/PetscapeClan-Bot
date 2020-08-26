package com.petscape.bot.models

import com.squareup.moshi.Json

data class Drop(
        val item: String,
        val file: String,
        @Json(name = "drop_rate") val dropRate: Int
)