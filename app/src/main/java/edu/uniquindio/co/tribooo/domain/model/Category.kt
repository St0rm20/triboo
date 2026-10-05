package edu.uniquindio.co.tribooo.domain.model

data class Category(
    val id: String,
    val key: String,
    val name: String,
    val icon: String,
    val color: String
) {
    companion object {
        val DEPORTES = Category(
            id = "deportes",
            key = "deportes",
            name = "Deportes",
            icon = "sports",
            color = "#2563EB"
        )
        val CULTURA = Category(
            id = "cultura",
            key = "cultura",
            name = "Cultura",
            icon = "palette",
            color = "#7C3AED"
        )
        val ACADEMICO = Category(
            id = "academico",
            key = "academico",
            name = "Académico",
            icon = "school",
            color = "#F59E0B"
        )
        val VOLUNTARIADO = Category(
            id = "voluntariado",
            key = "voluntariado",
            name = "Voluntariado",
            icon = "volunteer_activism",
            color = "#10B981"
        )
        val SOCIAL = Category(
            id = "social",
            key = "social",
            name = "Social",
            icon = "groups",
            color = "#EC4899"
        )

        val defaultCategories = listOf(DEPORTES, CULTURA, ACADEMICO, VOLUNTARIADO, SOCIAL)
    }
}
