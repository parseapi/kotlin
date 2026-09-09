package com.parseapi

import java.net.HttpURLConnection
import java.net.URI
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
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
class ParseAPIRequest internal constructor(
	val url: String,
	val headers: Map<String, String>,
	val timeoutMs: Int,
)

/** Raw transport answer. Header names are lowercase. */
class ParseAPIResponse(
	val status: Int,
	val body: String,
	val headers: Map<String, String>,
)

/** Suspending transport hook for tests and instrumentation. Cooperates with coroutine cancellation. */
fun interface ParseAPITransport {
	suspend fun execute(request: ParseAPIRequest): ParseAPIResponse
}

private object HttpURLConnectionTransport : ParseAPITransport {
	override suspend fun execute(request: ParseAPIRequest): ParseAPIResponse =
		kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
			val connection = URI(request.url).toURL().openConnection() as HttpURLConnection
			connection.instanceFollowRedirects = false
			connection.connectTimeout = request.timeoutMs
			connection.readTimeout = request.timeoutMs
			request.headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
			// Disconnect outside the caller's thread: some JVM implementations can
			// block during cleanup. Cancellation still resumes the caller promptly.
			continuation.invokeOnCancellation {
				Dispatchers.IO.dispatch(kotlin.coroutines.EmptyCoroutineContext, Runnable { connection.disconnect() })
			}
			Dispatchers.IO.dispatch(continuation.context, Runnable {
				if (!continuation.isActive) return@Runnable
				try {
					val status = connection.responseCode
					val stream = if (status >= 400) connection.errorStream else connection.inputStream
					val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
					val headers = buildMap {
						connection.headerFields.forEach { (name, values) ->
							if (name != null && values.isNotEmpty()) put(name.lowercase(java.util.Locale.ROOT), values[0])
						}
					}
					continuation.resumeWith(Result.success(ParseAPIResponse(status, body, headers)))
				} catch (error: Exception) {
					connection.disconnect()
					continuation.resumeWith(Result.failure(error))
				}
			})
		}
}

/** Reusable ParseAPI client. Configure optional settings with a trailing block. */
class ParseAPI private constructor(key: String?, options: ParseAPIOptions) {
	// You found Dev. https://parseapi.com/dev
	constructor() : this(null, ParseAPIOptions())
	constructor(key: String?) : this(key, ParseAPIOptions())
	constructor(configure: ParseAPIOptions.() -> Unit) : this(null, ParseAPIOptions().apply(configure))
	constructor(key: String?, configure: ParseAPIOptions.() -> Unit) : this(key, ParseAPIOptions().apply(configure))

	private val key: String = (key ?: System.getenv("PARSEAPI_KEY"))?.takeIf { it.isNotEmpty() }
		?: throw ParseAPIException(0, "missing_api_key", "ParseAPI: missing API key. Pass one or set PARSEAPI_KEY.", null, null)
	private val appId = options.appId
	private val timeoutMs = options.timeoutMs
	private val retries = options.retries
	private val transport = options.transport ?: HttpURLConnectionTransport
	private val baseUrl = (options.baseUrl ?: System.getenv("PARSEAPI_BASE_URL") ?: "https://api.parseapi.com").trimEnd('/')

	init {
		require(timeoutMs > 0) { "timeoutMs must be positive" }
		require(retries == null || retries >= 0) { "retries must be nonnegative" }
		val uri = try { URI(baseUrl) } catch (error: Exception) { throw IllegalArgumentException("baseUrl must be an absolute HTTP(S) URL", error) }
		require(uri.scheme?.lowercase(java.util.Locale.ROOT) in setOf("http", "https") && !uri.host.isNullOrEmpty() && uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null) {
			"baseUrl must be an absolute HTTP(S) URL without credentials, query, or fragment"
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	private val json: Json = Json {
		ignoreUnknownKeys = true
		coerceInputValues = true
		namingStrategy = JsonNamingStrategy.SnakeCase
	}

	// Methods mirror routes exactly, flattened like Go.

	/**
	 * Look up an IP. Deep enrichment is included with a paid plan, without a separate check meter.
	 */
	suspend fun ip(ip: String): Ip =
		ip(ip) {}

	/**
	 * Look up an IP. Deep enrichment is included with a paid plan, without a separate check meter.
	 */
	suspend fun ip(ip: String, configure: IpOptions.() -> Unit): Ip =
		with(IpOptions().apply(configure)) {
			get("/ip/${enc(ip)}", deepQuery(deep))
		}

	/**
	 * Look up the public IP making this request. On a server, this is the server's IP.
	 */
	suspend fun ipSelf(): Ip =
		ipSelf() {}

	/**
	 * Look up the public IP making this request. On a server, this is the server's IP.
	 */
	suspend fun ipSelf(configure: IpSelfOptions.() -> Unit): Ip =
		with(IpSelfOptions().apply(configure)) {
			get("/ip", deepQuery(deep))
		}

	suspend fun continent(code: String): Continent =
		get("/continent/${enc(code)}")

	suspend fun continentCountries(code: String): ContinentCountries =
		get("/continent/${enc(code)}/countries")

	suspend fun country(code: String): Country = country(code) {}

	suspend fun country(code: String, configure: CountryOptions.() -> Unit): Country =
		with(CountryOptions().apply(configure)) {
			get("/country/${enc(code)}", deepQuery(deep))
		}

	suspend fun bloc(code: String): Bloc =
		get("/bloc/${enc(code)}")

	suspend fun blocCountries(code: String): BlocCountries =
		get("/bloc/${enc(code)}/countries")

	suspend fun countryStates(code: String): CountryStates =
		get("/country/${enc(code)}/states")

	suspend fun state(code: String): State =
		state(code) {}

	suspend fun state(code: String, configure: StateOptions.() -> Unit): State =
		with(StateOptions().apply(configure)) {
			get("/state/${enc(code)}", listOf("country" to country) + deepQuery(deep))
		}

	suspend fun stateDistricts(code: String): StateDistricts =
		stateDistricts(code) {}

	suspend fun stateDistricts(code: String, configure: StateDistrictsOptions.() -> Unit): StateDistricts =
		with(StateDistrictsOptions().apply(configure)) {
			get("/state/${enc(code)}/districts", listOf("country" to country) + deepQuery(deep))
		}

	suspend fun district(code: String): District =
		district(code) {}

	suspend fun district(code: String, configure: DistrictOptions.() -> Unit): District =
		with(DistrictOptions().apply(configure)) {
			get("/district/${enc(code)}", listOf("country" to country, "state" to state) + deepQuery(deep))
		}

	suspend fun city(name: String): City =
		city(name) {}

	suspend fun city(name: String, configure: CityOptions.() -> Unit): City =
		with(CityOptions().apply(configure)) {
			get("/city/${enc(name)}", listOf("country" to country, "state" to state) + deepQuery(deep))
		}

	/** Pin or refetch a city by its minted id (city_ + 12 chars). */
	suspend fun cityId(id: String): City = cityId(id) {}

	suspend fun cityId(id: String, configure: CityIdOptions.() -> Unit): City =
		with(CityIdOptions().apply(configure)) {
			get("/city/id/${enc(id)}", deepQuery(deep))
		}

	suspend fun citySearch(query: String): CitySearch =
		citySearch(query) {}

	suspend fun citySearch(query: String, configure: CitySearchOptions.() -> Unit): CitySearch =
		with(CitySearchOptions().apply(configure)) {
			get("/city", listOf("q" to query, "country" to country, "state" to state, "limit" to limit?.toString()) + deepQuery(deep))
		}

	suspend fun cityNearest(lat: Double, lon: Double): CityNearest = cityNearest(lat, lon) {}

	suspend fun cityNearest(lat: Double, lon: Double, configure: CityNearestOptions.() -> Unit): CityNearest =
		with(CityNearestOptions().apply(configure)) {
			get("/city", listOf("lat" to num(lat), "lon" to num(lon)) + deepQuery(deep))
		}

	suspend fun cityNearby(name: String): CityNearby =
		cityNearby(name) {}

	suspend fun cityNearby(name: String, configure: CityNearbyOptions.() -> Unit): CityNearby =
		with(CityNearbyOptions().apply(configure)) {
			get(
				"/city/${enc(name)}/nearby",
				listOf(
					"radius" to radius?.let(::num),
					"unit" to unit,
					"country" to country,
					"state" to state,
					"limit" to limit?.toString(),
				) + deepQuery(deep),
			)
		}

	/** One language by BCP 47 shortest code (en) or ISO 639-3 (eng). */
	suspend fun language(code: String): Language = language(code) {}

	suspend fun language(code: String, configure: LanguageOptions.() -> Unit): Language =
		with(LanguageOptions().apply(configure)) {
			get("/language/${enc(code)}", deepQuery(deep))
		}

	/** Parse a person's name. Junk input returns valid false, never an error. */
	suspend fun name(name: String): Name =
		name(name) {}

	/** Parse a name with an optional ISO2 country context for gender. */
	suspend fun name(name: String, configure: NameOptions.() -> Unit): Name =
		with(NameOptions().apply(configure)) {
			get("/name/${enc(name)}", listOf("country" to country) + deepQuery(deep))
		}

	/**
	 * Look up a postal area. Pass country when known. Check nullable coordinates before another
	 * location lookup.
	 */
	suspend fun postal(code: String): Postal =
		postal(code) {}

	/**
	 * Look up a postal area. Pass country when known. Check nullable coordinates before another
	 * location lookup.
	 */
	suspend fun postal(code: String, configure: PostalOptions.() -> Unit): Postal =
		with(PostalOptions().apply(configure)) {
			get("/postal/${enc(code)}", listOf("country" to country) + deepQuery(deep))
		}

	suspend fun postalNearby(code: String): PostalNearby =
		postalNearby(code) {}

	suspend fun postalNearby(code: String, configure: PostalNearbyOptions.() -> Unit): PostalNearby =
		with(PostalNearbyOptions().apply(configure)) {
			get("/postal/${enc(code)}/nearby", listOf("country" to country, "radius" to radius?.let(::num), "unit" to unit) + deepQuery(deep))
		}

	suspend fun postalDistance(from: String, to: String): PostalDistance =
		postalDistance(from, to) {}

	suspend fun postalDistance(from: String, to: String, configure: PostalDistanceOptions.() -> Unit): PostalDistance =
		with(PostalDistanceOptions().apply(configure)) {
			get("/postal/${enc(from)}/distance/${enc(to)}", listOf("country" to country) + deepQuery(deep))
		}

	/**
	 * Parse an email and check its format and domain. Deep explicitly requests a metered
	 * deliverability check. Deep checks use one attempt by default. An explicit retry count can
	 * repeat paid usage. Metered checks require a secret key on a server. App keys return empty
	 * deep.
	 */
	suspend fun email(email: String): Email =
		email(email) {}

	/**
	 * Parse an email and check its format and domain. Deep explicitly requests a metered
	 * deliverability check. Deep checks use one attempt by default. An explicit retry count can
	 * repeat paid usage. Metered checks require a secret key on a server. App keys return empty
	 * deep.
	 */
	suspend fun email(email: String, configure: EmailOptions.() -> Unit): Email =
		with(EmailOptions().apply(configure)) {
			get("/email/${enc(email)}", deepQuery(deep))
		}

	/**
	 * Check VAT format and checksum. Deep requests a metered registry check where supported. Deep
	 * checks use one attempt by default. Supply your own VAT number for a consultation reference
	 * when supported. Metered checks require a secret key on a server. App keys return empty deep.
	 */
	suspend fun vat(number: String): Vat =
		vat(number) {}

	/**
	 * Check VAT format and checksum. Deep requests a metered registry check where supported. Deep
	 * checks use one attempt by default. Supply your own VAT number for a consultation reference
	 * when supported. Metered checks require a secret key on a server. App keys return empty deep.
	 */
	suspend fun vat(number: String, configure: VatOptions.() -> Unit): Vat =
		with(VatOptions().apply(configure)) {
			get("/vat/${enc(number)}", listOf("country" to country, "from" to from) + deepQuery(deep))
		}

	/** Checksum and structure. bank and branch are codes inside the number, not names. */
	suspend fun iban(iban: String): Iban =
		iban(iban) {}

	suspend fun iban(iban: String, configure: IbanOptions.() -> Unit): Iban =
		with(IbanOptions().apply(configure)) {
			get("/iban/${enc(iban)}", listOf("country" to country) + deepQuery(deep))
		}

	/** Look up a US healthcare provider by NPI. Deep adds Medicare enrollment on paid plans. */
	suspend fun npi(npi: String): Npi =
		npi(npi) {}

	suspend fun npi(npi: String, configure: NpiOptions.() -> Unit): Npi =
		with(NpiOptions().apply(configure)) {
			get("/npi/${enc(npi)}", deepQuery(deep))
		}

	/**
	 * Parse a phone number and its formats. Pass country for national numbers when needed. Deep
	 * reveals numbering-plan location and timezone on every plan. Carrier, caller, and HLR are separate metered lookups.
	 */
	suspend fun phone(number: String): Phone =
		phone(number) {}

	/**
	 * Parse a phone number and its formats. Pass country for national numbers when needed. Deep
	 * reveals numbering-plan location and timezone on every plan. Carrier, caller, and HLR are separate metered lookups.
	 */
	suspend fun phone(number: String, configure: PhoneOptions.() -> Unit): Phone =
		with(PhoneOptions().apply(configure)) {
			get("/phone/${enc(number)}", listOf("country" to country) + deepQuery(deep))
		}

	/**
	 * Request a metered carrier lookup. No automatic retries by default. Use a secret key on a
	 * server. App keys return a 403.
	 */
	suspend fun carrier(number: String): Carrier =
		carrier(number) {}

	/**
	 * Request a metered carrier lookup. No automatic retries by default. Use a secret key on a
	 * server. App keys return a 403.
	 */
	suspend fun carrier(number: String, configure: CarrierOptions.() -> Unit): Carrier =
		with(CarrierOptions().apply(configure)) {
			get("/carrier/${enc(number)}", listOf("country" to country) + deepQuery(deep))
		}

	/**
	 * Request a metered caller-name lookup for a NANP number. No automatic retries by default. Use
	 * a secret key on a server. App keys return a 403.
	 */
	suspend fun caller(number: String): Caller =
		caller(number) {}

	/**
	 * Request a metered caller-name lookup for a NANP number. No automatic retries by default. Use
	 * a secret key on a server. App keys return a 403.
	 */
	suspend fun caller(number: String, configure: CallerOptions.() -> Unit): Caller =
		with(CallerOptions().apply(configure)) {
			get("/caller/${enc(number)}", listOf("country" to country))
		}

	/**
	 * Look up phone status at the last check. Live means assigned and connected means reachable at
	 * that check. Cached results may be returned. Null means unconfirmed. Deep adds network
	 * diagnostics within the same metered lookup. No automatic retries by default. Use a secret key on your server. App keys return 403.
	 */
	suspend fun hlr(number: String): Hlr =
		hlr(number) {}

	/**
	 * Look up phone status at the last check. Live means assigned and connected means reachable at
	 * that check. Cached results may be returned. Null means unconfirmed. Deep adds network
	 * diagnostics within the same metered lookup. No automatic retries by default. Use a secret key on your server. App keys return 403.
	 */
	suspend fun hlr(number: String, configure: HlrOptions.() -> Unit): Hlr =
		with(HlrOptions().apply(configure)) {
			get("/hlr/${enc(number)}", listOf("country" to country) + deepQuery(deep))
		}

	/** Check whether a domain is registered. */
	suspend fun domain(domain: String): Domain =
		domain(domain) {}

	/** Check whether a domain is registered. Deep adds registration dates, registrar, status and DNSSEC on paid plans. */
	suspend fun domain(domain: String, configure: DomainOptions.() -> Unit): Domain =
		with(DomainOptions().apply(configure)) {
			get("/domain/${enc(domain)}", deepQuery(deep))
		}

	suspend fun asn(asn: String): Asn =
		get("/asn/${enc(asn)}")

	suspend fun mac(mac: String): Mac =
		get("/mac/${enc(mac)}")

	/** Look up a 6-11 digit card prefix. Preserve leading zeros in the string. */
	suspend fun bin(bin: String): Bin = bin(bin) {}

	suspend fun bin(bin: String, configure: BinOptions.() -> Unit): Bin =
		with(BinOptions().apply(configure)) {
			get("/bin/${enc(bin)}", deepQuery(deep))
		}


	/** Parse or convert a measurement. Amount is a decimal string. Without to, use its canonical unit. */
	suspend fun measure(measure: String): Measure = measure(measure) {}

	/** Locale and system (us or imperial) resolve explicit ambiguity. Invalid measurements return valid=false. */
	suspend fun measure(measure: String, configure: MeasureOptions.() -> Unit): Measure =
		with(MeasureOptions().apply(configure)) {
			get("/measure/${enc(measure)}", listOf("to" to to, "locale" to locale, "system" to system))
		}

	suspend fun measureUnits(): MeasureUnits = measureUnits {}

	/** Discover reviewed units. unit filters compatible conversion targets. */
	suspend fun measureUnits(configure: MeasureUnitsOptions.() -> Unit): MeasureUnits =
		with(MeasureUnitsOptions().apply(configure)) {
			get("/measure/units", listOf("q" to query, "type" to type, "unit" to unit))
		}


	/** Published DNS records with TTLs. Pooled on every plan. */
	suspend fun dns(domain: String): Dns = dns(domain) {}

	/** Omit type to check all supported types. Values retain DNS presentation syntax. */
	suspend fun dns(domain: String, configure: DnsOptions.() -> Unit): Dns =
		with(DnsOptions().apply(configure)) {
			get("/dns/${enc(domain)}", listOf("type" to type))
		}

	suspend fun mx(domain: String): Mx =
		get("/mx/${enc(domain)}")

	/**
	 * Parses the given user agent string. It is sent as the User-Agent
	 * header for this one request.
	 */
	suspend fun useragent(ua: String): Useragent =
		useragent(ua) {}

	suspend fun useragent(ua: String, configure: UseragentOptions.() -> Unit): Useragent =
		with(UseragentOptions().apply(configure)) {
			get("/useragent", deepQuery(deep), userAgent = ua)
		}

	/** Decodes a 17-character VIN. Deep adds open recall campaigns on paid plans. */
	suspend fun vin(vin: String): Vin =
		vin(vin) {}

	suspend fun vin(vin: String, configure: VinOptions.() -> Unit): Vin =
		with(VinOptions().apply(configure)) {
			get("/vin/${enc(vin)}", deepQuery(deep))
		}

	/**
	 * Look up the general US duty schedule line. Paid deep adds units and the special and other
	 * schedule columns. Add origin with deep to resolve country-specific measures. Without origin,
	 * schedule detail remains available and origin-dependent fields are null. A null effective rate is
	 * not a zero rate.
	 */
	suspend fun tariff(code: String): Tariff =
		tariff(code) {}

	/**
	 * Look up the general US duty schedule line. Paid deep adds units and the special and other
	 * schedule columns. Add origin with deep to resolve country-specific measures. Without origin,
	 * schedule detail remains available and origin-dependent fields are null. A null effective rate is
	 * not a zero rate.
	 */
	suspend fun tariff(code: String, configure: TariffOptions.() -> Unit): Tariff =
		with(TariffOptions().apply(configure)) {
			get("/tariff/${enc(code)}", listOf("origin" to origin) + deepQuery(deep))
		}

	/** US NAICS 2022 definition and hierarchy. */
	suspend fun naics(code: String): NAICS = naics(code) {}

	suspend fun naics(code: String, configure: NaicsOptions.() -> Unit): NAICS =
		with(NaicsOptions().apply(configure)) {
			get("/naics/${enc(code)}", deepQuery(deep))
		}

	/** Keyword search. Limit defaults to 10 and accepts 1-50. */
	suspend fun naicsSearch(query: String): NAICSSearch = naicsSearch(query) {}

	suspend fun naicsSearch(query: String, configure: NaicsSearchOptions.() -> Unit): NAICSSearch =
		with(NaicsSearchOptions().apply(configure)) {
			get("/naics", listOf("q" to query, "limit" to limit?.toString()) + deepQuery(deep))
		}

	/** Searches tariff schedule descriptions by product. */
	suspend fun tariffSearch(query: String): TariffSearch =
		get("/tariff", listOf("q" to query))

	suspend fun currency(code: String): Currency = currency(code) {}

	suspend fun currency(code: String, configure: CurrencyOptions.() -> Unit): Currency =
		with(CurrencyOptions().apply(configure)) {
			get("/currency/${enc(code)}", deepQuery(deep))
		}

	/** Daily official reference cross rate. Pass date for a past day, amount to convert. */
	suspend fun currencyRate(base: String, quote: String): CurrencyRate =
		currencyRate(base, quote) {}

	suspend fun currencyRate(base: String, quote: String, configure: CurrencyRateOptions.() -> Unit): CurrencyRate =
		with(CurrencyRateOptions().apply(configure)) {
			get("/currency/${enc(base)}/${enc(quote)}", listOf("date" to date, "amount" to amount?.let(::num)))
		}

	/** Current local time, UTC by default. With to, offsetless at is source wall time. */
	suspend fun time(timezone: String? = null): Time = time(timezone) {}

	suspend fun time(timezone: String? = null, configure: TimeOptions.() -> Unit): Time =
		with(TimeOptions().apply(configure)) {
			get(timezone?.let { "/time/${enc(it)}" } ?: "/time", listOf("at" to at, "to" to to) + deepQuery(deep))
		}

	suspend fun timeAt(lat: Double, lon: Double): Time = timeAt(lat, lon) {}

	suspend fun timeAt(lat: Double, lon: Double, configure: TimeAtOptions.() -> Unit): Time =
		with(TimeAtOptions().apply(configure)) {
			get("/time", listOf("lat" to num(lat), "lon" to num(lon), "at" to at, "to" to to) + deepQuery(deep))
		}

	suspend fun timezone(id: String): Timezone =
		timezone(id) {}

	suspend fun timezone(id: String, configure: TimezoneOptions.() -> Unit): Timezone =
		with(TimezoneOptions().apply(configure)) {
			get("/timezone/${enc(id)}", listOf("at" to at, "to" to to) + deepQuery(deep))
		}

	/** Coords in, zone out. */
	suspend fun timezoneAt(lat: Double, lon: Double): Timezone =
		timezoneAt(lat, lon) {}

	suspend fun timezoneAt(lat: Double, lon: Double, configure: TimezoneAtOptions.() -> Unit): Timezone =
		with(TimezoneAtOptions().apply(configure)) {
			get("/timezone", listOf("lat" to num(lat), "lon" to num(lon), "at" to at) + deepQuery(deep))
		}

	suspend fun holiday(country: String): HolidayYear =
		holiday(country) {}

	suspend fun holiday(country: String, configure: HolidayOptions.() -> Unit): HolidayYear =
		with(HolidayOptions().apply(configure)) {
			get("/holiday/${enc(country)}", listOf("year" to year?.toString()))
		}

	/** Calendar facts for a date. Use format for month-first or day-first input. */
	suspend fun date(date: String): DateInfo =
		date(date) {}

	suspend fun date(date: String, configure: DateOptions.() -> Unit): DateInfo =
		with(DateOptions().apply(configure)) {
			get("/date/${enc(date)}", listOf("format" to format, "to" to to) + deepQuery(deep))
		}

	/** Today's calendar date in UTC. Pass to for the signed day difference. */
	suspend fun dateToday(): DateInfo =
		dateToday() {}

	suspend fun dateToday(configure: DateTodayOptions.() -> Unit): DateInfo =
		with(DateTodayOptions().apply(configure)) {
			get("/date", listOf("to" to to) + deepQuery(deep))
		}

	/** One date. A covered date that is not a holiday answers holiday null. */
	suspend fun holidayDate(country: String, date: String): HolidayDate =
		get("/holiday/${enc(country)}/${enc(date)}")

	suspend fun elevation(lat: Double, lon: Double): Elevation =
		get("/elevation", listOf("lat" to num(lat), "lon" to num(lon)))

	/**
	 * Resolve the country, state, district and timezone at coordinates. Deep adds terrain and compact
	 * nearest-city context on every plan. The timezone ID stays in core. The nearest city is null when
	 * none is within 200 km.
	 */
	suspend fun point(lat: Double, lon: Double): Point =
		point(lat, lon) {}

	/**
	 * Resolve the country, state, district and timezone at coordinates. Deep adds terrain and compact
	 * nearest-city context on every plan. The timezone ID stays in core. The nearest city is null when
	 * none is within 200 km.
	 */
	suspend fun point(lat: Double, lon: Double, configure: PointOptions.() -> Unit): Point =
		with(PointOptions().apply(configure)) {
			get("/point", listOf("lat" to num(lat), "lon" to num(lon)) + deepQuery(deep))
		}

	/**
	 * Get current conditions in metric and imperial units. Paid deep adds specialist current
	 * measurements, forecasts and related detail. With deep, date selects a past UTC day (YYYY-MM-DD)
	 * in deep.history alongside current conditions. Date alone does not request history.
	 */
	suspend fun weather(lat: Double, lon: Double): Weather =
		weather(lat, lon) {}

	/**
	 * Get current conditions in metric and imperial units. Paid deep adds specialist current
	 * measurements, forecasts and related detail. With deep, date selects a past UTC day (YYYY-MM-DD)
	 * in deep.history alongside current conditions. Date alone does not request history.
	 */
	suspend fun weather(lat: Double, lon: Double, configure: WeatherOptions.() -> Unit): Weather =
		with(WeatherOptions().apply(configure)) {
			get("/weather", listOf("lat" to num(lat), "lon" to num(lon), "date" to date) + deepQuery(deep))
		}

	suspend fun emoji(emoji: String): Emoji = emoji(emoji) {}

	suspend fun emoji(emoji: String, configure: EmojiOptions.() -> Unit): Emoji =
		with(EmojiOptions().apply(configure)) {
			get("/emoji/${enc(emoji)}", deepQuery(deep))
		}

	suspend fun emojiSearch(query: String): EmojiSearch =
		emojiSearch(query) {}

	suspend fun emojiSearch(query: String, configure: EmojiSearchOptions.() -> Unit): EmojiSearch =
		with(EmojiSearchOptions().apply(configure)) {
			get("/emoji", listOf("q" to query, "limit" to limit?.toString()) + deepQuery(deep))
		}

	suspend fun address(address: String): Address = address(address) {}
	suspend fun address(address: String, configure: AddressOptions.() -> Unit): Address = with(AddressOptions().apply(configure)) {
		get("/address/${enc(address)}", listOf("country" to country) + deepQuery(deep))
	}

	/**
	 * Find address suggestions using the context supplied. Prefer postal, or city and state, from the
	 * form; ip is an optional end-user locality hint for server-side calls. An empty result has reason
	 * more_input, missing_context or no_matches. Suggestions have reason null. Operational failures
	 * are errors.
	 */
	suspend fun addressSearch(query: String): AddressSearch = addressSearch(query) {}
	/**
	 * Find address suggestions using the context supplied. Prefer postal, or city and state, from the
	 * form; ip is an optional end-user locality hint for server-side calls. An empty result has reason
	 * more_input, missing_context or no_matches. Suggestions have reason null. Operational failures
	 * are errors.
	 */
	suspend fun addressSearch(query: String, configure: AddressSearchOptions.() -> Unit): AddressSearch = with(AddressSearchOptions().apply(configure)) {
		get("/address", listOf("q" to query, "country" to country, "postal" to postal, "city" to city, "state" to state, "ip" to ip))
	}

	suspend fun company(number: String): Company = company(number) {}
	suspend fun company(number: String, configure: CompanyOptions.() -> Unit): Company = with(CompanyOptions().apply(configure)) {
		get("/company/${enc(number)}", listOf("country" to country) + deepQuery(deep))
	}

	// Transport

	private fun deepQuery(deep: Boolean): List<Pair<String, String?>> =
		if (deep) listOf("deep" to "true") else emptyList()

	private fun num(value: Double): String =
		if (value == Math.floor(value) && kotlin.math.abs(value) < 1e15) value.toLong().toString() else value.toString()

	private suspend inline fun <reified T> get(
		path: String,
		query: List<Pair<String, String?>> = emptyList(),
		userAgent: String? = null,
	): T = json.decodeFromString(fetch(path, query, userAgent))

	private suspend fun fetch(path: String, query: List<Pair<String, String?>>, userAgent: String?): String {
		var url = baseUrl + path
		val pairs = query.mapNotNull { (name, value) -> value?.let { "$name=${enc(it)}" } }
		if (pairs.isNotEmpty()) url += "?" + pairs.joinToString("&")

		val headers = buildMap {
			put("X-API-Key", key)
			put("User-Agent", userAgent ?: "parseapi-kotlin/$VERSION")
			appId?.let { put("X-App-Id", it) }
		}
		val request = ParseAPIRequest(url, headers, timeoutMs)

		val retryLimit = retries ?: defaultRetries(path, query)
		var attempt = 0
		while (true) {
			currentCoroutineContext().ensureActive()
			val response = try {
				transport.execute(request)
			} catch (error: Exception) {
				if (error is CancellationException || error is ParseAPIException) throw error
				currentCoroutineContext().ensureActive()
				if (attempt < retryLimit) {
					delay(retryDelayMs(attempt, null))
					attempt++
					continue
				}
				throw error
			}

			if (response.status in 200..299) return response.body

			if (response.status in RETRY_STATUS && attempt < retryLimit) {
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

	internal fun retryDelayMs(attempt: Int, retryAfter: String?): Long {
		retryAfter?.toDoubleOrNull()?.let { seconds ->
			if (seconds.isFinite() && seconds >= 0) return min(seconds * 1_000, RETRY_AFTER_CAP_MS.toDouble()).toLong()
		}
		if (retryAfter != null) {
			for (pattern in listOf("EEE, dd MMM yyyy HH:mm:ss zzz", "EEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy")) {
				val format = java.text.SimpleDateFormat(pattern, java.util.Locale.US).apply {
					timeZone = java.util.TimeZone.getTimeZone("GMT")
					isLenient = false
				}
				val position = java.text.ParsePosition(0)
				val at = format.parse(retryAfter, position)
				if (at != null && position.index == retryAfter.length) return (at.time - System.currentTimeMillis()).coerceIn(0, RETRY_AFTER_CAP_MS)
			}
		}
		return (Random.nextDouble() * min(250 * 2.0.pow(attempt), RETRY_AFTER_CAP_MS.toDouble())).toLong()
	}

	private fun defaultRetries(path: String, query: List<Pair<String, String?>>): Int {
		val product = path.removePrefix("/").substringBefore('/')
		return if (product in setOf("carrier", "caller", "hlr", "litigator", "reassigned") ||
			(product in setOf("email", "vat", "address") && query.any { it.first == "deep" && it.second == "true" })) 0 else 2
	}

	companion object {
		const val VERSION = "0.3.2"
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
