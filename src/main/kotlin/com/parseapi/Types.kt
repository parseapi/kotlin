package com.parseapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Response types for the parseAPI public API. Shapes are append-only
// upstream, so these only ever grow. Nullable fields default to null.
// Deep objects follow the triad: null when not requested, empty when
// requested but locked, populated when unlocked, so every field inside
// a deep type is nullable. Unknown fields are ignored.
// JSON keys are snake_case and map via the SnakeCase naming strategy.

@Serializable
class IpDeep private constructor(
	val state: String? = null,
	val city: String? = null,
	val registry: String? = null,
	val datacenter: Boolean? = null,
	val relay: Boolean? = null,
	val tor: Boolean? = null,
	val vpn: Boolean? = null,
	val provider: String? = null,
)

@Serializable
class Ip private constructor(
	val ip: String,
	val country: String? = null,
	val countryName: String? = null,
	val continent: String? = null,
	val asn: String? = null,
	val asnName: String? = null,
	val deep: IpDeep? = null,
)

@Serializable
class Continent private constructor(
	val continent: String,
	val name: String,
	val region: String,
	val subregion: String,
	val population: Long? = null,
	val area: Double? = null,
	val emoji: String,
)

@Serializable
class ContinentCountryItem private constructor(
	val country: String,
	val name: String,
	val emoji: String? = null,
	val callingCode: String? = null,
)

@Serializable
class ContinentCountries private constructor(
	val continent: String,
	val countries: List<ContinentCountryItem> = emptyList(),
)

@Serializable
class Country private constructor(
	val country: String,
	val iso3: String,
	val numeric: Int,
	val name: String,
	val fullName: String? = null,
	val localName: String? = null,
	val demonym: String? = null,
	val capital: String? = null,
	val capitalLat: Double? = null,
	val capitalLon: Double? = null,
	val continent: String,
	val region: String? = null,
	val subregion: String? = null,
	val population: Long? = null,
	val area: Double? = null,
	val currency: String? = null,
	val currencyName: String? = null,
	val currencySymbol: String? = null,
	val tld: String? = null,
	val callingCode: String? = null,
	val emoji: String? = null,
	val languages: List<String> = emptyList(),
	val borders: List<String> = emptyList(),
	val blocs: List<String> = emptyList(),
)

@Serializable
class CountryStateItem private constructor(
	val state: String,
	val name: String,
	val type: String? = null,
)

@Serializable
class Bloc private constructor(
	val bloc: String,
	val name: String,
	val members: Int,
)

@Serializable
class BlocCountryItem private constructor(
	val country: String,
	val name: String,
	val emoji: String? = null,
	val callingCode: String? = null,
)

@Serializable
class BlocCountries private constructor(
	val bloc: String,
	val countries: List<BlocCountryItem> = emptyList(),
)

@Serializable
class CountryStates private constructor(
	val country: String,
	val states: List<CountryStateItem> = emptyList(),
)

@Serializable
class State private constructor(
	val state: String,
	val name: String,
	val localName: String? = null,
	val type: String? = null,
	val country: String,
	val countryName: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val population: Long? = null,
	val area: Double? = null,
	val timezone: String? = null,
	val timezones: List<String> = emptyList(),
	@SerialName("iso_3166_2") val iso31662: String? = null,
	val fips: String? = null,
	val capital: String? = null,
	val areaCodes: List<String> = emptyList(),
	val tax: String? = null,
	val taxRate: Double? = null,
)

@Serializable
class StateDistrictItem private constructor(
	val district: String,
	val name: String,
	val type: String? = null,
)

@Serializable
class StateDistricts private constructor(
	val state: String,
	val stateName: String? = null,
	val country: String,
	val countryName: String? = null,
	val districts: List<StateDistrictItem> = emptyList(),
)

@Serializable
class District private constructor(
	val district: String,
	val name: String,
	val type: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val country: String,
	val countryName: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val population: Long? = null,
	/** Total area in km2 (land + water, or the official total). */
	val area: Double? = null,
	/** Land area in km2. Null when the source publishes total only. */
	val landArea: Double? = null,
	/** Water area in km2. Null when the source publishes total only. */
	val waterArea: Double? = null,
	val seat: String? = null,
	val timezone: String? = null,
	val timezones: List<String> = emptyList(),
)

@Serializable
class City private constructor(
	val name: String,
	val localName: String? = null,
	val type: String? = null,
	/** What this city is the capital of: country, state, or null. */
	val capitalOf: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val district: String? = null,
	val districtName: String? = null,
	val country: String,
	val countryName: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val elevation: Double? = null,
	val elevationFt: Double? = null,
	val population: Long? = null,
	/** Total area in km2 (land + water, or the official total). */
	val area: Double? = null,
	/** Land area in km2. Null when the source publishes total only. */
	val landArea: Double? = null,
	/** Water area in km2. Null when the source publishes total only. */
	val waterArea: Double? = null,
	val timezone: String? = null,
	/** Minted parse id (city_ + 12 chars). Stable pin via cityId(). */
	val id: String,
)

/** Nearest-city lookups add the distance from the query point. */
@Serializable
class CityNearest private constructor(
	val name: String,
	val localName: String? = null,
	val type: String? = null,
	val capitalOf: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val district: String? = null,
	val districtName: String? = null,
	val country: String,
	val countryName: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val elevation: Double? = null,
	val elevationFt: Double? = null,
	val population: Long? = null,
	val area: Double? = null,
	val landArea: Double? = null,
	val waterArea: Double? = null,
	val timezone: String? = null,
	val id: String,
	val distance: Double,
	val distanceMi: Double,
)

@Serializable
class CitySearch private constructor(
	val q: String,
	val country: String? = null,
	val state: String? = null,
	val cities: List<City> = emptyList(),
)

@Serializable
class CityNearby private constructor(
	val city: String,
	val state: String? = null,
	val country: String,
	val radius: Double,
	val unit: String,
	val nearby: List<CityNearest> = emptyList(),
)

@Serializable
class Postal private constructor(
	val postal: String,
	val city: String? = null,
	val cityLocal: String? = null,
	val district: String? = null,
	val districtName: String? = null,
	val districtNameLocal: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val stateNameLocal: String? = null,
	val country: String,
	val countryName: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val elevation: Double? = null,
	val elevationFt: Double? = null,
	val population: Long? = null,
	/** Total area in km2. Null when the source has no water split. */
	val area: Double? = null,
	/** Land area in km2, where the source has it. */
	val landArea: Double? = null,
	/** Water area in km2, where the source has it. */
	val waterArea: Double? = null,
	val timezone: String? = null,
	val currency: String? = null,
	val neighbors: List<String> = emptyList(),
)

@Serializable
class PostalNearbyItem private constructor(
	val postal: String,
	val city: String? = null,
	val state: String? = null,
	val country: String,
	val distance: Double,
	val distanceMi: Double,
)

@Serializable
class PostalNearby private constructor(
	val postal: String,
	val country: String,
	val radius: Double,
	val unit: String,
	val nearby: List<PostalNearbyItem> = emptyList(),
)

@Serializable
class PostalDistanceEnd private constructor(
	val postal: String,
	val city: String? = null,
)

@Serializable
class PostalDistance private constructor(
	val country: String,
	val from: PostalDistanceEnd,
	val to: PostalDistanceEnd,
	val distance: Double,
	val distanceMi: Double,
)

@Serializable
class EmailDeep private constructor(
	val deliverable: Boolean? = null,
	val catchall: Boolean? = null,
)

@Serializable
class Email private constructor(
	val email: String,
	/** Suggested full address when the host is a known misspelling. Never a guess. */
	val didyoumean: String? = null,
	val valid: Boolean,
	val domain: String? = null,
	val domainValid: Boolean? = null,
	val role: Boolean,
	val disposable: Boolean,
	val deep: EmailDeep? = null,
)

@Serializable
class VatAddress private constructor(
	val street: String? = null,
	val city: String? = null,
	val postal: String? = null,
	val country: String? = null,
)

@Serializable
class VatDeep private constructor(
	val registered: Boolean? = null,
	val name: String? = null,
	val address: VatAddress? = null,
	val consultation: String? = null,
	/** Registry timestamp of this check, ISO. */
	val consultedAt: String? = null,
)

@Serializable
class Vat private constructor(
	val vat: String? = null,
	val valid: Boolean,
	val country: String? = null,
	val from: String? = null,
	val deep: VatDeep? = null,
)

@Serializable
class Iban private constructor(
	val iban: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/** Print form in groups of four, for display. Null when invalid. */
	val formatted: String? = null,
	val checksum: String? = null,
	/** Bank identifier parsed from the number, not a name. */
	val bank: String? = null,
	/** Institution name from the national bank-code directory. Null when unsourced. */
	val bankName: String? = null,
	/** BIC from that same directory. Null when unsourced or missing. */
	val bic: String? = null,
	/** Branch identifier when that country has one. */
	val branch: String? = null,
	val account: String? = null,
)

@Serializable
class Npi private constructor(
	/** Normalized 10-digit NPI. Invalid input still echoes the fold. */
	val npi: String? = null,
	val valid: Boolean,
	/** Exists in the CMS NPPES registry. */
	val registered: Boolean? = null,
	val active: Boolean? = null,
	/** Date CMS deactivated the NPI, YYYY-MM-DD. Null when still active. */
	val deactivatedAt: String? = null,
	/** On the OIG exclusion list. */
	val excluded: Boolean? = null,
	/** individual or organization. */
	val type: String? = null,
	val name: String? = null,
	val first: String? = null,
	val last: String? = null,
	val credential: String? = null,
	val specialty: String? = null,
	/** NUCC taxonomy code. */
	val taxonomy: String? = null,
	val address: String? = null,
	val city: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val postal: String? = null,
	val country: String? = null,
	val phone: String? = null,
	val deep: NpiDeep? = null,
)

@Serializable
class NpiEnrollment private constructor(
	/** part_a, part_b, practitioner, dme, order_refer, mdpp. Null when unknown. */
	val type: String? = null,
	val specialty: String? = null,
	val state: String? = null,
)

@Serializable
class NpiDeep private constructor(
	/** In the published Medicare FFS enrollment extract. */
	val medicare: Boolean? = null,
	/** On the CMS opt-out affidavit list. Matched by NPI only. */
	val optOut: Boolean? = null,
	/** Enrollment rows. Empty when medicare is false. */
	val enrollments: List<NpiEnrollment>? = null,
)

@Serializable
class TariffMeasure private constructor(
	/** Chapter 99 heading, dotted (9903.01.24). */
	val heading: String,
	/** The measure text verbatim. */
	val description: String,
	/** The rate string verbatim. */
	val rate: String? = null,
	/** Effective from, ISO YYYY-MM-DD. Null when the schedule states none. */
	val from: String? = null,
	/** Expires, ISO YYYY-MM-DD. Null when open-ended. */
	val until: String? = null,
)

@Serializable
class TariffDeep private constructor(
	/** The origin country the measures were resolved for. */
	val origin: String? = null,
	/** Composed ad valorem percent. Null when the components do not compose cleanly. */
	@SerialName("effective_rate") val effectiveRate: Double? = null,
	/** Every Chapter 99 tariff measure that applies to this code from this origin. */
	val measures: List<TariffMeasure>? = null,
)

@Serializable
class Tariff private constructor(
	/** Normalized code with dots (8471.30.01.00). */
	val hts: String,
	/** The schedule line verbatim. */
	val description: String,
	/** Parent descriptions from the schedule outline, outermost first. */
	val lineage: List<String> = emptyList(),
	/** Units of quantity (No., kg). */
	val units: List<String> = emptyList(),
	/** Column 1 general rate, verbatim. */
	val general: String? = null,
	/** Column 1 special rate, verbatim. */
	val special: String? = null,
	/** Column 2 rate, verbatim. */
	val other: String? = null,
	/** The official release that answered (2026HTSRev17). */
	val revision: String,
	val deep: TariffDeep? = null,
)

@Serializable
class TariffSearchHit private constructor(
	val hts: String,
	val description: String,
	val general: String? = null,
)

@Serializable
class TariffSearch private constructor(
	val q: String,
	val revision: String,
	/** Up to 20 lines, best match first. */
	val lines: List<TariffSearchHit> = emptyList(),
)

@Serializable
class VinRecall private constructor(
	/** Government campaign number. */
	val campaign: String,
	/** Report date, ISO YYYY-MM-DD. */
	val date: String? = null,
	val component: String? = null,
	/** The filed summary verbatim. */
	val summary: String? = null,
)

@Serializable
class VinDeep private constructor(
	/**
	 * Open recall campaigns for the decoded vehicle. Empty when none,
	 * null when the recall registry did not answer.
	 */
	val recalls: List<VinRecall>? = null,
)

@Serializable
class Vin private constructor(
	/** Normalized VIN, uppercase, no spaces. Invalid input still echoes the fold. */
	val vin: String? = null,
	val valid: Boolean,
	val year: Int? = null,
	val make: String? = null,
	val model: String? = null,
	val trim: String? = null,
	val series: String? = null,
	/** Body style (sedan, coupe, suv, pickup). */
	val body: String? = null,
	/** Vehicle type (passenger car, truck, motorcycle, bus, trailer). */
	val type: String? = null,
	val doors: Int? = null,
	val cylinders: Int? = null,
	/** Engine displacement in liters. */
	val displacement: Double? = null,
	val fuel: String? = null,
	val horsepower: Double? = null,
	/** fwd, rwd, awd, 4wd. */
	val drive: String? = null,
	/** automatic, manual, cvt. */
	val transmission: String? = null,
	val manufacturer: String? = null,
	@SerialName("plant_city") val plantCity: String? = null,
	@SerialName("plant_state") val plantState: String? = null,
	@SerialName("plant_country") val plantCountry: String? = null,
	/** Gross vehicle weight rating class as filed. */
	val gvwr: String? = null,
	val deep: VinDeep? = null,
)

/** Always empty. The metered proves are their own endpoints: carrier, caller, hlr. */
@Serializable
class PhoneDeep private constructor()

@Serializable
class Phone private constructor(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/**
	 * What the numbering plan can see: mobile, landline, toll_free, unknown.
	 * Never voip (that is the carrier field's word). Null when invalid.
	 */
	val type: String? = null,
	/** NPA-derived state code (US/CA). */
	val state: String? = null,
	val stateName: String? = null,
	/** Numbering-plan IANA zone. Null when the prefix covers more than one zone. */
	val timezone: String? = null,
	val national: String? = null,
	val international: String? = null,
	val deep: PhoneDeep? = null,
)

@Serializable
class Carrier private constructor(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/** The network's word, including voip. Null when invalid. */
	val type: String? = null,
	/** Current carrier display name. Null when the probe had no answer. */
	val carrier: String? = null,
	/** Carrier is a known burner number app. Null when carrier is unknown. */
	val burner: Boolean? = null,
	/** Issuing rate-center city. */
	val city: String? = null,
	val state: String? = null,
	val stateName: String? = null,
)

@Serializable
class Caller private constructor(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/**
	 * CNAM record verbatim (all-caps telco artifact). Null when no record,
	 * outside NANP, or invalid.
	 */
	val caller: String? = null,
)

@Serializable
class Hlr private constructor(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/** Assigned to a subscriber. Null when invalid. */
	val live: Boolean? = null,
	/** Handset reachable right now. Null means unconfirmed, never no. */
	val connected: Boolean? = null,
	/** The six network extras fill on live HLR dips only. Null elsewhere (NANP, failover). */
	val roaming: Boolean? = null,
	val roamingNetwork: String? = null,
	/** ISO2, uppercase. */
	val roamingCountry: String? = null,
	/** Current serving network name. */
	val network: String? = null,
	val originalNetwork: String? = null,
	val mcc: String? = null,
	val mnc: String? = null,
)

@Serializable
class MxRecord private constructor(
	val priority: Int,
	val host: String,
)

@Serializable
class DomainRegistration private constructor(
	val registered: Boolean? = null,
	val created: String? = null,
	val updated: String? = null,
	val expires: String? = null,
	val registrar: String? = null,
	val status: List<String>? = null,
	val dnssec: Boolean? = null,
)

@Serializable
class DomainDeep private constructor(
	val a: List<String>? = null,
	val aaaa: List<String>? = null,
	val ns: List<String>? = null,
	val mx: List<MxRecord>? = null,
	val txt: List<String>? = null,
	/** The brand behind the MX (Google, Microsoft). */
	val mailhost: String? = null,
	val registration: DomainRegistration? = null,
)

@Serializable
class Domain private constructor(
	val domain: String,
	val available: Boolean,
	val deep: DomainDeep? = null,
)

@Serializable
class Asn private constructor(
	val asn: Long,
	val name: String? = null,
	val country: String? = null,
	val countryName: String? = null,
)

@Serializable
class Mac private constructor(
	val mac: String,
	val valid: Boolean,
	val vendor: String? = null,
	val local: Boolean? = null,
	val multicast: Boolean? = null,
)

@Serializable
class Mx private constructor(
	val domain: String,
	val mx: List<MxRecord> = emptyList(),
)

@Serializable
class UseragentDeviceDeep private constructor(
	val type: String? = null,
	val brand: String? = null,
	val model: String? = null,
	val cpu: String? = null,
	val touchscreen: Boolean? = null,
)

@Serializable
class UseragentOsDeep private constructor(
	val name: String? = null,
	val version: String? = null,
	val platform: String? = null,
)

@Serializable
class UseragentBrowserBrand private constructor(
	val brand: String,
	val version: String,
)

@Serializable
class UseragentBrowserDeep private constructor(
	val name: String? = null,
	val version: String? = null,
	val type: String? = null,
	val brands: List<UseragentBrowserBrand>? = null,
)

@Serializable
class UseragentEngineDeep private constructor(
	val name: String? = null,
	val version: String? = null,
)

@Serializable
class UseragentBot private constructor(
	val name: String? = null,
	val category: String? = null,
	val vendor: String? = null,
	val url: String? = null,
)

@Serializable
class UseragentDeep private constructor(
	val device: UseragentDeviceDeep? = null,
	val os: UseragentOsDeep? = null,
	val browser: UseragentBrowserDeep? = null,
	val engine: UseragentEngineDeep? = null,
	val headless: Boolean? = null,
	val bot: UseragentBot? = null,
	val ai: Boolean? = null,
)

@Serializable
class Useragent private constructor(
	val useragent: String,
	val device: String? = null,
	val os: String? = null,
	val browser: String? = null,
	val bot: Boolean,
	val mobile: Boolean,
	val deep: UseragentDeep? = null,
)

@Serializable
class Currency private constructor(
	val currency: String,
	val numeric: Int? = null,
	val name: String,
	val namePlural: String? = null,
	val symbol: String? = null,
	val symbolNative: String? = null,
	val digits: Int? = null,
	val countries: List<String> = emptyList(),
)

/** One language by BCP 47 shortest code (en) or ISO 639-3 (eng). Codes are lowercase. */
@Serializable
class Language private constructor(
	val language: String,
	val iso3: String? = null,
	val name: String,
	val localName: String? = null,
	val script: String? = null,
	val direction: String,
	val countries: List<String> = emptyList(),
)

/**
 * A parsed person name. Junk input returns valid false, never an error.
 * Gender comes from dictionary data and is null when the data does not decide.
 */
@Serializable
class Name private constructor(
	val name: String,
	val valid: Boolean,
	val prefix: String? = null,
	val first: String? = null,
	val middle: String? = null,
	val last: String? = null,
	val suffix: String? = null,
	val gender: String? = null,
	val salutation: String? = null,
)

@Serializable
class CurrencyRate private constructor(
	val base: String,
	val quote: String,
	val rate: Double,
	val date: String,
	val amount: Double? = null,
	val converted: Double? = null,
	val source: String? = null,
)

@Serializable
class TimezoneNextDst private constructor(
	val at: String,
	val dst: Boolean,
	val offset: String,
	val abbreviation: String,
)

@Serializable
class Timezone private constructor(
	/** Echoed on coordinate lookups only. */
	val latitude: Double? = null,
	val longitude: Double? = null,
	val timezone: String? = null,
	val name: String? = null,
	val abbreviation: String? = null,
	val offset: String? = null,
	val offsetMinutes: Int? = null,
	val dst: Boolean? = null,
	val nextDst: TimezoneNextDst? = null,
	val at: String? = null,
	val to: TimezoneConversionTarget? = null,
)

/** Calendar facts. Invalid or ambiguous input has valid false and null calendar fields. */
@Serializable
class DateInfo private constructor(
	val date: String,
	val valid: Boolean,
	val year: Int? = null,
	val month: Int? = null,
	val monthName: String? = null,
	val day: Int? = null,
	/** ISO weekday, Monday 1 through Sunday 7. */
	val weekday: Int? = null,
	val weekdayName: String? = null,
	val week: Int? = null,
	val weekYear: Int? = null,
	val dayOfYear: Int? = null,
	val quarter: Int? = null,
	val leap: Boolean? = null,
	val daysInMonth: Int? = null,
	/** Unix time at midnight UTC, in seconds. */
	val unix: Long? = null,
	val to: String? = null,
	val days: Int? = null,
)

@Serializable
class Holiday private constructor(
	val date: String,
	val name: String,
	val localName: String? = null,
	/** public for an official day off, observance for cultural days. */
	val type: String,
	val regions: List<String>? = null,
	val substitute: Boolean,
)

@Serializable
class HolidayYear private constructor(
	val country: String,
	val year: Int,
	val holidays: List<Holiday> = emptyList(),
)

@Serializable
class HolidayDate private constructor(
	val country: String,
	val date: String,
	val holiday: Holiday? = null,
)

@Serializable
class Elevation private constructor(
	val latitude: Double,
	val longitude: Double,
	val elevation: Double? = null,
	val elevationFt: Double? = null,
	val resolution: Double? = null,
)

@Serializable
class PointDeep private constructor(
	val city: CityNearest? = null,
	val timezone: Timezone? = null,
)

@Serializable
class Point private constructor(
	val latitude: Double,
	val longitude: Double,
	val country: String? = null,
	val countryName: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val district: String? = null,
	val districtName: String? = null,
	val elevation: Double? = null,
	val elevationFt: Double? = null,
	val resolution: Double? = null,
	val deep: PointDeep? = null,
)

@Serializable
class WeatherForecastPeriod private constructor(
	val name: String,
	val start: String? = null,
	val end: String? = null,
	val daytime: Boolean? = null,
	val temperature: Double? = null,
	val temperatureF: Double? = null,
	val precipitationChance: Double? = null,
	val windSpeed: Double? = null,
	val windSpeedMph: Double? = null,
	val windDirection: Double? = null,
	val condition: String? = null,
	val conditionName: String? = null,
	val conditionEmoji: String? = null,
)

@Serializable
class WeatherAlert private constructor(
	val event: String,
	val severity: String? = null,
	val urgency: String? = null,
	val headline: String? = null,
	val onset: String? = null,
	val expires: String? = null,
)

@Serializable
class WeatherHour private constructor(
	val at: String? = null,
	val daytime: Boolean? = null,
	val temperature: Double? = null,
	val temperatureF: Double? = null,
	val humidity: Double? = null,
	val precipitationChance: Double? = null,
	val windSpeed: Double? = null,
	val windSpeedMph: Double? = null,
	val windDirection: Double? = null,
	val condition: String? = null,
	val conditionName: String? = null,
	val conditionEmoji: String? = null,
	val feelsLike: Double? = null,
	val feelsLikeF: Double? = null,
	val windGust: Double? = null,
	val windGustMph: Double? = null,
)

@Serializable
class WeatherDeep private constructor(
	val forecast: List<WeatherForecastPeriod>? = null,
	val alerts: List<WeatherAlert>? = null,
	val hours: List<WeatherHour>? = null,
	val minutes: List<WeatherMinute>? = null,
	val days: List<WeatherDay>? = null,
	val air: WeatherAir? = null,
	val history: WeatherHistory? = null,
)

@Serializable
class WeatherCurrent private constructor(
	val temperature: Double? = null,
	val temperatureF: Double? = null,
	val feelsLike: Double? = null,
	val feelsLikeF: Double? = null,
	val dewpoint: Double? = null,
	val dewpointF: Double? = null,
	val humidity: Double? = null,
	val windSpeed: Double? = null,
	val windSpeedMph: Double? = null,
	val windGust: Double? = null,
	val windGustMph: Double? = null,
	val windDirection: Double? = null,
	val pressure: Double? = null,
	val pressureInhg: Double? = null,
	val visibility: Double? = null,
	val visibilityMi: Double? = null,
	val condition: String? = null,
	val conditionName: String? = null,
	val conditionEmoji: String? = null,
	val observedAt: String? = null,
)

@Serializable
class WeatherStation private constructor(
	val id: String,
	val name: String? = null,
	val distance: Double? = null,
	val distanceMi: Double? = null,
)

@Serializable
class WeatherSource private constructor(
	val id: String,
	val name: String? = null,
)

@Serializable
class Weather private constructor(
	val latitude: Double,
	val longitude: Double,
	val current: WeatherCurrent,
	val station: WeatherStation? = null,
	val source: WeatherSource,
	val deep: WeatherDeep? = null,
)

@Serializable
class EmojiSkin private constructor(
	val emoji: String,
	val tone: String,
	val unicode: String? = null,
	val hex: String? = null,
)

@Serializable
class Emoji private constructor(
	val emoji: String,
	val name: String,
	val shortcodes: List<String> = emptyList(),
	val codepoints: List<String> = emptyList(),
	val hex: String,
	val category: String? = null,
	val status: String? = null,
	val version: String? = null,
	val keywords: List<String> = emptyList(),
	val skins: List<EmojiSkin> = emptyList(),
)

@Serializable
class EmojiSearch private constructor(
	val q: String,
	val emojis: List<Emoji> = emptyList(),
)

@Serializable
class WeatherMinute private constructor(
	val at: String? = null,
	val precipitation: Double? = null,
	val precipitationIn: Double? = null,
	val type: String? = null,
)

@Serializable
class WeatherDay private constructor(
	val date: String? = null,
	val high: Double? = null,
	val highF: Double? = null,
	val low: Double? = null,
	val lowF: Double? = null,
	val precipitationChance: Double? = null,
	val condition: String? = null,
	val conditionName: String? = null,
	val conditionEmoji: String? = null,
	val sunrise: String? = null,
	val sunset: String? = null,
	val moonPhase: String? = null,
	val moonPhaseName: String? = null,
	val moonPhaseEmoji: String? = null,
)

@Serializable
class WeatherAir private constructor(
	val aqi: Double? = null,
	val aqiName: String? = null,
	@SerialName("pm2_5") val pm25: Double? = null,
	val pm10: Double? = null,
)

@Serializable
class WeatherHistory private constructor(
	val date: String? = null,
	val high: Double? = null,
	val highF: Double? = null,
	val low: Double? = null,
	val lowF: Double? = null,
	val precipitation: Double? = null,
	val precipitationIn: Double? = null,
	val windMax: Double? = null,
	val windMaxMph: Double? = null,
	val sunrise: String? = null,
	val sunset: String? = null,
	val moonPhase: String? = null,
	val moonPhaseName: String? = null,
	val moonPhaseEmoji: String? = null,
)

@Serializable
class TimezoneConversionTarget private constructor(
	val timezone: String,
	val name: String? = null,
	val abbreviation: String? = null,
	val offset: String,
	val offsetMinutes: Int,
	val dst: Boolean,
	val at: String,
)

@Serializable
class Address private constructor(
	val address: String? = null,
	val valid: Boolean,
	val registered: Boolean? = null,
	val number: String? = null,
	val street: String? = null,
	val unit: String? = null,
	val city: String? = null,
	val district: String? = null,
	val districtName: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val postal: String? = null,
	val country: String? = null,
	val countryName: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val deep: AddressDeep? = null,
)

@Serializable
class AddressSuggestion private constructor(
	val address: String,
	val number: String? = null,
	val street: String? = null,
	val unit: String? = null,
	val city: String? = null,
	val state: String? = null,
	val postal: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
)

@Serializable
class AddressSearch private constructor(
	val q: String,
	val postal: String? = null,
	val city: String? = null,
	val state: String? = null,
	val country: String? = null,
	val addresses: List<AddressSuggestion> = emptyList(),
)

@Serializable
class CompanyCountry private constructor(
	val name: String? = null,
	val blocs: List<String> = emptyList(),
	val tax: String? = null,
)

@Serializable
class CompanyDeep private constructor(
	val country: CompanyCountry? = null,
	val postal: Postal? = null,
	val city: City? = null,
)

@Serializable
class Company private constructor(
	val company: String? = null,
	val valid: Boolean,
	val registered: Boolean? = null,
	val country: String? = null,
	val type: String? = null,
	val name: String? = null,
	val active: Boolean? = null,
	val activity: String? = null,
	val address: String? = null,
	val city: String? = null,
	val state: String? = null,
	val stateName: String? = null,
	val postal: String? = null,
	val countryName: String? = null,
	val vat: String? = null,
	val gst: Boolean? = null,
	val acn: String? = null,
	val siren: String? = null,
	val siege: Boolean? = null,
	val kind: String? = null,
	val invoice: String? = null,
	val deep: CompanyDeep? = null,
)

@Serializable
class AddressDeep private constructor()
