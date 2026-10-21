package edu.uniquindio.co.tribooo.core.util

/** "Daniela Restrepo" → "DR" */
fun String.initials(): String =
    split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

/** "Camilo Arango" → "Camilo A." */
fun String.shortName(): String {
    val parts = split(" ").filter { it.isNotBlank() }
    return if (parts.size < 2) this else "${parts[0]} ${parts[1].first().uppercase()}."
}

/** "Camilo Arango" → "Camilo" */
fun String.firstName(): String = trim().substringBefore(" ")
