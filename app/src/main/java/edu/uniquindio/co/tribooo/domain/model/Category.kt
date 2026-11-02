package edu.uniquindio.co.tribooo.domain.model

data class Category(
    val id: String,
    val key: String,
    val icon: String,
    val color: String
) {
    companion object {
        // Íconos (Material Symbols) y colores tomados del prototipo
        val DEPORTES = Category(
            id = "deportes",
            key = "deportes",
            icon = "directions_bike",
            color = "#074A24"
        )
        val CULTURA = Category(
            id = "cultura",
            key = "cultura",
            icon = "theater_comedy",
            color = "#562A55"
        )
        val ACADEMICO = Category(
            id = "academico",
            key = "academico",
            icon = "school",
            color = "#29396C"
        )
        val VOLUNTARIADO = Category(
            id = "voluntariado",
            key = "voluntariado",
            icon = "volunteer_activism",
            color = "#5D3000"
        )
        val SOCIAL = Category(
            id = "social",
            key = "social",
            icon = "storefront",
            color = "#642721"
        )

        val defaultCategories = listOf(DEPORTES, CULTURA, ACADEMICO, VOLUNTARIADO, SOCIAL)
    }
}
