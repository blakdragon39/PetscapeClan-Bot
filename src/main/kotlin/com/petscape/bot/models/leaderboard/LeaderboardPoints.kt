package com.petscape.bot.models.leaderboard

import com.petscape.bot.models.Boss
import com.petscape.bot.models.Drop

class LeaderboardPoints(
        val boss: Boss,
        val drop: Drop,
        val points: Int
)