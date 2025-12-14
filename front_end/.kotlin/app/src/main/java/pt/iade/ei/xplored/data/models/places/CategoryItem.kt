package pt.iade.ei.xplored.data.models.places

import pt.iade.ei.xplored.data.models.misc.IdString

/**
 * Mirrors table: categories
 */
data class Category(
    val categoryId: IdString,
    val name: String,
    val colorHex: String,  // like "#AABBCC"
    val iconName: String? = null
)
