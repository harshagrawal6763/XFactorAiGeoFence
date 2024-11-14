package com.harsh.geofence.utils

import org.junit.Assert.*

import org.junit.Test

class AppConstsKtTest {

    @Test
    fun testGetUniqueKeyInt() {
        assertNotSame(getUniqueKeyInt(),getUniqueKeyInt())
    }

    @Test
    fun testConvertLongToTime() {
        assert(convertLongToTime(System.currentTimeMillis()).isNotEmpty())
    }
}