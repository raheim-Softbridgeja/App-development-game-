package com.islandmatch.yardvibes.game

import android.content.Context

class YardTaskManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val baseTasks = listOf(
        YardTask("clear_leaves", "Clear leaves from the front path", 1),
        YardTask("plant_hibiscus", "Plant hibiscus by the gate", 1),
        YardTask("repair_bench", "Repair the old yard bench", 2),
        YardTask("string_lanterns", "Hang lanterns over the cookout spot", 2),
        YardTask("set_domino_table", "Set up the domino table nook", 3),
    )

    fun tasks(): List<YardTask> {
        val completed = completedIds()
        return baseTasks.map { task ->
            task.copy(isCompleted = task.id in completed)
        }
    }

    fun complete(taskId: String): YardTask? {
        val task = baseTasks.firstOrNull { it.id == taskId } ?: return null
        val completed = completedIds().toMutableSet()
        if (taskId in completed) {
            return task.copy(isCompleted = true)
        }

        completed += taskId
        prefs.edit().putStringSet(KEY_COMPLETED_TASKS, completed).apply()
        return task.copy(isCompleted = true)
    }

    fun completedCount(): Int = completedIds().size

    fun totalCount(): Int = baseTasks.size

    private fun completedIds(): Set<String> {
        return prefs.getStringSet(KEY_COMPLETED_TASKS, emptySet()).orEmpty()
    }

    private companion object {
        const val PREFS_NAME = "yard_vibes_progress"
        const val KEY_COMPLETED_TASKS = "completed_yard_tasks"
    }
}
