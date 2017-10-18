package hr.bpervan.novaeva.utilities

import hr.bpervan.novaeva.main.R

/**
 * Class which handles requiremens for various colour types
 * @author Branimir
 */
object ResourceHandler {

    fun getContentTitleBarResourceId(colourset: Int): Int = when (colourset) {
        EvaCategory.AKTUALNO.id -> R.drawable.vijest_naslovnaaktualno
        EvaCategory.DUHOVNOST.id -> R.drawable.vijest_naslovnaduhovnost
        EvaCategory.EVANDJELJE.id -> R.drawable.vijest_naslovnaevandjelje
        EvaCategory.IZREKE.id -> R.drawable.vijest_naslovnaizreke
        EvaCategory.MULTIMEDIJA.id -> R.drawable.vijest_naslovnamultimedija
        EvaCategory.ODGOVORI.id -> R.drawable.vijest_naslovnaodgovori
        EvaCategory.PJESMARICA.id -> R.drawable.vijest_naslovnamp3
        EvaCategory.POZIV.id -> R.drawable.vijest_naslovnapoziv
        EvaCategory.PROPOVIJEDI.id -> R.drawable.vijest_naslovnapropovjedi
        else -> R.drawable.vijest_naslovnapropovjedi
    }

    fun getListViewHeader(colourset: Int): Int = when (colourset) {
        EvaCategory.AKTUALNO.id -> R.drawable.izbornik_top_aktualno
        EvaCategory.DUHOVNOST.id -> R.drawable.izbornik_top_duhovnost
        EvaCategory.EVANDJELJE.id -> R.drawable.izbornik_top_evandjelje
        EvaCategory.MULTIMEDIJA.id -> R.drawable.izbornik_top_multimedija
        EvaCategory.ODGOVORI.id -> R.drawable.izbornik_top_odgovori
        EvaCategory.PJESMARICA.id -> R.drawable.izbornik_top_mp3
        EvaCategory.POZIV.id -> R.drawable.izbornik_top_poziv
        EvaCategory.PROPOVIJEDI.id -> R.drawable.izbornik_top_propovjedi
        else -> R.drawable.izbornik_top_odgovori
    }

    /**
     * Used by ListaVijestiActivity object.
     * Returns Id from R class defining background colour for FakeActionBar
     * @author Branimir
     * @return Resource id from R.java directly usable in View.setBackgroundDrawable
     */
    fun getFakeActionBarResourceId(colourset: Int): Int = when (colourset) {
        EvaCategory.AKTUALNO.id -> R.drawable.toolbar_aktualno
        EvaCategory.DUHOVNOST.id -> R.drawable.toolbar_duhovnost
        EvaCategory.EVANDJELJE.id -> R.drawable.toolbar_evandjelje
        EvaCategory.IZREKE.id -> R.drawable.toolbar_izreke
        EvaCategory.MULTIMEDIJA.id -> R.drawable.toolbar_multimedija
        EvaCategory.ODGOVORI.id -> R.drawable.toolbar_odgovori
        EvaCategory.PJESMARICA.id -> R.drawable.toolbar_piesmarica
        EvaCategory.POZIV.id -> R.drawable.toolbar_poziv
        EvaCategory.PROPOVIJEDI.id -> R.drawable.toolbar_propoviedi
        else -> R.drawable.toolbar_odgovori
    }

    fun getDirectoryListItemResourceId(colourset: Int): Int = when (colourset) {
        EvaCategory.AKTUALNO.id -> R.drawable.izbornik_btn_normal_aktualno_folder
        EvaCategory.DUHOVNOST.id -> R.drawable.izbornik_btn_normal_duhovnosti_folder
        EvaCategory.EVANDJELJE.id -> R.drawable.izbornik_btn_normal_evandjelje_folder
        EvaCategory.MULTIMEDIJA.id -> R.drawable.izbornik_btn_normal_multimedija_folder
        EvaCategory.ODGOVORI.id -> R.drawable.izbornik_btn_normal_odgovori_folder
        EvaCategory.PJESMARICA.id -> R.drawable.izbornik_btn_normal_mp3_folder
        EvaCategory.POZIV.id -> R.drawable.izbornik_btn_normal_poziv_folder
        EvaCategory.PROPOVIJEDI.id -> R.drawable.izbornik_btn_normal_propovijedi_folder
        else -> R.drawable.izbornik_btn_normal_odgovori_folder
    }

    fun getContentListItemResourceId(colourset: Int): Int = when (colourset) {
        EvaCategory.AKTUALNO.id -> R.drawable.izbornik_btn_normal_aktualno
        EvaCategory.DUHOVNOST.id -> R.drawable.izbornik_btn_normal_duhovnosti
        EvaCategory.EVANDJELJE.id -> R.drawable.izbornik_btn_normal_evandjelje
        EvaCategory.MULTIMEDIJA.id -> R.drawable.izbornik_btn_normal_multimedija
        EvaCategory.ODGOVORI.id -> R.drawable.izbornik_btn_normal_odgovori
        EvaCategory.PJESMARICA.id -> R.drawable.izbornik_btn_normal_mp3
        EvaCategory.POZIV.id -> R.drawable.izbornik_btn_normal_poziv
        EvaCategory.PROPOVIJEDI.id -> R.drawable.izbornik_btn_normal_propovijedi
        else -> R.drawable.izbornik_btn_normal_odgovori
    }
}
