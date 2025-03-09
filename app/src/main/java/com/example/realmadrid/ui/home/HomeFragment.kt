package com.example.realmadrid.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.realmadrid.R
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var matchScoreTextView: TextView
    private lateinit var goalsTextView: TextView
    private lateinit var homeTeamLogo: ImageView
    private lateinit var awayTeamLogo: ImageView
    private lateinit var historyButton: Button
    private lateinit var statsButton: Button
    private lateinit var historyLayout: LinearLayout
    private lateinit var statisticsLayout: LinearLayout
    private lateinit var league_name: TextView
    private lateinit var timeTextView: TextView

    private var allMatches: List<Match> = emptyList()
    private var currentMatchIndex = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        matchScoreTextView = view.findViewById(R.id.match_score_text_view)
        goalsTextView = view.findViewById(R.id.goals_text_view)
        homeTeamLogo = view.findViewById(R.id.home_team_logo)
        awayTeamLogo = view.findViewById(R.id.away_team_logo)
        historyButton = view.findViewById(R.id.history_button)
        historyLayout = view.findViewById(R.id.history_layout)
        statsButton = view.findViewById(R.id.stats_button)
        statisticsLayout = view.findViewById(R.id.Statistics_layout)
        league_name = view.findViewById(R.id.league_name) // Находим textView2
        timeTextView = view.findViewById(R.id.time) // Находим time

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMatches()
        historyButton.setOnClickListener { showHistory() }
//        statsButton.setOnClickListener { showStats() }
    }

    private fun loadMatches() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMatches(
                    teamId = 2986,
                    from = "2024-08-07",
                    to = "2025-12-09",
                    apiKey = "fbeb2c690d9967cfb237f410b95c9658dd64bdbf8143a629f0ffb56c216ebe64",
                    timezone = "Europe/Moscow"
                )
                if (response.isNotEmpty()) {
                    allMatches = response
                    updateUI(response[currentMatchIndex])
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(match: Match) {
        // Устанавливаем название лиги
        league_name.text = match.league_name

        // Устанавливаем текущую минуту матча
        timeTextView.text = when {
            match.match_live == "0" -> ""
            else -> match.match_status // Текущая минута матча
        }

        // Устанавливаем счет матча
        val scoreText = if (match.match_status == "") {
            "${match.match_date} ${match.match_time}"
        } else {
            "${match.match_hometeam_name} ${match.match_hometeam_score} - ${match.match_awayteam_score} ${match.match_awayteam_name}"
        }
        matchScoreTextView.text = scoreText

        // Загружаем логотипы команд
        Glide.with(requireContext()).load(match.team_home_badge).into(homeTeamLogo)
        Glide.with(requireContext()).load(match.team_away_badge).into(awayTeamLogo)

        // Устанавливаем список голов
        val goalsText = match.goalscorer.joinToString("\n") { goal ->
            when {
                !goal.home_scorer.isNullOrEmpty() -> "⚽ ${goal.time}' ${goal.home_scorer} (${match.match_hometeam_name}) ${goal.info}"
                !goal.away_scorer.isNullOrEmpty() -> "⚽ ${goal.time}' ${goal.away_scorer} (${match.match_awayteam_name}) ${goal.info}"
                else -> ""
            }
        }.trim()
        goalsTextView.text = goalsText

        // Переход к следующему матчу, если текущий завершен
        if (match.match_status == "Finished" && currentMatchIndex < allMatches.size - 1) {
            currentMatchIndex++
            updateUI(allMatches[currentMatchIndex])
        }
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun showHistory() {
        statisticsLayout.visibility = View.GONE
        historyLayout.visibility = View.VISIBLE
        historyLayout.removeAllViews()
        allMatches.forEach { match ->
            val matchView = layoutInflater.inflate(R.layout.item_match_history, historyLayout, false)
            val homeTeamLogo = matchView.findViewById<ImageView>(R.id.history_home_team_logo)
            val awayTeamLogo = matchView.findViewById<ImageView>(R.id.history_away_team_logo)
            val scoreTextView = matchView.findViewById<TextView>(R.id.history_score_text_view)

            Glide.with(requireContext()).load(match.team_home_badge).into(homeTeamLogo)
            Glide.with(requireContext()).load(match.team_away_badge).into(awayTeamLogo)
            scoreTextView.text = "${match.match_hometeam_name} ${match.match_hometeam_score} - ${match.match_awayteam_score} ${match.match_awayteam_name}"

            historyLayout.addView(matchView)
        }
    }

//    @SuppressLint("SetTextI18n", "MissingInflatedId")
//    private fun showStats() {
//        historyLayout.visibility = View.GONE
//        statisticsLayout.visibility = View.VISIBLE
//        statisticsLayout.removeAllViews()
//
//        // Инициализация элементов интерфейса
//        val statsView = layoutInflater.inflate(R.layout.statistics_match, statisticsLayout, false)
//        val statsTable = statsView.findViewById<TableLayout>(R.id.statistics_table)
//        val statsHomeTeamLogo = statsView.findViewById<ImageView>(R.id.stats_home_team_logo)
//        val statsAwayTeamLogo = statsView.findViewById<ImageView>(R.id.stats_away_team_logo)
//        val nameStadium = statsView.findViewById<TextView>(R.id.name_stadium)
//
//        // Загрузка логотипов команд
//        Glide.with(requireContext()).load(allMatches[currentMatchIndex].team_home_badge).into(statsHomeTeamLogo)
//        Glide.with(requireContext()).load(allMatches[currentMatchIndex].team_away_badge).into(statsAwayTeamLogo)
//
//        // Установка названия стадиона
//        nameStadium.text = allMatches[currentMatchIndex].match_stadium
//
//        // Заполнение данных статистики
//        allMatches[currentMatchIndex].statistics.forEach { stat ->
//            val tableRow = layoutInflater.inflate(R.layout.statistics_match, statsTable, false)
//            val indicatorStats = tableRow.findViewById<TextView>(R.id.indicator_stats)
//            val indicatorHomeTeam = tableRow.findViewById<TextView>(R.id.indicator_home_team)
//            val indicatorAwayTeam = tableRow.findViewById<TextView>(R.id.indicator_away_team)
//
//            indicatorStats.text = stat.type
//            indicatorHomeTeam.text = stat.home
//            indicatorAwayTeam.text = stat.away
//
//            statsTable.addView(tableRow)
//        }
//        statisticsLayout.addView(statsView)
//    }
}