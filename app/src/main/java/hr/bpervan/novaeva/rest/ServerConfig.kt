package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.rest.Region.*

enum class Region(val id: Long) {
    CROATIA(1L),
    POLAND(2L)
}

val region = CROATIA

val serverV2 = novaEvaUrl

val serverV3 = when (region) {
    CROATIA -> mockServiceUrl
    POLAND -> mockServiceUrl
}

val novaEvaServiceV2 by lazy {
    RestInterfaceBuilder.build<NovaEvaServiceV2>(serverV2)
}

val novaEvaServiceV3 by lazy {
    RestInterfaceBuilder.build<NovaEvaServiceV3>("$serverV3/api/v3/")
}

enum class EvaCategory(val endpointRoot: String) {
    SPIRITUALITY("spirituality"),
    TRENDING("trending"),
    QUOTES("quotes"),
    MULTIMEDIA("multimedia"),
    GOSPEL("gospel"),
    SERMONS("sermons"),
    VOCATION("vocations"),
    ANSWERS("answers"),
    SONGBOOK("songbook"),
    RADIO("radio"),
    PRAYERS("prayers")
}