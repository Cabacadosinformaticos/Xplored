package pt.iade.ei.xplored.models

object CategoryMapper {
    private val nameToId = mapOf(
        "Atividades" to 1L,
        "Lojas" to 2L,
        "Restauração" to 3L,
        "Históricos" to 4L,
        "Paisagens" to 5L
    )

    private val idToName = nameToId.entries.associate { (name, id) -> id to name }

    fun getIdByName(name: String): Long? = nameToId[name]
    fun getNameById(id: Long?): String = idToName[id] ?: "Desconhecido"
}
