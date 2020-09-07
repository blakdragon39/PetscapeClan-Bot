package com.petscape.bot.models.bingo

import com.petscape.bot.models.Boss
import com.petscape.bot.models.Drop

class BingoSquare(
        val id: String,
        val completed: Boolean,
        val task: String?,
        val boss: Boss?,
        val item: Drop?
)