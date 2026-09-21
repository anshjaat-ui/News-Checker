package com.example

import com.example.data.model.FactCheckVerdict
import com.example.data.remote.KnownFactChecks
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verdictParsing_isAccurate() {
        assertEquals(FactCheckVerdict.FAKE, FactCheckVerdict.fromString("FAKE"))
        assertEquals(FactCheckVerdict.MISLEADING, FactCheckVerdict.fromString("MISLEADING"))
        assertEquals(FactCheckVerdict.TRUE, FactCheckVerdict.fromString("TRUE"))
        assertEquals(FactCheckVerdict.UNVERIFIED, FactCheckVerdict.fromString("UNKNOWN"))
    }

    @Test
    fun knownFactChecks_matchesViralClaims() {
        val anthemMatch = KnownFactChecks.findMatch("UNESCO declares Jana Gana Mana best anthem")
        assertNotNull(anthemMatch)
        assertEquals(FactCheckVerdict.FAKE, anthemMatch?.verdict)

        val rechargeMatch = KnownFactChecks.findMatch("Free recharge offer from Tata anniversary link")
        assertNotNull(rechargeMatch)
        assertEquals(FactCheckVerdict.FAKE, rechargeMatch?.verdict)
    }
}

