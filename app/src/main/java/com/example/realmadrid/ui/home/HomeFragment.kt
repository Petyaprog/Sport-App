package com.example.realmadrid.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var historyLayout: LinearLayout

    private var allMatches: List<Match> = emptyList()
    private var currentMatchIndex = 0

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMatches()
        historyButton.setOnClickListener { showHistory() }
    }

    private fun loadMatches() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMatches(
                    teamId = 76,
                    from = "2025-02-07",
                    to = "2025-11-09",
                    apiKey = "fbeb2c690d9967cfb237f410b95c9658dd64bdbf8143a629f0ffb56c216ebe64"
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

    private fun updateUI(match: Match) {
        val matchStatus = match.match_status
        val scoreText = if (matchStatus == "") {
            "${match.match_date} ${match.match_time}"
        } else {
            "${match.match_hometeam_name} ${match.match_hometeam_score} - ${match.match_awayteam_score} ${match.match_awayteam_name}"
        }
        matchScoreTextView.text = scoreText

        Glide.with(requireContext()).load(match.team_home_badge).into(homeTeamLogo)
        Glide.with(requireContext()).load(match.team_away_badge).into(awayTeamLogo)

        val goalsText = match.goalscorer.joinToString("\n") { goal ->
            when {
                !goal.home_scorer.isNullOrEmpty() -> "⚽ ${goal.time}' ${goal.home_scorer} (${match.match_hometeam_name}) ${goal.info}"
                !goal.away_scorer.isNullOrEmpty() -> "⚽ ${goal.time}' ${goal.away_scorer} (${match.match_awayteam_name}) ${goal.info}"
                else -> ""
            }
        }.trim()
        goalsTextView.text = goalsText

        if (matchStatus == "Finished" && currentMatchIndex < allMatches.size - 1) {
            currentMatchIndex++
            updateUI(allMatches[currentMatchIndex])
        }
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    private fun showHistory() {
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
}