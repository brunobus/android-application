package hr.bpervan.novaeva.model

import hr.bpervan.novaeva.util.TransitionAnimation

/**
 *
 */

data class OpenContentEvent(val contentMetadata: EvaContentMetadata,
                            val themeId: Int = -1,
                            val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenDirectoryEvent(val directoryMetadata: EvaDirectoryMetadata,
                              val themeId: Int = -1,
                              val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenPrayerCategoryEvent(val prayerCategory: PrayerCategory,
                                   val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenQuotesEvent(val quoteId: Long = -1,
                           val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenBreviaryContentEvent(val breviaryId: Int,
                                    val animation: TransitionAnimation = TransitionAnimation.FADE)

