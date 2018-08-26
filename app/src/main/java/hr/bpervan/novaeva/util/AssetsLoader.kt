package hr.bpervan.novaeva.util

import android.content.Context
import hr.bpervan.novaeva.model.Prayer
import hr.bpervan.novaeva.model.PrayerCategory

/**
 *
 */
object AssetsLoader {

    fun loadPrayerBook(context: Context): List<PrayerCategory> {
        val basePath = "molitve"
        return context.assets.list(basePath)
                .map { Pair(it, context.assets.list("$basePath/$it")) }
                .filter { it.second.isNotEmpty() }
                .map { pair ->
                    val prayerCategoryDir = pair.first
                    val prayerFiles = pair.second

                    val prayerCategoryId = prayerCategoryDir.substringBefore(' ').toInt()
                    val prayerCategoryName = prayerCategoryDir.substringAfter(' ')

                    val prayers = prayerFiles.map { prayerFile ->
                        val prayerId = prayerFile.substringBefore(' ').toInt()
                        val prayerName = prayerFile
                                .substringBeforeLast('.')
                                .substringAfter(' ')

                        Prayer(prayerId, prayerName, "$basePath/$prayerCategoryDir/$prayerFile")
                    }.sortedBy { it.id }

                    PrayerCategory(prayerCategoryId, prayerCategoryName, prayers)
                }.sortedBy { it.id }
    }
}