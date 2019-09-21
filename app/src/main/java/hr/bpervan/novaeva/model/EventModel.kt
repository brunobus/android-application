package hr.bpervan.novaeva.model

import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.util.TransitionAnimation

/**
 * todo implement Parcelable
 */

data class OpenContentEvent(val content: EvaContent,
                            val themeId: Int = -1,
                            val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenDirectoryEvent(val directory: EvaDirectory,
                              val themeId: Int = -1,
                              val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenPrayerCategoryEvent(val prayerCategory: PrayerCategory,
                                   val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenQuotesEvent(val quoteId: Long = -1,
                           val animation: TransitionAnimation = TransitionAnimation.FADE)

data class OpenBreviaryContentEvent(val breviaryId: Int,
                                    val animation: TransitionAnimation = TransitionAnimation.FADE)

