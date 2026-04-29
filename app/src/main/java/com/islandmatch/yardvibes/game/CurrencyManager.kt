package com.islandmatch.yardvibes.game

import android.content.Context

class CurrencyManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var starBalance: Int = prefs.getInt(KEY_STARS, 0)
        private set

    fun addStars(amount: Int) {
        if (amount > 0) {
            starBalance += amount
            persist()
        }
    }

    fun canAfford(cost: Int): Boolean = starBalance >= cost

    fun spendStars(cost: Int): Boolean {
        if (canAfford(cost)) {
            starBalance -= cost
            persist()
            return true
        }
        return false
    }

    private fun persist() {
        prefs.edit().putInt(KEY_STARS, starBalance).apply()
    }

    private companion object {
        const val PREFS_NAME = "yard_vibes_progress"
        const val KEY_STARS = "stars"
    }
}
