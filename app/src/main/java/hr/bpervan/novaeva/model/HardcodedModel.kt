package hr.bpervan.novaeva.model

enum class EvaCategory(val id: Long) {
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

//todo move to server
val PRAYER_CATEGORIES: List<PrayerCategory> = listOf(
        PrayerCategory(24, "Često tražene molitve", "24_Najcesce_Koristene_Molitve", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(0, "0. Uvod", "0_Uvod", listOf(
                Prayer("1. Molitva", "1_Molitva.html"),
                Prayer("2. Moliti", "2_Moliti.html"),
                Prayer("3. Molitva može biti", "3_Molitva_moze_biti.html"),
                Prayer("4. Molitvom izražavamo", "4_Molitvom_izrazavamo.html"),
                Prayer("5. Molitva je ljudska potreba", "5_Molitva_je_ljudska_potreba.html"),
                Prayer("6. Prava molitva", "6_Prava_molitva.html"),
                Prayer("7. Molitva treba biti", "7_Molitva_treba_biti.html"),
                Prayer("8. O molitveniku", "8_O_molitveniku.html"))),
        PrayerCategory(1, "1. Obrasci vjere", "1_Obrasci_vjere", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(2, "2. Osnovne molitve", "2_Osnovne_molitve", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(3, "3. Svagdanje jutarnje molitve", "3_Svagdanje_jutarnje_molitve", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(4, "4. Svagdanje večernje molitve", "4_Svagdanje_vecernje_molitve", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(5, "5. Prigodne molitve", "5_Prigodne_molitve", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(6, "6. Molitve mladih", "6_Molitve_mladih", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(7, "7. Molitve u kušnji i napasti", "7_Molitve_u_kusnji_i_napasti", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(8, "8. Molitve za obitelj i roditelje", "8_Molitve_za_obitelj_i_roditelje", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(9, "9. Molitve za bolesne i umiruće", "9_Molitve_za_bolesne_i_umiruce", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(10, "10. Molitve po posebnim nakanama", "10_Molitve_po_posebnim_nakanama", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(11, "11. Molitve svetih i velikih ljudi", "11_Molitve_svetih_i_velikih_ljudi", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(12, "12. Kratke molitve i zazivi", "12_Kratke_molitve_i_zazivi", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(13, "13. Molitve Duhu Svetome", "13_Molitve_Duhu_Svetome", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(14, "14. Euharistijska pobožnost", "14_Euharistijska_poboznost", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(15, "15. Pomirenje", "15_Pomirenje", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(16, "16. Pobožnost križnog puta", "16_Poboznost_kriznog_puta", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(17, "17. Deventica i krunica Božanskom milosrđu", "17_Deventica_i_krunica_bozanskom_milosrdu", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(18, "18. Molitve Blaženoj Djevici Mariji", "18_Molitve_Blazenoj_Djevici_Mariji", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(19, "19. Salezijanske molitve", "19_Salezijanske_molitve", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(20, "20. Molitve mladih", "20_Molitve_mladih", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(21, "21. Molitve svetima", "21_Molitve_svetima", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(22, "22. Lectio Divina", "22_Lectio_Divina", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""))),
        PrayerCategory(23, "23. Moliti igrajući pred Gospodinom", "23_Moliti_igrajuci_pred_Gospodinom", listOf(
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", ""),
                Prayer("", "")))
)

/**
 *
 */

class Prayer(val title: String, val fileName: String)

class PrayerCategory(val id: Int,
                     val title: String,
                     val directoryName: String,
                     val prayerList: List<Prayer>)