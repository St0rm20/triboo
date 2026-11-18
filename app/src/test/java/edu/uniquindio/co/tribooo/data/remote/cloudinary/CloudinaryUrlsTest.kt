package edu.uniquindio.co.tribooo.data.remote.cloudinary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CloudinaryUrlsTest {

    private val cloud = "demo"

    @Test
    fun extractsThePublicIdWithFoldersFromASecureUrl() {
        val url = "https://res.cloudinary.com/demo/image/upload/v1712345678/tribooo/events/3f2a-9b.jpg"
        assertEquals("tribooo/events/3f2a-9b", CloudinaryUrls.publicIdOf(url, cloud))
    }

    @Test
    fun acceptsUrlsWithoutVersionOrExtension() {
        assertEquals("tribooo/profiles/a", CloudinaryUrls.publicIdOf("https://res.cloudinary.com/demo/image/upload/tribooo/profiles/a", cloud))
    }

    @Test
    fun ignoresImagesOfOtherAccountsOrHosts() {
        assertNull(CloudinaryUrls.publicIdOf("https://res.cloudinary.com/other/image/upload/v1/tribooo/events/a.jpg", cloud))
        assertNull(CloudinaryUrls.publicIdOf("https://lh3.googleusercontent.com/a/photo.jpg", cloud))
        assertNull(CloudinaryUrls.publicIdOf("content://media/picker/1", cloud))
        assertFalse(CloudinaryUrls.isHosted("https://res.cloudinary.com/demo/image/upload/v1/a.jpg", ""))
    }
}
