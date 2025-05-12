package com.example.realmadrid.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.realmadrid.databinding.FragmentHomeBinding
import com.example.realmadrid.databinding.ItemMatchHistoryBinding
import com.example.realmadrid.databinding.StatisticsMatchBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var allMatches: List<Match> = emptyList()
    private var currentMatchIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        loadMatches()
    }

    private fun setupClickListeners() {
        binding.historyButton.setOnClickListener {
            showHistory()
            binding.searchLayout.visibility = View.VISIBLE
        }

        binding.statsButton.setOnClickListener {
            showStats()
            binding.searchLayout.visibility = View.GONE
        }

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                filterMatches(query)
            } else {
                Toast.makeText(requireContext(), "Введите название команды", Toast.LENGTH_SHORT).show()
            }
        }

        binding.showAllButton.setOnClickListener {
            showAllMatches()
        }
    }

    private fun filterMatches(query: String) {
        val filteredMatches = allMatches.filter {
            it.match_hometeam_name.contains(query, ignoreCase = true) ||
                    it.match_awayteam_name.contains(query, ignoreCase = true)
        }

        if (filteredMatches.isEmpty()) {
            Toast.makeText(requireContext(), "Матчи не найдены", Toast.LENGTH_SHORT).show()
        } else {
            showFilteredMatches(filteredMatches)
        }
    }

    private fun showFilteredMatches(matches: List<Match>) {
        binding.historyLayout.removeAllViews()
        matches.forEach { match ->
            addMatchToHistoryLayout(match)
        }
    }

    private fun showAllMatches() {
        binding.historyLayout.removeAllViews()
        allMatches.forEach { match ->
            addMatchToHistoryLayout(match)
        }
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
        binding.leagueName.text = match.league_name
        binding.time.text = if (match.match_live == "0") "" else match.match_status

        binding.matchScoreTextView.text = if (match.match_status == "") {
            "${match.match_date} ${match.match_time}"
        } else {
            "${match.match_hometeam_name} ${match.match_hometeam_score} - ${match.match_awayteam_score} ${match.match_awayteam_name}"
        }

        Glide.with(requireContext()).load(match.team_home_badge).into(binding.homeTeamLogo)
        Glide.with(requireContext()).load(match.team_away_badge).into(binding.awayTeamLogo)

        val goalsText = match.goalscorer.joinToString("\n") { goal ->
            when {
                !goal.home_scorer.isNullOrEmpty() -> "⚽ ${goal.time}' ${goal.home_scorer} (${match.match_hometeam_name}) ${goal.info}"
                !goal.away_scorer.isNullOrEmpty() -> "⚽ ${goal.time}' ${goal.away_scorer} (${match.match_awayteam_name}) ${goal.info}"
                else -> ""
            }
        }.trim()
        binding.goalsTextView.text = goalsText

        if (match.match_status == "Finished" && currentMatchIndex < allMatches.size - 1) {
            currentMatchIndex++
            updateUI(allMatches[currentMatchIndex])
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addMatchToHistoryLayout(match: Match) {
        val itemBinding = ItemMatchHistoryBinding.inflate(layoutInflater)
        Glide.with(requireContext()).load(match.team_home_badge).into(itemBinding.historyHomeTeamLogo)
        Glide.with(requireContext()).load(match.team_away_badge).into(itemBinding.historyAwayTeamLogo)
        itemBinding.historyScoreTextView.text =
            "${match.match_hometeam_name} ${match.match_hometeam_score} - ${match.match_awayteam_score} ${match.match_awayteam_name}"

        binding.historyLayout.addView(itemBinding.root)
    }

    private fun showHistory() {
        binding.StatisticsLayout.visibility = View.GONE
        binding.historyLayout.visibility = View.VISIBLE
        binding.historyLayout.removeAllViews()

        allMatches.forEach { match ->
            addMatchToHistoryLayout(match)
        }
    }

    private fun showStats() {
        binding.historyLayout.visibility = View.GONE
        binding.StatisticsLayout.visibility = View.VISIBLE
        binding.StatisticsLayout.removeAllViews()

        val statsBinding = StatisticsMatchBinding.inflate(layoutInflater)
        // Здесь можно заполнить данные статистики, если они есть
        binding.StatisticsLayout.addView(statsBinding.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}