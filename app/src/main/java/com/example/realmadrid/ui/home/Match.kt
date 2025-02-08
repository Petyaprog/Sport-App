package com.example.realmadrid.ui.home

data class Match(
    val match_id: String,
    val country_id: String,
    val country_name: String,
    val league_id: String,
    val league_name: String,
    val match_date: String,
    val match_status: String,
    val match_time: String,
    val match_hometeam_id: String,
    val match_hometeam_name: String,
    val match_hometeam_score: String,
    val match_awayteam_name: String,
    val match_awayteam_id: String,
    val match_awayteam_score: String,
    val goalscorer: List<Goal>,
    val substitutions: Substitutions,
    val cards: List<Card>,
    val lineup: Lineup,
    val statistics: List<Statistic>
)
