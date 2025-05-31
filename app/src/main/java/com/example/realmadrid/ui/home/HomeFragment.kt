package com.example.realmadrid.ui.home

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.realmadrid.API.Match
import com.example.realmadrid.API.RetrofitClient
import com.example.realmadrid.databinding.FragmentHomeBinding
import com.example.realmadrid.databinding.ItemMatchHistoryBinding
import com.example.realmadrid.databinding.StatisticsMatchBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var allMatches: List<Match> = emptyList()
    private var currentMatchIndex = 0
    private val uniqueTeams = mutableSetOf<String>()
    private var currentTeamName: String? = null

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
            binding.showBack.visibility = View.GONE
            binding.searchLayout.visibility = View.VISIBLE
            showHistory()
        }

        binding.showBack.setOnClickListener {
            binding.StatisticsLayout.visibility = View.GONE
            binding.showBack.visibility = View.GONE
            binding.period.visibility = View.VISIBLE
        }

        binding.statsButton.setOnClickListener {
            binding.searchLayout.visibility = View.GONE
            showStats()
        }

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                currentTeamName = query
                filterMatches(query)
            } else {
                Toast.makeText(requireContext(), "Введите название команды", Toast.LENGTH_SHORT).show()
            }
        }

        binding.showAllButton.setOnClickListener {
            currentTeamName = null
            showAllMatches()
        }
    }

    private fun filterMatches(query: String) {
        val filteredMatches = allMatches.filter {
            it.match_hometeam_name.equals(query, ignoreCase = true) ||
                    it.match_awayteam_name.equals(query, ignoreCase = true)
        }

        if (filteredMatches.isEmpty()) {
            Toast.makeText(requireContext(), "Матчи не найдены", Toast.LENGTH_SHORT).show()
        } else {
            showFilteredMatches(filteredMatches, query)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showFilteredMatches(matches: List<Match>, teamName: String) {
        binding.statsResultLayout.visibility = View.VISIBLE
        binding.historyLayout.removeAllViews()

        val teamMatches = allMatches.filter {
            it.match_hometeam_name.equals(teamName, ignoreCase = true) ||
                    it.match_awayteam_name.equals(teamName, ignoreCase = true)
        }

        if (teamMatches.isNotEmpty()) {
            val totalMatches = teamMatches.size
            val wins = teamMatches.count { match ->
                (match.match_hometeam_name.equals(teamName, ignoreCase = true) && match.match_hometeam_score < match.match_awayteam_score) ||
                        (match.match_awayteam_name.equals(teamName, ignoreCase = true) && match.match_awayteam_score < match.match_hometeam_score)
            }
            val losses = teamMatches.count { match ->
                (match.match_hometeam_name.equals(teamName, ignoreCase = true) && match.match_hometeam_score > match.match_awayteam_score) ||
                        (match.match_awayteam_name.equals(teamName, ignoreCase = true) && match.match_awayteam_score > match.match_hometeam_score)
            }
            val draws = teamMatches.count { it.match_hometeam_score == it.match_awayteam_score }

            val goalsFor = teamMatches.sumOf { match ->
                if (match.match_hometeam_name.equals(teamName, ignoreCase = true)) {
                    match.match_hometeam_score.toIntOrNull() ?: 0
                } else {
                    match.match_awayteam_score.toIntOrNull() ?: 0
                }
            }

            val goalsAgainst = teamMatches.sumOf { match ->
                if (match.match_hometeam_name.equals(teamName, ignoreCase = true)) {
                    match.match_awayteam_score.toIntOrNull() ?: 0
                } else {
                    match.match_hometeam_score.toIntOrNull() ?: 0
                }
            }

            binding.matchesPlayed.text = "Матчей сыграно: $totalMatches"
            binding.team1Wins.text = "Победы: $wins"
            binding.team2Wins.text = "Поражения: $losses"
            binding.barcelonaGoals.text = "Ничьи: $draws"
            binding.realGoals.text = "Голы: $goalsAgainst - $goalsFor"
            binding.statsHeader.text = "Статистика против $teamName"
        }

        matches.forEach { match ->
            addMatchToHistory(match)
        }
    }

    private fun showAllMatches() {
        binding.statsResultLayout.visibility = View.GONE
        binding.historyLayout.removeAllViews()
        allMatches.forEach { match ->
            listTeams(match)
        }
    }

    private fun loadMatches() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMatches(
                    teamId = 2986,
                    from = "2024-07-20",
                    to = "2025-05-25",
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
            "${match.match_hometeam_score} - ${match.match_awayteam_score}"
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
    private fun listTeams(match: Match) {

        if (match.match_awayteam_name == "Billericay Town") return
        if (match.match_awayteam_name == "Billericay") return
        val itemBinding = ItemMatchHistoryBinding.inflate(layoutInflater)

        Glide.with(requireContext()).load(match.team_away_badge).into(itemBinding.historyAwayTeamLogo)

        itemBinding.historyScoreTextView.text = match.match_awayteam_name
        match.match_awayteam_name?.let { uniqueTeams.add(it) }

        // Настройка выделения текста
        itemBinding.historyScoreTextView.setTextIsSelectable(true)
        itemBinding.historyScoreTextView.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menu?.removeItem(android.R.id.selectAll)
                menu?.removeItem(android.R.id.cut)
                menu?.removeItem(android.R.id.shareText)
                return true
            }
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        binding.historyLayout.addView(itemBinding.root)
    }

    @SuppressLint("SetTextI18n")
    private fun addMatchToHistory(match: Match) {
        val itemBinding = ItemMatchHistoryBinding.inflate(layoutInflater)

        Glide.with(requireContext()).load(match.team_home_badge).into(itemBinding.historyHomeTeamLogo)
        Glide.with(requireContext()).load(match.team_away_badge).into(itemBinding.historyAwayTeamLogo)

        itemBinding.historyScoreTextView.text =
            "${match.match_awayteam_name} ${match.match_awayteam_score} - ${match.match_hometeam_score} ${match.match_hometeam_name}"

        binding.historyLayout.addView(itemBinding.root)
    }

    private fun showHistory() {
        binding.period.visibility = View.GONE
        binding.StatisticsLayout.visibility = View.GONE
        binding.statsResultLayout.visibility = View.GONE
        binding.historyLayout.visibility = View.VISIBLE
        binding.historyLayout.removeAllViews()

        allMatches.forEach { match ->
            listTeams(match)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showStats() {
        binding.showBack.visibility = View.GONE
        binding.statsResultLayout.visibility = View.GONE
        binding.historyLayout.visibility = View.GONE
        binding.StatisticsLayout.visibility = View.GONE
        binding.period.visibility = View.VISIBLE

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        binding.startDateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                binding.startDateEditText.setText(dateFormat.format(selectedDate.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.endDateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                binding.endDateEditText.setText(dateFormat.format(selectedDate.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.applyDateRangeButton.setOnClickListener {
            val startDate = binding.startDateEditText.text.toString()
            val endDate = binding.endDateEditText.text.toString()
            val minDate = dateFormat.parse("2024-07-20")!!
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите обе даты", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Фильтрация матчей по выбранному диапазону дат
            val filteredMatches = allMatches.filter { match ->
                val matchDate = dateFormat.parse(match.match_date) ?: return@filter false
                matchDate in start..end
            }

            if (start != null) {
                if (start.before(minDate)) {
                    Toast.makeText(requireContext(), "Начальная дата должна быть не раньше 2024-07-20", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (end != null) {
                if (end.before(minDate)) {
                    Toast.makeText(requireContext(), "Конечная дата должна быть не раньше 2024-07-20", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (start != null) {
                if (start.after(end)) {
                    Toast.makeText(requireContext(), "Начальная дата не может быть позже конечной", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (filteredMatches.isEmpty()) {
                Toast.makeText(requireContext(), "Нет матчей в выбранном диапазоне", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Скрываем форму выбора дат и показываем статистику
            binding.period.visibility = View.GONE
            binding.StatisticsLayout.visibility = View.VISIBLE
            binding.showBack.visibility = View.VISIBLE

            // Создаем и заполняем статистику
            val statsBinding = StatisticsMatchBinding.inflate(layoutInflater)
            binding.StatisticsLayout.removeAllViews()
            binding.StatisticsLayout.addView(statsBinding.root)

            // Подсчет статистики
            val totalMatches = filteredMatches.size
            val wins = filteredMatches.count { it.match_hometeam_score > it.match_awayteam_score }
            val losses = filteredMatches.count { it.match_hometeam_score < it.match_awayteam_score }
            val draws = filteredMatches.count { it.match_hometeam_score == it.match_awayteam_score }

            val goalsScored = filteredMatches.sumOf { it.match_hometeam_score.toIntOrNull() ?: 0 }
            val goalsConceded = filteredMatches.sumOf { it.match_awayteam_score.toIntOrNull() ?: 0 }

            val bestPlayer = filteredMatches.flatMap { match ->
                match.goalscorer.filter { !it.home_scorer.isNullOrEmpty() }.map { it.home_scorer to it.time }
            }.groupBy { it.first }
                .maxByOrNull { it.value.size }
                ?.key ?: "Не определен"

            statsBinding.statsPeriod.text = "$startDate - $endDate"
            statsBinding.statsGoals.text = "$goalsScored - $goalsConceded (забито - пропущено)"
            statsBinding.statsMatches.text = totalMatches.toString()
            statsBinding.statsWins.text = "$wins победы, $losses поражения, $draws ничьи"
            statsBinding.statsBestPlayer.text = bestPlayer
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
