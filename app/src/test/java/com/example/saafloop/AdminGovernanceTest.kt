package com.example.saafloop

import android.content.Context
import android.content.ContextWrapper
import com.example.saafloop.core.data.AdminGovernanceRepositoryImpl
import com.example.saafloop.core.model.GovernanceCaseStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdminGovernanceTest {

    private lateinit var fakeContext: Context
    private lateinit var repository: AdminGovernanceRepositoryImpl

    @Before
    fun setUp() {
        fakeContext = GovernanceTestContext()
        repository = AdminGovernanceRepositoryImpl(fakeContext)
    }

    @Test
    fun observeGovernanceCases_returnsCasesAndAppeals() = runBlocking {
        val cases = repository.observeGovernanceCases().first()
        val appeals = repository.observeGovernanceAppeals().first()
        val orgs = repository.observeOrgVerificationQueue().first()

        assertTrue(cases.isNotEmpty())
        assertTrue(appeals.isNotEmpty())
        assertTrue(orgs.isNotEmpty())
    }

    @Test
    fun resolveGovernanceCase_executesSuccessfully() = runBlocking {
        val result = repository.resolveGovernanceCase(
            caseId = "case_gov_001",
            newStatus = GovernanceCaseStatus.RESOLVED,
            decisionReason = "Photo verified; no duplicate violation found.",
            reviewerUid = "coord_admin_10"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun resolveAppeal_executesSuccessfully() = runBlocking {
        val result = repository.resolveAppeal(
            appealId = "appeal_101",
            outcomeCode = "OVERTURNED",
            decisionNotes = "Re-evaluation shows separate dumping site in Sector 70.",
            reviewerUid = "admin_super_01"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun updateOrgVerification_executesSuccessfully() = runBlocking {
        val result = repository.updateOrgVerificationStatus(
            reviewId = "org_rev_01",
            newStatus = "VERIFIED",
            decisionNotes = "Registration certificate verified.",
            reviewerUid = "admin_super_01"
        )

        assertTrue(result.isSuccess)
    }
}

private class GovernanceTestContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
}
