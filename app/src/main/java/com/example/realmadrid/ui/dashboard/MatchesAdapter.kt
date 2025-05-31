package com.example.realmadrid.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realmadrid.R
import com.example.realmadrid.API.Match

class MatchesAdapter(private val matches: List<Match>) : RecyclerView.Adapter<MatchesAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matches[position]
        holder.bind(match)
    }

    override fun getItemCount() = matches.size

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val homeTeamTextView: TextView = itemView.findViewById(R.id.homeTeamTextView)
        private val awayTeamTextView: TextView = itemView.findViewById(R.id.awayTeamTextView)
        private val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        private val homeTeamLogo: ImageView = itemView.findViewById(R.id.homeTeamLogo)
        private val awayTeamLogo: ImageView = itemView.findViewById(R.id.awayTeamLogo)

        fun bind(match: Match) {
            dateTextView.text = "${match.match_date} • ${match.match_time}"
            homeTeamTextView.text = match.match_hometeam_name
            awayTeamTextView.text = match.match_awayteam_name
            scoreTextView.text = "${match.match_hometeam_score} - ${match.match_awayteam_score}"

            // Загрузка логотипов через Glide
            Glide.with(itemView)
                .load(match.team_home_badge)
                .placeholder(R.drawable.user_img) // Заглушка, если нет лого
                .into(homeTeamLogo)

            Glide.with(itemView)
                .load(match.team_away_badge)
                .placeholder(R.drawable.user_img)
                .into(awayTeamLogo)
        }
    }
}