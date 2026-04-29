package com.islandmatch.yardvibes.game

class CurrencyManager {
    var starBalance: Int = 0
        private set

    fun addStars(amount: Int) {
        if (amount > 0) {
            starBalance += amount
        }
    }

    fun canAfford(cost: Int): Boolean = starBalance >= cost

    fun spendStars(cost: Int): Boolean {
        if (canAfford(cost)) {
            starBalance -= cost
            return true
        }
        return false
    }
}
