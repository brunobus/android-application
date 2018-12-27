package hr.bpervan.novaeva.model

class Prayer(val id: Int, val title: String, val filePath: String)

class PrayerCategory(val id: Int,
                     val title: String,
                     val prayerList: List<Prayer>)