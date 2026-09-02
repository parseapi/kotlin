package com.parseapi

import java.net.HttpURLConnection
import java.net.URI
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Every non-2xx response from the API. Branch on [code], never on the message.
 *
 * @property status HTTP status.
 * @property code Machine-readable error code, e.g. not_found, invalid_api_key, rate_limited.
 * @property docs Link to the docs section for this error.
 * @property requestId Send this if you contact support.
 */
class ParseAPIException(
	val status: Int,
	val code: String,
	message: String,
	val docs: String?,
	val requestId: String?,
) : Exception(message)

/** One prepared GET, handed to the transport. */
data class ParseAPIRequest(
	val url: String,
	val headers: Map<String, String>,
	val timeoutMs: Int,
)

/** Raw transport answer. Header names are lowercase. */
data class ParseAPIResponse(
	val status: Int,
	val body: String,
	val headers: Map<String, String>,
)

/** Transport hook for tests and instrumentation. Blocking, the client wraps it in Dispatchers.IO. */
fun interface ParseAPITransport {
	fun execute(request: ParseAPIRequest): ParseAPIResponse
}

private object HttpURLConnectionTransport : ParseAPITransport {
	override fun execute(request: ParseAPIRequest): ParseAPIResponse {
		val connection = URI(request.url).toURL().openConnection() as HttpURLConnection
		connection.connectTimeout = request.timeoutMs
		connection.readTimeout = request.timeoutMs
		request.headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
		val status = connection.responseCode
		val stream = if (status >= 400) connection.errorStream else connection.inputStream
		val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
		val headers = buildMap {
			connection.headerFields.forEach { (name, values) ->
				if (name != null && values.isNotEmpty()) put(name.lowercase(), values[0])
			}
		}
		return ParseAPIResponse(status, body, headers)
	}
}

/**
 * parseAPI client. One instance keeps one connection pool alive.
 *
 *     val parse = ParseAPI("parse_app_...", appId = "com.example.weather")
 *     val ip = parse.ip("8.8.8.8")
 *
 * @param key API key. Falls back to the PARSEAPI_KEY environment variable.
 * @param appId App identity sent as X-App-Id (your applicationId / package name),
 *   matched against the key's app-id list for app keys. Secret keys ignore it.
 * @param baseUrl Override https://api.parseapi.com (tests, canaries).
 *   Also read from PARSEAPI_BASE_URL.
 * @param timeoutMs Per-attempt timeout in milliseconds. Default 10000.
 * @param retries Retries after the first attempt on network errors / 429 / 5xx.
 *   Default 2, 0 disables.
 * @param transport Custom transport (tests, instrumentation).
 */
class ParseAPI(
	key: String? = null,
	private val appId: String? = null,
	baseUrl: String? = null,
	private val timeoutMs: Int = 10_000,
	private val retries: Int = 2,
	private val transport: ParseAPITransport = HttpURLConnectionTransport,
) {
	private val key: String = key
		?: System.getenv("PARSEAPI_KEY")?.takeIf { it.isNotEmpty() }
		?: throw ParseAPIException(0, "missing_api_key", "ParseAPI: missing API key. Pass one or set PARSEAPI_KEY.", null, null)

	private val baseUrl: String =
		(baseUrl ?: System.getenv("PARSEAPI_BASE_URL") ?: "https://api.parseapi.com").trimEnd('/')

	@OptIn(ExperimentalSerializationApi::class)
	@PublishedApi
	internal val json: Json = Json {
		ignoreUnknownKeys = true
		namingStrategy = JsonNamingStrategy.SnakeCase
	}

	// Methods mirror routes exactly, flattened like Go.

	suspend fun ip(ip: String, deep: Boolean = false): Ip =
		get("/ip/${enc(ip)}", deepQuery(deep))

	/**
	 * Bare /ip: the caller's own IP record. The SDK always sends its key,
	 * so this rides the keyed path.
	 */
	suspend fun ipSelf(deep: Boolean = false): Ip =
		get("/ip", deepQuery(deep))

	suspend fun continent(code: String): Continent =
		get("/continent/${enc(code)}")

	suspend fun continentCountries(code: String): ContinentCountries =
		get("/continent/${enc(code)}/countries")

	suspend fun country(code: String): Country =
		get("/country/${enc(code)}")

	suspend fun countryStates(code: String): CountryStates =
		get("/country/${enc(code)}/states")

	suspend fun state(code: String, country: String? = null): State =
		get("/state/${enc(code)}", listOf("country" to country))

	suspend fun stateDistricts(code: String, country: String? = null): StateDistricts =
		get("/state/${enc(code)}/districts", listOf("country" to country))

	suspend fun district(code: String, country: String? = null, state: String? = null): District =
		get("/district/${enc(code)}", listOf("country" to country, "state" to state))

	suspend fun city(name: String, country: String? = null, state: String? = null): City =
		get("/city/${enc(name)}", listOf("country" to country, "state" to state))

	/** Pin or refetch a city by its minted id (city_ + 12 chars). */
	suspend fun cityId(id: String): City =
		get("/city/id/${enc(id)}")

	suspend fun citySearch(q: String, country: String? = null, state: String? = null, limit: Int? = null): CitySearch =
		get("/city", listOf("q" to q, "country" to country, "state" to state, "limit" to limit?.toString()))

	suspend fun cityNearest(lat: Double, lon: Double): CityNearest =
		get("/city", listOf("lat" to num(lat), "lon" to num(lon)))

	suspend fun cityNearby(
		name: String,
		radius: Double? = null,
		unit: String? = null,
		country: String? = null,
		state: String? = null,
		limit: Int? = null,
	): CityNearby =
		get(
			"/city/${enc(name)}/nearby",
			listOf(
				"radius" to radius?.let(::num),
				"unit" to unit,
				"country" to country,
				"state" to state,
				"limit" to limit?.toString(),
			),
		)

	/** One language by BCP 47 shortest code (en) or ISO 639-3 (eng). */
	suspend fun language(code: String): Language =
		get("/language/${enc(code)}")

	/** Parse a person's name. Junk input returns valid false, never an error. */
	suspend fun name(name: String): Name =
		get("/name/${enc(name)}")

	suspend fun postal(code: String, country: String? = null): Postal =
		get("/postal/${enc(code)}", listOf("country" to country))

	suspend fun postalNearby(code: String, country: String? = null, radius: Double? = null, unit: String? = null): PostalNearby =
		get("/postal/${enc(code)}/nearby", listOf("country" to country, "radius" to radius?.let(::num), "unit" to unit))

	suspend fun postalDistance(from: String, to: String, country: String? = null): PostalDistance =
		get("/postal/${enc(from)}/distance/${enc(to)}", listOf("country" to country))

	suspend fun email(email: String, deep: Boolean = false): Email =
		get("/email/${enc(email)}", deepQuery(deep))

	/** Format and checksum on every call. Deep asks the live EU registry. */
	suspend fun vat(number: String, country: String? = null, from: String? = null, deep: Boolean = false): Vat =
		get("/vat/${enc(number)}", listOf("country" to country, "from" to from) + deepQuery(deep))

	/** Checksum and structure. bank and branch are codes inside the number, not names. */
	suspend fun iban(iban: String, country: String? = null): Iban =
		get("/iban/${enc(iban)}", listOf("country" to country))

	/** NPI lookup in the CMS NPPES registry of US healthcare providers. Deep adds Medicare enrollment on paid plans. */
	suspend fun npi(npi: String, deep: Boolean = false): Npi =
		get("/npi/${enc(npi)}", deepQuery(deep))

	suspend fun phone(number: String, country: String? = null, deep: Boolean = false): Phone =
		get("/phone/${enc(number)}", listOf("country" to country) + deepQuery(deep))

	/** Metered core. Not available on app keys, use a secret key server-side. */
	suspend fun carrier(number: String, country: String? = null): Carrier =
		get("/carrier/${enc(number)}", listOf("country" to country))

	/** Metered core, NANP only. Not available on app keys. */
	suspend fun caller(number: String, country: String? = null): Caller =
		get("/caller/${enc(number)}", listOf("country" to country))

	/** Metered core, worldwide. Not available on app keys. */
	suspend fun hlr(number: String, country: String? = null): Hlr =
		get("/hlr/${enc(number)}", listOf("country" to country))

	suspend fun domain(domain: String, deep: Boolean = false): Domain =
		get("/domain/${enc(domain)}", deepQuery(deep))

	suspend fun mx(domain: String): Mx =
		get("/mx/${enc(domain)}")

	/**
	 * Parses the given user agent string. It is sent as the User-Agent
	 * header for this one request.
	 */
	suspend fun useragent(ua: String, deep: Boolean = false): Useragent =
		get("/useragent", deepQuery(deep), userAgent = ua)

	/** Decodes a 17-character VIN. Deep adds open recall campaigns on paid plans. */
	suspend fun vin(vin: String, deep: Boolean = false): Vin =
		get("/vin/${enc(vin)}", deepQuery(deep))

	/**
	 * Looks up US import duty for an HTS code. Deep with an origin
	 * resolves the Chapter 99 tariff measures that apply from that country.
	 */
	suspend fun tariff(code: String, deep: Boolean = false, origin: String? = null): Hts =
		get("/tariff/${enc(code)}", listOf("origin" to origin) + deepQuery(deep))

	/** Searches tariff schedule descriptions by product. */
	suspend fun tariffSearch(q: String): HtsSearch =
		get("/tariff", listOf("q" to q))

	suspend fun currency(code: String): Currency =
		get("/currency/${enc(code)}")

	/** Daily official reference cross rate. Pass date for a past day, amount to convert. */
	suspend fun currencyRate(base: String, quote: String, date: String? = null, amount: Double? = null): CurrencyRate =
		get("/currency/${enc(base)}/${enc(quote)}", listOf("date" to date, "amount" to amount?.let(::num)))

	suspend fun timezone(id: String, at: String? = null): Timezone =
		get("/timezone/${enc(id)}", listOf("at" to at))

	/** Coords in, zone out. */
	suspend fun timezoneAt(lat: Double, lon: Double, at: String? = null): Timezone =
		get("/timezone", listOf("lat" to num(lat), "lon" to num(lon), "at" to at))

	suspend fun holiday(country: String, year: Int? = null): HolidayYear =
		get("/holiday/${enc(country)}", listOf("year" to year?.toString()))

	/** One date. A covered date that is not a holiday answers holiday null. */
	suspend fun holidayDate(country: String, date: String): HolidayDate =
		get("/holiday/${enc(country)}/${enc(date)}")

	suspend fun elevation(lat: Double, lon: Double): Elevation =
		get("/elevation", listOf("lat" to num(lat), "lon" to num(lon)))

	suspend fun point(lat: Double, lon: Double, deep: Boolean = false): Point =
		get("/point", listOf("lat" to num(lat), "lon" to num(lon)) + deepQuery(deep))

	suspend fun weather(lat: Double, lon: Double, deep: Boolean = false): Weather =
		get("/weather", listOf("lat" to num(lat), "lon" to num(lon)) + deepQuery(deep))

	suspend fun emoji(emoji: String): Emoji =
		get("/emoji/${enc(emoji)}")

	suspend fun emojiSearch(q: String, limit: Int? = null): EmojiSearch =
		get("/emoji", listOf("q" to q, "limit" to limit?.toString()))

	// Transport

	private fun deepQuery(deep: Boolean): List<Pair<String, String?>> =
		if (deep) listOf("deep" to "true") else emptyList()

	private fun num(value: Double): String =
		if (value == Math.floor(value) && !value.isInfinite()) value.toLong().toString() else value.toString()

	@PublishedApi
	internal suspend inline fun <reified T> get(
		path: String,
		query: List<Pair<String, String?>> = emptyList(),
		userAgent: String? = null,
	): T = json.decodeFromString(fetch(path, query, userAgent))

	@PublishedApi
	internal suspend fun fetch(path: String, query: List<Pair<String, String?>>, userAgent: String?): String {
		var url = baseUrl + path
		val pairs = query.mapNotNull { (name, value) -> value?.let { "$name=${enc(it)}" } }
		if (pairs.isNotEmpty()) url += "?" + pairs.joinToString("&")

		val headers = buildMap {
			put("X-API-Key", key)
			put("User-Agent", userAgent ?: "parseapi-kotlin/$VERSION")
			appId?.let { put("X-App-Id", it) }
		}
		val request = ParseAPIRequest(url, headers, timeoutMs)

		var attempt = 0
		while (true) {
			val response = try {
				withContext(Dispatchers.IO) { transport.execute(request) }
			} catch (error: Exception) {
				if (error is ParseAPIException) throw error
				if (attempt < retries) {
					delay(retryDelayMs(attempt, null))
					attempt++
					continue
				}
				throw error
			}

			if (response.status in 200..299) return response.body

			if (response.status in RETRY_STATUS && attempt < retries) {
				delay(retryDelayMs(attempt, response.headers["retry-after"]))
				attempt++
				continue
			}

			val body = try {
				json.parseToJsonElement(response.body) as? JsonObject
			} catch (ignored: Exception) {
				null
			}
			fun field(name: String): String? = (body?.get(name) as? JsonPrimitive)?.contentOrNull
			throw ParseAPIException(
				response.status,
				field("code") ?: "unknown_error",
				field("message") ?: "Request failed with status ${response.status}",
				field("docs"),
				field("request_id"),
			)
		}
	}

	private fun retryDelayMs(attempt: Int, retryAfter: String?): Long {
		retryAfter?.toDoubleOrNull()?.let { seconds ->
			if (seconds >= 0) return min(seconds * 1_000, RETRY_AFTER_CAP_MS.toDouble()).toLong()
		}
		return (Random.nextDouble() * 250 * 2.0.pow(attempt)).toLong()
	}

	companion object {
		const val VERSION = "0.2.0"
		private val RETRY_STATUS = setOf(429, 500, 502, 503, 504)
		private const val RETRY_AFTER_CAP_MS = 5_000L
	}
}

// RFC 3986 unreserved characters. Everything else percent-encodes,
// including / in timezone ids, @ in emails, and + in phone numbers.
internal fun enc(value: String): String = buildString {
	for (byte in value.toByteArray(Charsets.UTF_8)) {
		val char = byte.toInt().toChar()
		if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~") {
			append(char)
		} else {
			append('%')
			append("0123456789ABCDEF"[(byte.toInt() shr 4) and 0xF])
			append("0123456789ABCDEF"[byte.toInt() and 0xF])
		}
	}
}
