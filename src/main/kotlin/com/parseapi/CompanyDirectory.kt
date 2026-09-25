package com.parseapi

import kotlinx.serialization.Serializable

/** An address with its recorded role; role does not imply mailing validity or headquarters. */
@Serializable
class CompanyProfileAddress private constructor(
	val type: String,
	val street: String? = null,
	val city: String? = null,
	val state: String? = null,
	val postal: String? = null,
	val country: String? = null,
)

/** A reported exchange/symbol pair. No listings does not establish private ownership. */
@Serializable
class CompanyProfileListing private constructor(
	val exchange: String,
	val symbol: String,
)

/** Recorded registration jurisdiction, separate from address or operating location. */
@Serializable
class CompanyProfileJurisdiction private constructor(
	val country: String,
	val state: String? = null,
)

/** Another associated hostname and its recorded URL, when known. */
@Serializable
class CompanyProfileWebsite private constructor(
	val domain: String,
	val url: String? = null,
)

/** An authority-scoped identifier; values preserve leading zeros. */
@Serializable
class CompanyProfileIdentifier private constructor(
	val type: String,
	val authority: String,
	val value: String,
)

/** A reported classification; type is an open scheme string. */
@Serializable
class CompanyProfileIndustry private constructor(
	val type: String,
	val code: String,
	val name: String? = null,
)

/** Reported founding value and precision (year, month or day), distinct from incorporation. */
@Serializable
class CompanyProfileFounding private constructor(
	val value: String,
	/** Open string; currently year, month or day. Preserve the source value without padding. */
	val precision: String,
)

/** Reported total headcount at its explicit measurement date. */
@Serializable
class CompanyProfileEmployees private constructor(
	val count: Long,
	val asOf: String,
	/** Open string, currently legal_entity or consolidated_group. */
	val scope: String,
	/** Open string, currently reported. */
	val method: String,
	val approximate: Boolean,
)

/** Legal form recorded by a register; codes remain open strings. */
@Serializable
class CompanyProfileRegistrationLegalForm private constructor(
	val code: String,
	val name: String,
)

/** Recorded principal-address components, not inferred ISO codes or headquarters. */
@Serializable
class CompanyProfileRegistrationAddress private constructor(
	val kind: String,
	val line1: String? = null,
	val line2: String? = null,
	val city: String? = null,
	val state: String? = null,
	val postal: String? = null,
	val countryRaw: String? = null,
)

/** Registry-scoped legal facts, not an operation or tax-exemption verdict. */
@Serializable
class CompanyProfileRegistration private constructor(
	val authority: String,
	val number: String,
	val jurisdiction: CompanyProfileJurisdiction,
	val role: String,
	val legalForm: CompanyProfileRegistrationLegalForm,
	val status: String,
	/** This register's reported entity-form date, not universal incorporation or founding. */
	val formationDate: String? = null,
	val address: CompanyProfileRegistrationAddress? = null,
)

/** Attribution only for the named selected enrichment fields; observation is not a source update. */
@Serializable
class CompanyProfileSource private constructor(
	val type: String,
	val url: String,
	val fields: List<String> = emptyList(),
	/** Artifact observation timestamp. */
	val observedAt: String,
	/** Explicit source update timestamp, or null. Measurement dates belong to employees.as_of. */
	val updatedAt: String? = null,
)

/** Optional directory detail. Every member may be missing or null; existing releases may omit enrichment fields. */
@Serializable
class CompanyProfileDeep private constructor(
	val legalName: String? = null,
	val aliases: List<String>? = null,
	val jurisdiction: CompanyProfileJurisdiction? = null,
	/** Recorded legal status; not an operating or compliance verdict. */
	val status: String? = null,
	val websites: List<CompanyProfileWebsite>? = null,
	val identifiers: List<CompanyProfileIdentifier>? = null,
	val incorporated: String? = null,
	val addresses: List<CompanyProfileAddress>? = null,
	val industries: List<CompanyProfileIndustry>? = null,
	val parent: String? = null,
	val description: String? = null,
	/** Reported asset URL; the client does not fetch or license the asset. */
	val logo: String? = null,
	/** Selected company account URLs; an empty array does not prove no accounts exist. */
	val socials: List<String>? = null,
	val founded: CompanyProfileFounding? = null,
	val employees: CompanyProfileEmployees? = null,
	val registrations: List<CompanyProfileRegistration>? = null,
	/** Attribution for projected enrichment fields only, not the entire legal profile. */
	val sources: List<CompanyProfileSource>? = null,
)

/** Search match evidence. Open strings permit future fields and identifier/listing namespaces. */
@Serializable
class CompanyMatch private constructor(
	val field: String? = null,
	val value: String? = null,
	val type: String? = null,
	val authority: String? = null,
	val exchange: String? = null,
)

/** A directory profile, distinct from national company-number validation. */
@Serializable
class CompanyProfile private constructor(
	val id: String,
	val name: String,
	val country: String? = null,
	val website: String? = null,
	val listings: List<CompanyProfileListing> = emptyList(),
	val address: CompanyProfileAddress? = null,
	val deep: CompanyProfileDeep? = null,
)

/** A directory search profile with match evidence; a match is not proof of legal identity. */
@Serializable
class CompanyCandidate private constructor(
	val id: String,
	val name: String,
	val country: String? = null,
	val website: String? = null,
	val listings: List<CompanyProfileListing> = emptyList(),
	val address: CompanyProfileAddress? = null,
	val deep: CompanyProfileDeep? = null,
	val match: CompanyMatch,
)

/** One page of company candidates. Reuse next with the same selector, filters and limit. */
@Serializable
class CompanySearch private constructor(
	val companies: List<CompanyCandidate> = emptyList(),
	val next: String? = null,
)

/** Counts for this directory edition, not complete country or worldwide coverage. */
@Serializable
class CompanyCoverage private constructor(
	val scope: String,
	val label: String,
	val description: String,
	val snapshotAt: String,
	val companies: Long,
	val countries: List<String> = emptyList(),
	val withWebsite: Long,
	val withListings: Long,
	val withAddress: Long,
)
