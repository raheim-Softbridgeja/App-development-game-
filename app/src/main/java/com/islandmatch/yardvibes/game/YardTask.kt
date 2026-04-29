package com.islandmatch.yardvibes.game

data class YardTask(
    val id: String,
    val description: String,
    val cost: Int,
    var isCompleted: Boolean = false
)
