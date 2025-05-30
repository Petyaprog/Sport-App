    package com.example.realmadrid.ui.dashboard

    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.bumptech.glide.Glide
    import com.example.realmadrid.R
    import com.example.realmadrid.databinding.FragmentCalendarBinding
    import com.example.realmadrid.ui.home.Match
    import com.example.realmadrid.ui.home.RetrofitClient
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import java.text.SimpleDateFormat
    import java.util.*

    class CalendarFragment : Fragment() {
        private var _binding: FragmentCalendarBinding? = null
        private val binding get() = _binding!!
        private lateinit var matchesAdapter: MatchesAdapter
        private val matchesList = mutableListOf<Match>()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentCalendarBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setupRecyclerView()
            fetchMatches()
        }

        private fun setupRecyclerView() {
            matchesAdapter = MatchesAdapter(matchesList)
            binding.matchesRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = matchesAdapter
            }
        }

        private fun fetchMatches() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val from = "2024-07-20"
                    val to = "2025-05-25"

                    Log.d("CalendarFragment", "Запрос матчей: teamId=2986, from=$from, to=$to")

                    val matches = RetrofitClient.apiService.getMatches(
                        timezone = "Europe/Moscow",
                        teamId = 2986,
                        from = from,
                        to = to,
                        apiKey = "fbeb2c690d9967cfb237f410b95c9658dd64bdbf8143a629f0ffb56c216ebe64"
                    )

                    Log.d("CalendarFragment", "Получено матчей: ${matches.size}")

                    withContext(Dispatchers.Main) {
                        if (matches.isNotEmpty()) {
                            matchesList.clear()
                            matchesList.addAll(matches)
                            matchesAdapter.notifyDataSetChanged()

                            // Вывод информации о первом матче для проверки
                            matches.firstOrNull()?.let {
                                Log.d("CalendarFragment",
                                    "Матч: ${it.match_date} ${it.match_time}\n" +
                                            "${it.match_hometeam_name} vs ${it.match_awayteam_name}\n" +
                                            "Стадион: ${it.match_stadium}")
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Матчей с $from по $to не найдено",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalendarFragment", "Ошибка загрузки", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: ${e.message?.take(50)}...", // Обрезаем длинные сообщения
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }