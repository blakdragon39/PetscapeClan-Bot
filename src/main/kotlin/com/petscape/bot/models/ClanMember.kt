package com.petscape.bot.models

import java.time.LocalDate

class ClanMember(
    val id: String? = null,
    val runescapeName: String,
    val rank: Rank,
    val joinDate: LocalDate,
    val lastSeen: LocalDate?,
    val bossKc: Int,
    val pets: List<Pet>,
    val achievements: List<Achievement>,
    val points: Int,
    val alts: List<String> = listOf()
)
