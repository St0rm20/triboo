package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Ruta de los recursos de la app, tanto si las pruebas corren desde el módulo como desde la raíz del proyecto. */
internal fun resourceFile(folder: String): File =
    listOf("src/main/res/$folder/strings.xml", "app/src/main/res/$folder/strings.xml")
        .map(::File)
        .first { it.exists() }

/**
 * [ResourceProvider] de pruebas sin Android: lee `res/<folder>/strings.xml` (por defecto, el español real)
 * y resuelve los ids de `R` por su nombre.
 */
class FakeResourceProvider(folder: String = "values") : ResourceProvider {

    private val strings = mutableMapOf<String, String>()
    private val plurals = mutableMapOf<String, Map<String, String>>()
    private val arrays = mutableMapOf<String, List<String>>()
    private val namesById = buildMap {
        listOf(R.string::class.java, R.plurals::class.java, R.array::class.java).forEach { holder ->
            holder.fields.forEach { put(it.getInt(null), it.name) }
        }
    }

    init {
        val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resourceFile(folder)).documentElement
        val children = root.childNodes
        for (i in 0 until children.length) {
            val node = children.item(i) as? Element ?: continue
            val name = node.getAttribute("name")
            when (node.tagName) {
                "string" -> strings[name] = unescape(node.textContent)
                "plurals" -> plurals[name] = items(node).associate { it.getAttribute("quantity") to unescape(it.textContent) }
                "string-array" -> arrays[name] = items(node).map { unescape(it.textContent) }
            }
        }
    }

    override fun getString(id: Int, vararg args: Any): String = format(strings.getValue(nameOf(id)), args)

    override fun getQuantityString(id: Int, quantity: Int, vararg args: Any): String {
        val forms = plurals.getValue(nameOf(id))
        return format(forms[if (quantity == 1) "one" else "other"] ?: forms.getValue("other"), args)
    }

    override fun getStringArray(id: Int): List<String> = arrays.getValue(nameOf(id))

    private fun nameOf(id: Int): String = namesById.getValue(id)

    private fun format(pattern: String, args: Array<out Any>): String = String.format(pattern, *args)

    private fun items(node: Element): List<Element> =
        (0 until node.childNodes.length).mapNotNull { node.childNodes.item(it) as? Element }

    private fun unescape(raw: String): String = raw.trim()
        .replace("\\'", "'").replace("\\\"", "\"").replace("\\@", "@").replace("\\?", "?").replace("\\n", "\n")
}

/** Instancia compartida con los textos en español para las pruebas. */
val testResources: ResourceProvider = FakeResourceProvider()
