package com.example.realmadrid.ui.home

data class TeamLineup(
    val starting_lineups: List<Player>,
    val substitutes: List<Player>,
    val coach: List<Coach>,
    val missing_players: List<Any>
)
