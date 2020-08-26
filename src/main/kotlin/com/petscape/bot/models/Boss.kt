package com.petscape.bot.models

import com.squareup.moshi.Json

data class Boss(
        val name: String,
        val file: String,
        val wilderness: Boolean,
        @Json(name = "slayer_level")val slayerLevel: Int
)