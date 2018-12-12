package hr.bpervan.novaeva.model

import hr.bpervan.novaeva.rest.EvaCategory
import hr.bpervan.novaeva.util.TransitionAnimation

/**
 *
 */

data class OpenContentEvent(val category: EvaCategory,
                            val content: EvaContent,
                            val themeId: Int = -1,
                            val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenDirectoryEvent(val category: EvaCategory,
                              val directory: EvaDirectory,
                              val themeId: Int = -1,
                              val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenPrayerCategoryEvent(val prayerCategory: PrayerCategory,
                                   val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenQuotesEvent(val quoteId: Long = -1,
                           val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenBreviaryContentEvent(val breviaryId: Int,
                                    val animation: TransitionAnimation = TransitionAnimation.FADE)

