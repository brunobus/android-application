package hr.bpervan.novaeva.model

enum class EvaCategoryLegacy(val id: Long) {
    SPIRITUALITY(354),
    TRENDING(9),
    QUOTES(1),
    MULTIMEDIA(10),
    GOSPEL(4),
    SERMONS(7),
    VOCATION(8),
    ANSWERS(11),
    SONGBOOK(355),
    RADIO(473)
}

class Prayer(val id: Int, val title: String, val filePath: String)

class PrayerCategory(val id: Int,
                     val title: String,
                     val prayerList: List<Prayer>)