package com.petscape.bot.models.leaderboard

import com.petscape.bot.models.Boss
import com.petscape.bot.models.Drop

class LeaderboardSubmissions(
        val username: String,
        val boss: Boss,
        val drop: Drop,
        val proof: String,
        val time: Long
)