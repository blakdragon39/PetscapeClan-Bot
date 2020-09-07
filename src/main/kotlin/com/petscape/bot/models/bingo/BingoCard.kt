package com.petscape.bot.models.bingo

class BingoCard(
        val id: String,
        val username: String,
        val notes: String,
        val squares: List<BingoSquare>
)