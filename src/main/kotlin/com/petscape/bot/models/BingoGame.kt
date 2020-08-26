package com.petscape.bot.models

class BingoGame(
        val id: String,
        val name: String,
        val type: GameType,
        val freeSpace: Boolean,
        val parentCard: List<BingoSquare>,
        val cards: List<BingoCard>
)