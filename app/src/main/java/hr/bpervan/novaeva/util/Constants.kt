package hr.bpervan.novaeva.util

/**
 *
 */
const val syncIntervalMillis: Long = 60 * 1000
const val evictionIntervalMillis: Long = 2 * 24 * 60 * 60 * 1000

const val defaultTextSize = 16
const val minTextSize = 12
const val maxTextSize = 26

const val NOVA_EVA_PREFS_NAME = "hr.novaeva"
const val TEXT_SIZE_KEY = "hr.novaeva.textsize"
const val SCROLL_PERCENT_KEY = "hr.novaeva.scrollPercent"
const val FLOATING_MODE_KEY = "hr.novaeva.floatingMode"
const val EVA_THEME_KEY = "hr.novaeva.evaTheme"
const val NEW_CONTENT_KEY_PREFIX = "hr.novaeva.newContent"
const val LATEST_CONTENT_ID_KEY_PREFIX = "hr.novaeva.latestContentId"
const val LAST_EVICTION_TIME_MILLIS_KEY = "hr.novaeva.lastEvictionTime"
const val LAST_SYNC_TIME_MILLIS_KEY = "hr.novaeva.lastSyncTime"

const val ASSETS_DIR_PATH = "file:///android_asset/"