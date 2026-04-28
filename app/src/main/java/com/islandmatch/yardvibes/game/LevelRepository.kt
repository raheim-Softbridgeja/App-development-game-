package com.islandmatch.yardvibes.game

import android.content.res.AssetManager
import org.json.JSONArray
import org.json.JSONObject

class LevelRepository(
    private val assets: AssetManager,
) {
    fun listLevelIds(): List<String> {
        return assets
            .list("Levels")
            ?.filter { it.endsWith(".json") }
            ?.sorted()
            ?.map { it.removeSuffix(".json") }
            .orEmpty()
    }

    fun loadLevel(levelId: String): LevelData {
        val rawJson = assets.open("Levels/$levelId.json").bufferedReader().use { it.readText() }
        val json = JSONObject(rawJson)

        return LevelData(
            id = json.getString("id"),
            version = json.optInt("version", 1),
            width = json.getInt("width"),
            height = json.getInt("height"),
            moves = json.getInt("moves"),
            targetScore = json.getInt("targetScore"),
            layout = json.getJSONArray("layout").toIntArray(),
            objectives = json.optJSONArray("objectives").toStringList(),
        )
    }
}

private fun JSONArray.toIntArray(): IntArray {
    val values = IntArray(length())
    for (index in 0 until length()) {
        values[index] = getInt(index)
    }
    return values
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) {
        return emptyList()
    }

    val values = mutableListOf<String>()
    for (index in 0 until length()) {
        values += optString(index)
    }
    return values
}
