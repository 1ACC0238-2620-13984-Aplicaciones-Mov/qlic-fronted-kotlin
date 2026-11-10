package com.qlic.mobile.app

import com.qlic.mobile.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectBaselineTest {
    @Test
    fun `application identity matches the configured package`() {
        assertEquals("com.qlic.mobile", AppIdentity.applicationId)
        assert(BuildConfig.APPLICATION_ID.startsWith(AppIdentity.applicationId))
    }
}
