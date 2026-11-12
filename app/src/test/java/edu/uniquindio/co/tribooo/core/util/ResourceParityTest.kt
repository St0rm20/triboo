package edu.uniquindio.co.tribooo.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/** El inglés debe traducir exactamente lo que el español define: mismas claves y mismos marcadores. */
class ResourceParityTest {

    /** Textos de una carpeta de recursos por clave (`string:x`, `plurals:x`, `array:x`), cada uno con sus partes. */
    private fun load(folder: String): Map<String, List<String>> {
        val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resourceFile(folder)).documentElement
        val result = linkedMapOf<String, List<String>>()
        for (i in 0 until root.childNodes.length) {
            val node = root.childNodes.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            val parts = (0 until node.childNodes.length).mapNotNull { node.childNodes.item(it) as? Element }
            when (node.tagName) {
                "string" -> result["string:$name"] = listOf(node.textContent)
                "plurals" -> result["plurals:$name"] = parts.map { "${it.getAttribute("quantity")}=${it.textContent}" }
                "string-array" -> result["array:$name"] = parts.map { it.textContent }
            }
        }
        return result
    }

    private val placeholder = Regex("%(\\d+)\\$[sd]")

    private fun placeholders(text: String): Set<String> = placeholder.findAll(text).map { it.value }.toSet()

    private val spanish = load("values")
    private val english = load("values-en")

    @Test
    fun bothLanguagesDefineTheSameKeys() {
        assertEquals(spanish.keys, english.keys)
    }

    @Test
    fun pluralsAndArraysHaveTheSameShape() {
        spanish.forEach { (key, es) ->
            val en = english.getValue(key)
            if (key.startsWith("array:")) assertEquals("Elementos de $key", es.size, en.size)
            if (key.startsWith("plurals:")) {
                assertEquals("Cantidades de $key", es.map { it.substringBefore('=') }, en.map { it.substringBefore('=') })
            }
        }
    }

    @Test
    fun placeholdersMatchInBothLanguages() {
        spanish.forEach { (key, es) ->
            val en = english.getValue(key)
            es.indices.forEach { index ->
                assertEquals("Marcadores de $key", placeholders(es[index]), placeholders(en[index]))
            }
        }
    }

    @Test
    fun noTextIsEmpty() {
        listOf(spanish, english).forEach { texts ->
            texts.forEach { (key, parts) ->
                parts.forEach { assertTrue("$key está vacío", it.substringAfter('=').isNotBlank()) }
            }
        }
    }
}
