package com.petscape.bot.exceptions

class InvalidGameTypeException(gameType: String) : Exception("Invalid game type $gameType")