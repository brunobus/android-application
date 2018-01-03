package hr.bpervan.novaeva.model

import android.os.Parcel
import android.os.Parcelable

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

class Prayer(val title: String, val contentUrl: String)

class PrayerCategory(val id: Int,
                     val title: String,
                     val url: String) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeString(url)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<PrayerCategory> {
        override fun createFromParcel(parcel: Parcel): PrayerCategory = PrayerCategory(parcel)
        override fun newArray(size: Int): Array<PrayerCategory?> = arrayOfNulls(size)
    }
}

val HARDCODED_PRAYER_CATEGORY_LIST: List<PrayerCategory> = mapOf(
        "Često tražene molitve" to "24_Najcesce_Koristene_Molitve.htm",
        "0. Uvod" to "0_Uvod.htm",
        "1. Obrasci vjere" to "1_Obrasci_vjere.htm",
        "2. Osnovne molitve" to "2_Osnovne_molitve.htm",
        "3. Svagdanje jutarnje molitve" to "3_Svagdanje_jutarnje_molitve.htm",
        "4. Svagdanje večernje molitve" to "4_Svagdanje_vecernje_molitve.htm",
        "5. Prigodne molitve" to "5_Prigodne_molitve.htm",
        "6. Molitve mladih" to "6_Molitve_mladih.htm",
        "7. Molitve u kušnji i napasti" to "7_Molitve_u_kusnji_i_napasti.htm",
        "8. Molitve za obitelj i roditelje" to "8_Molitve_za_obitelj_i_roditelje.htm",
        "9. Molitve za bolesne i umiruće" to "9_Molitve_za_bolesne_i_umiruce.htm",
        "10. Molitve po posebnim nakanama" to "10_Molitve_po_posebnim_nakanama.htm",
        "11. Molitve svetih i velikih ljudi" to "11_Molitve_svetih_i_velikih_ljudi.htm",
        "12. Kratke molitve i zazivi" to "12_Kratke_molitve_i_zazivi.htm",
        "13. Molitve Duhu Svetome" to "13_Molitve_Duhu_Svetome.htm",
        "14. Euharistijska pobožnost" to "14_Euharistijska_poboznost.htm",
        "15. Pomirenje" to "15_Pomirenje.htm",
        "16. Pobožnost križnog puta" to "16_Poboznost_kriznog_puta.htm",
        "17. Deventica i krunica Božanskom milosrđu" to "17_Deventica_i_krunica_bozanskom_milosrdu.htm",
        "18. Molitve Blaženoj Djevici Mariji" to "18_Molitve_Blazenoj_Djevici_Mariji.htm",
        "19. Salezijanske molitve" to "19_Salezijanske_molitve.htm",
        "20. Molitve mladih" to "20_Molitve_mladih.htm",
        "21. Molitve svetima" to "21_Molitve_svetima.htm",
        "22. Lectio Divina" to "22_Lectio_Divina.htm",
        "23. Moliti igrajući pred Gospodinom" to "23_Moliti_igrajuci_pred_Gospodinom.htm")
        .entries
        .mapIndexed { index, entry ->
            PrayerCategory(index, entry.key, "file:///android_asset/${entry.value}")
        }

private fun String.vertical() = this.asSequence().joinToString(separator = "\n")
