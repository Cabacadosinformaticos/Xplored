package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: categories
 */
data class Category(
    val categoryId: IdString,
    val name: String,
    val colorHex: String,  // like "#AABBCC"
    val iconName: String? = null
)
