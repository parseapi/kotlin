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
data class IpDeep(
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
data class Ip(
	val ip: String,
	val country: String? = null,
	val countryName: String? = null,
	val continent: String? = null,
	val asn: String? = null,
	val asnName: String? = null,
	val deep: IpDeep? = null,
)

@Serializable
data class Continent(
	val continent: String,
	val name: String,
	val region: String,
	val subregion: String,
	val population: Long? = null,
	val area: Double? = null,
	val emoji: String,
)

@Serializable
data class ContinentCountryItem(
	val country: String,
	val name: String,
	val emoji: String? = null,
	val callingCode: String? = null,
)

@Serializable
data class ContinentCountries(
	val continent: String,
	val countries: List<ContinentCountryItem>,
)

@Serializable
data class Country(
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
)

@Serializable
data class CountryStateItem(
	val state: String,
	val name: String,
	val type: String? = null,
)

@Serializable
data class CountryStates(
	val country: String,
	val states: List<CountryStateItem>,
)

@Serializable
data class State(
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
data class StateDistrictItem(
	val district: String,
	val name: String,
	val type: String? = null,
)

@Serializable
data class StateDistricts(
	val state: String,
	val stateName: String? = null,
	val country: String,
	val countryName: String? = null,
	val districts: List<StateDistrictItem>,
)

@Serializable
data class District(
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
	val landArea: Double? = null,
	val waterArea: Double? = null,
	val seat: String? = null,
	val timezone: String? = null,
	val timezones: List<String> = emptyList(),
)

@Serializable
data class City(
	val name: String,
	val localName: String? = null,
	val type: String? = null,
	val capital: String? = null,
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
	val landArea: Double? = null,
	val waterArea: Double? = null,
	val timezone: String? = null,
	/** Minted parse id (city_ + 12 chars). Stable pin via cityId(). */
	val id: String,
)

/** Nearest-city lookups add the distance from the query point. */
@Serializable
data class CityNearest(
	val name: String,
	val localName: String? = null,
	val type: String? = null,
	val capital: String? = null,
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
	val landArea: Double? = null,
	val waterArea: Double? = null,
	val timezone: String? = null,
	val id: String,
	val distance: Double,
	val distanceMi: Double,
)

@Serializable
data class CitySearch(
	val q: String,
	val country: String? = null,
	val state: String? = null,
	val cities: List<City>,
)

@Serializable
data class CityNearby(
	val city: String,
	val state: String? = null,
	val country: String,
	val radius: Double,
	val unit: String,
	val nearby: List<CityNearest> = emptyList(),
)

@Serializable
data class Postal(
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
	val landArea: Double? = null,
	val waterArea: Double? = null,
	val timezone: String? = null,
	val currency: String? = null,
	val neighbors: List<String> = emptyList(),
)

@Serializable
data class PostalNearbyItem(
	val postal: String,
	val city: String? = null,
	val state: String? = null,
	val country: String,
	val distance: Double,
	val distanceMi: Double,
)

@Serializable
data class PostalNearby(
	val postal: String,
	val country: String,
	val radius: Double,
	val unit: String,
	val nearby: List<PostalNearbyItem>,
)

@Serializable
data class PostalDistanceEnd(
	val postal: String,
	val city: String? = null,
)

@Serializable
data class PostalDistance(
	val country: String,
	val from: PostalDistanceEnd,
	val to: PostalDistanceEnd,
	val distance: Double,
	val distanceMi: Double,
)

@Serializable
data class EmailDeep(
	val deliverable: Boolean? = null,
	val catchall: Boolean? = null,
)

@Serializable
data class Email(
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
data class VatAddress(
	val street: String? = null,
	val city: String? = null,
	val postal: String? = null,
	val country: String? = null,
)

@Serializable
data class VatDeep(
	val registered: Boolean? = null,
	val name: String? = null,
	val address: VatAddress? = null,
	val consultation: String? = null,
	val consulted: String? = null,
)

@Serializable
data class Vat(
	val vat: String? = null,
	val valid: Boolean,
	val country: String? = null,
	val from: String? = null,
	val deep: VatDeep? = null,
)

@Serializable
data class Iban(
	val iban: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/** Print form in groups of four, for display. Null when invalid. */
	val formatted: String? = null,
	val checksum: String? = null,
	/** Bank identifier parsed from the number, not a name. */
	val bank: String? = null,
	/** Branch identifier when that country has one. */
	val branch: String? = null,
	val account: String? = null,
)

@Serializable
data class Npi(
	/** Normalized 10-digit NPI. Invalid input still echoes the fold. */
	val npi: String? = null,
	val valid: Boolean,
	/** Exists in the CMS NPPES registry. */
	val registered: Boolean? = null,
	val active: Boolean? = null,
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
)

@Serializable
data class HtsMeasure(
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
data class HtsDeep(
	/** The origin country the measures were resolved for. */
	val origin: String? = null,
	/** Composed ad valorem percent. Null when the components do not compose cleanly. */
	@SerialName("effective_rate") val effectiveRate: Double? = null,
	/** Every Chapter 99 tariff measure that applies to this code from this origin. */
	val measures: List<HtsMeasure>? = null,
)

@Serializable
data class Hts(
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
	val deep: HtsDeep? = null,
)

@Serializable
data class HtsSearchHit(
	val hts: String,
	val description: String,
	val general: String? = null,
)

@Serializable
data class HtsSearch(
	val q: String,
	val revision: String,
	/** Up to 20 lines, best match first. */
	val codes: List<HtsSearchHit> = emptyList(),
)

@Serializable
data class VinRecall(
	/** Government campaign number. */
	val campaign: String,
	/** Report date, ISO YYYY-MM-DD. */
	val date: String? = null,
	val component: String? = null,
	/** The filed summary verbatim. */
	val summary: String? = null,
)

@Serializable
data class VinDeep(
	/**
	 * Open recall campaigns for the decoded vehicle. Empty when none,
	 * null when the recall registry did not answer.
	 */
	val recalls: List<VinRecall>? = null,
)

@Serializable
data class Vin(
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
class PhoneDeep

@Serializable
data class Phone(
	val phone: String? = null,
	val valid: Boolean,
	/**
	 * What the numbering plan can see: mobile, landline, toll_free, unknown.
	 * Never voip (that is the carrier field's word). Present when valid.
	 */
	val type: String? = null,
	/** NPA-derived state code (US/CA). Present when valid. */
	val state: String? = null,
	val stateName: String? = null,
	val country: String? = null,
	val national: String? = null,
	val international: String? = null,
	val deep: PhoneDeep? = null,
)

@Serializable
data class Carrier(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/** The network's word, including voip. Present when valid. */
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
data class Caller(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/**
	 * CNAM record verbatim (all-caps telco artifact). Null when no record
	 * or outside NANP. Present when valid.
	 */
	val caller: String? = null,
)

@Serializable
data class Hlr(
	val phone: String? = null,
	val valid: Boolean,
	val country: String? = null,
	/** Assigned to a subscriber. Present when valid. */
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
data class MxRecord(
	val priority: Int,
	val host: String,
)

@Serializable
data class DomainRegistration(
	val registered: Boolean? = null,
	val created: String? = null,
	val updated: String? = null,
	val expires: String? = null,
	val registrar: String? = null,
	val status: List<String>? = null,
	val dnssec: Boolean? = null,
)

@Serializable
data class DomainDeep(
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
data class Domain(
	val domain: String,
	val available: Boolean,
	val deep: DomainDeep? = null,
)

@Serializable
data class Mx(
	val domain: String,
	val mx: List<MxRecord>,
)

@Serializable
data class UseragentDeviceDeep(
	val type: String? = null,
	val brand: String? = null,
	val model: String? = null,
	val cpu: String? = null,
	val touchscreen: Boolean? = null,
)

@Serializable
data class UseragentOsDeep(
	val name: String? = null,
	val version: String? = null,
	val platform: String? = null,
)

@Serializable
data class UseragentBrowserBrand(
	val brand: String,
	val version: String,
)

@Serializable
data class UseragentBrowserDeep(
	val name: String? = null,
	val version: String? = null,
	val type: String? = null,
	val brands: List<UseragentBrowserBrand>? = null,
)

@Serializable
data class UseragentEngineDeep(
	val name: String? = null,
	val version: String? = null,
)

@Serializable
data class UseragentBot(
	val name: String? = null,
	val category: String? = null,
	val vendor: String? = null,
	val url: String? = null,
)

@Serializable
data class UseragentDeep(
	val device: UseragentDeviceDeep? = null,
	val os: UseragentOsDeep? = null,
	val browser: UseragentBrowserDeep? = null,
	val engine: UseragentEngineDeep? = null,
	val headless: Boolean? = null,
	val bot: UseragentBot? = null,
	val ai: Boolean? = null,
)

@Serializable
data class Useragent(
	val useragent: String,
	val device: String? = null,
	val os: String? = null,
	val browser: String? = null,
	val bot: Boolean,
	val mobile: Boolean,
	val deep: UseragentDeep? = null,
)

@Serializable
data class Currency(
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
data class Language(
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
data class Name(
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
data class CurrencyRate(
	val base: String,
	val quote: String,
	val rate: Double,
	val date: String,
	val amount: Double? = null,
	val converted: Double? = null,
	val source: String? = null,
)

@Serializable
data class TimezoneNextDst(
	val at: String,
	val dst: Boolean,
	val offset: String,
	val abbreviation: String,
)

@Serializable
data class Timezone(
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
)

@Serializable
data class Holiday(
	val date: String,
	val name: String,
	val localName: String? = null,
	/** public for an official day off, observance for cultural days. */
	val type: String,
	val regions: List<String>? = null,
	val substitute: Boolean,
)

@Serializable
data class HolidayYear(
	val country: String,
	val year: Int,
	val holidays: List<Holiday>,
)

@Serializable
data class HolidayDate(
	val country: String,
	val date: String,
	val holiday: Holiday? = null,
)

@Serializable
data class Elevation(
	val latitude: Double,
	val longitude: Double,
	val elevation: Double? = null,
	val elevationFt: Double? = null,
	val resolution: Double? = null,
)

@Serializable
data class PointDeep(
	val city: CityNearest? = null,
	val timezone: Timezone? = null,
)

@Serializable
data class Point(
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
data class WeatherForecastPeriod(
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
data class WeatherAlert(
	val event: String,
	val severity: String? = null,
	val urgency: String? = null,
	val headline: String? = null,
	val onset: String? = null,
	val expires: String? = null,
)

@Serializable
data class WeatherHour(
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
)

@Serializable
data class WeatherDeep(
	val forecast: List<WeatherForecastPeriod>? = null,
	val alerts: List<WeatherAlert>? = null,
	val hours: List<WeatherHour>? = null,
)

@Serializable
data class WeatherCurrent(
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
data class WeatherStation(
	val id: String,
	val name: String? = null,
	val distance: Double? = null,
	val distanceMi: Double? = null,
)

@Serializable
data class WeatherSource(
	val id: String,
	val name: String? = null,
)

@Serializable
data class Weather(
	val latitude: Double,
	val longitude: Double,
	val current: WeatherCurrent,
	val station: WeatherStation? = null,
	val source: WeatherSource,
	val deep: WeatherDeep? = null,
)

@Serializable
data class EmojiSkin(
	val emoji: String,
	val tone: String,
	val unicode: String? = null,
	val hex: String? = null,
)

@Serializable
data class Emoji(
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
data class EmojiSearch(
	val q: String,
	val emojis: List<Emoji>,
)
