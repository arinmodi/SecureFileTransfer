package com.genz.secure_share.algorithms

import com.genz.secure_share.utils.NetworkCalls
import org.junit.Assert.*
import org.junit.Test

class NetworkCallsTest {

    @Test
    fun testReadRemoteFile() {
        val url = "https://firebasestorage.googleapis.com/v0/b/file-transfer-18e8e.appspot.com/o/files%2Fhell.txt?alt=media&token=d4443825-d76e-4827-b025-20b9ccec8354"
        val bytes = NetworkCalls().readRemoteFile(url)

        assertNotNull(bytes)
        assertEquals(true, bytes?.isNotEmpty())
        assertEquals("helllo world", bytes?.toString(Charsets.UTF_8))

    }
}