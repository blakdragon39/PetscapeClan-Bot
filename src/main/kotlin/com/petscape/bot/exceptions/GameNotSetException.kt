package com.petscape.bot.exceptions

class GameNotSetException : Exception("No game currently running. Admin can set a game with the !bingo setgame command")