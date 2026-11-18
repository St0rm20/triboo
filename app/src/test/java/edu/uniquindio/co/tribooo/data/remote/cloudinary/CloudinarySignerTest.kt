package edu.uniquindio.co.tribooo.data.remote.cloudinary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CloudinarySignerTest {

    private val signer = CloudinarySigner(apiKey = "key", apiSecret = "abcd", clock = { 1315060510 })

    @Test
    fun matchesTheExampleFromCloudinaryDocs() {
        // https://cloudinary.com/documentation/authentication_signatures
        assertEquals("a21ad0f63beb4de2e5575204b79ab90bffb02c10", signer.signatureOf(mapOf("timestamp" to "1315060510")))
    }

    @Test
    fun sortsParamsAndSkipsTheOnesCloudinaryDoesNotSign() {
        val unordered = mapOf("timestamp" to "1315060510", "public_id" to "x", "file" to "f", "api_key" to "key")
        assertEquals(signer.signatureOf(mapOf("public_id" to "x", "timestamp" to "1315060510")), signer.signatureOf(unordered))
    }

    @Test
    fun signAddsTimestampApiKeyAndSignature() {
        val signed = signer.sign(mapOf("public_id" to "tribooo/events/a"))
        assertEquals("1315060510", signed["timestamp"])
        assertEquals("key", signed["api_key"])
        assertEquals(signer.signatureOf(mapOf("public_id" to "tribooo/events/a", "timestamp" to "1315060510")), signed["signature"])
        assertFalse(signed.containsValue("abcd"))
    }
}
