package com.petscape.bot

class GameNotSetException : Exception("No game currently running. Admin can set a game with the !bingo setgame command")