package com.example.saafloop.core.data

/**
 * Represents the authentication and access state across the SaafLoop application.
 *
 * NOTE FOR SECTION 2:
 * Only [Guest] and [SignedOut] transitions are active in this section.
 * Other states ([AuthenticatedResident], [PendingOrganisationVerification], [VerifiedOrganisation])
 * are defined as domain models for Section 6 and MUST NOT be assigned without real backend verification.
 */
sealed interface UserAccessState {
    val label: String
    val isGuest: Boolean
        get() = this is Guest

    /** Active guest session allowing resident browsing without an account. */
    data object Guest : UserAccessState {
        override val label: String = "Guest Session"
    }

    /** No active session. Requires access choice screen. */
    data object SignedOut : UserAccessState {
        override val label: String = "Signed Out"
    }

    /** Verified resident account (Reserved for Section 6 backend verification). */
    data class AuthenticatedResident(
        val userId: String,
        val displayName: String
    ) : UserAccessState {
        override val label: String = "Authenticated Resident"
    }

    /** Organisation account pending manual/document verification (Reserved for Section 6). */
    data class PendingOrganisationVerification(
        val orgId: String,
        val orgName: String
    ) : UserAccessState {
        override val label: String = "Pending Verification"
    }

    /** Verified team or organisation with official credentials (Reserved for Section 6). */
    data class VerifiedOrganisation(
        val orgId: String,
        val orgName: String,
        val verificationBadgeToken: String
    ) : UserAccessState {
        override val label: String = "Verified Organisation"
    }
}
