package com.petscape.bot.models.leaderboard

class LeaderboardGame(
        val id: String,
        val name: String,
        val points: List<LeaderboardPoints>,
        val submissions: List<LeaderboardSubmissions>
)