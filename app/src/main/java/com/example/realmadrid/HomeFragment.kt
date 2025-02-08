package com.example.realmadrid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.ui.home.Match
import com.example.realmadrid.ui.home.RetrofitClient
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var matchScoreTextView: TextView
    private lateinit var goalsTextView: TextView
//
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        matchScoreTextView = view.findViewById(R.id.match_score_text_view)
        goalsTextView = view.findViewById(R.id.goals_text_view)
        return view
    }
//
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMatches()
    }
//
//    @SuppressLint("SetTextI18n")
    private fun loadMatches() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMatches(
                    leagueId = 302,
                    teamId = 76,
                    from = "2025-02-07",
                    to = "2025-10-09",
                    apiKey = "fbeb2c690d9967cfb237f410b95c9658dd64bdbf8143a629f0ffb56c216ebe64"
                )
                if (response.isNotEmpty()) {
                    updateUI(response[0]) // Берем первый матч из списка
                } else {
                    Log.e("HomeFragment", "No matches found")
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading matches", e)
            }
        }
    }

    private fun updateUI(match: Match) {
        // Проверка на наличие данных
        if (match.match_hometeam_name.isEmpty() || match.match_awayteam_name.isEmpty()) {
            Log.e("HomeFragment", "Match data is incomplete")
            return
        }

        // Обновляем счет или дату матча
        val scoreText = if (match.match_status.isEmpty()) {
            "Матч начнется: ${match.match_date} ${match.match_time}"
        } else {
            "${match.match_hometeam_name} ${match.match_hometeam_score} - ${match.match_awayteam_score} ${match.match_awayteam_name}"
        }
        matchScoreTextView.text = scoreText

        // Обновляем информацию о голах
        val goalsText = match.goalscorer.joinToString("\n") { goal ->
            "${goal.time}' ${goal.home_scorer ?: goal.away_scorer}"
        }
        goalsTextView.text = goalsText
    }
}