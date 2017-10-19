package hr.bpervan.novaeva.model

enum class EvaCategory(val id: Int, val rawName: String) {
    DUHOVNOST(354, "Duhovnost"),
    AKTUALNO(9, "Aktualno"),
    IZREKE(1, "Izreke"),
    MULTIMEDIJA(10, "Multimedija"),
    EVANDJELJE(4, "Evanđelje"),
    PROPOVIJEDI(7, "Propovijedi"),
    POZIV(8, "Poziv"),
    ODGOVORI(11, "Odgovori"),
    PJESMARICA(355, "Pjesmarica");

    val rawNameVertical = rawName.vertical()
}

enum class LocalCategory(val rawName: String) {
    BREVIJAR("Brevijar"),
    MOLITVENIK("Molitvenik"),
    INFO("Informacije"),
    BOOKMARKS("Zabilješke");

    val rawNameVertical = rawName.vertical()
}

private fun String.vertical() = this.asSequence().joinToString(separator = "\n")
