package com.petscape.bot.models

class BingoSquare(
        val id: String,
        val completed: Boolean,
        val task: String?,
        val boss: Boss?,
        val item: Drop?
)