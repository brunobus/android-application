package hr.bpervan.novaeva.model

import hr.bpervan.novaeva.utilities.TransitionAnimation

/**
 *
 */

data class OpenContentEvent(val contentMetadata: EvaContentMetadata, val themeId: Int = -1,
                            val animation: TransitionAnimation = TransitionAnimation.NONE)

data class OpenDirectoryEvent(val directoryMetadata: EvaDirectoryMetadata, val themeId: Int = -1,
                              val animation: TransitionAnimation = TransitionAnimation.NONE)

data class OpenPrayerCategoryEvent(val prayerCategory: PrayerCategory,
                                   val animation: TransitionAnimation = TransitionAnimation.NONE)

data class OpenQuotesEvent(val quoteId: Long = -1,
                           val animation: TransitionAnimation = TransitionAnimation.NONE)

data class OpenBreviaryContentEvent(val breviaryId: Int,
                                    val animation: TransitionAnimation = TransitionAnimation.NONE)