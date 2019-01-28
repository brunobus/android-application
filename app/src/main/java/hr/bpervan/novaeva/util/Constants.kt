package hr.bpervan.novaeva.util

/**
 *
 */
const val syncIntervalMillis: Long = 60 * 1000
const val evictionIntervalMillis: Long = 2 * 24 * 60 * 60 * 1000

const val defaultTextSize = 16
const val minTextSize = 12
const val maxTextSize = 26

const val NOVA_EVA_PREFS = "hr.novaeva"

const val TEXT_SIZE_KEY = "$NOVA_EVA_PREFS.textsize"
const val SCROLL_PERCENT_KEY = "$NOVA_EVA_PREFS.scrollPercent"
const val FLOATING_MODE_KEY = "$NOVA_EVA_PREFS.floatingMode"
const val EVA_THEME_KEY = "$NOVA_EVA_PREFS.evaTheme"

const val HAS_NEW_CONTENT_KEY_PREFIX = "$NOVA_EVA_PREFS.hasNewContent."
const val LATEST_CONTENT_ID_KEY_PREFIX = "$NOVA_EVA_PREFS.latestContentId."
const val LAST_EVICTION_TIME_MILLIS_KEY_PREFIX = "$NOVA_EVA_PREFS.lastEvictionTime."

const val BREVIARY_IMAGE_KEY = "$NOVA_EVA_PREFS.breviaryheaderimage"

const val LAST_SYNC_TIME_MILLIS_KEY = "$NOVA_EVA_PREFS.lastSyncTime"

const val ASSETS_DIR_PATH = "file:///android_asset/"