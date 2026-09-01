// Live smoke against the edge. Canary-ready: env-driven, clean exit codes.
//   PARSEAPI_KEY       required (secret key)
//   PARSEAPI_BASE_URL  optional override
//   PARSEAPI_APP_KEY   optional app key (parse_app_): runs the X-App-Id fence checks
//   PARSEAPI_APP_ID    app id listed on that key (required with PARSEAPI_APP_KEY)
// Run: gradle smoke

import com.parseapi.ParseAPI
import com.parseapi.ParseAPIException
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking

var failures = 0
var total = 0

fun check(name: String, ok: Boolean, detail: String = "") {
	total++
	if (!ok) failures++
	println("${if (ok) "ok  " else "FAIL"} $name${if (detail.isEmpty()) "" else " ($detail)"}")
}

suspend fun <T> expectOk(name: String, call: suspend () -> T, assert: (T) -> String?) {
	try {
		val problem = assert(call())
		check(name, problem == null, problem ?: "")
	} catch (error: ParseAPIException) {
		check(name, false, "${error.status} ${error.code}")
	} catch (error: Exception) {
		check(name, false, error.toString())
	}
}

suspend fun expectError(name: String, call: suspend () -> Any?, code: String) {
	try {
		call()
		check(name, false, "expected error, got 200")
	} catch (error: ParseAPIException) {
		check(name, error.code == code, "got ${error.code}")
	} catch (error: Exception) {
		check(name, false, error.toString())
	}
}

fun main(): Unit = runBlocking {
	val parse = try {
		ParseAPI()
	} catch (error: ParseAPIException) {
		println("FAIL missing PARSEAPI_KEY")
		exitProcess(1)
	}

	expectOk("ip", { parse.ip("8.8.8.8") }) { if (it.ip == "8.8.8.8") null else "wrong ip" }
	expectOk("ipSelf", { parse.ipSelf() }) { if (it.ip.isNotEmpty()) null else "no ip" }
	expectOk("continent", { parse.continent("NA") }) { if (it.name == "North America") null else "wrong name" }
	expectOk("continentCountries", { parse.continentCountries("NA") }) { if (it.countries.isNotEmpty()) null else "no countries" }
	expectOk("country", { parse.country("US") }) { if (it.iso3 == "USA") null else "wrong iso3" }
	expectOk("countryStates", { parse.countryStates("US") }) { if (it.states.size >= 50) null else "too few states" }
	expectOk("state", { parse.state("NC", country = "US") }) { if (it.name == "North Carolina") null else "wrong name" }
	expectOk("stateDistricts", { parse.stateDistricts("NC", country = "US") }) { if (it.districts.isNotEmpty()) null else "no districts" }
	expectOk("district", { parse.district("37081") }) { if (it.name.contains("Guilford")) null else "wrong district" }

	var cityId: String? = null
	expectOk("city", { parse.city("charlotte", country = "US") }) {
		when {
			it.name != "Charlotte" -> "wrong city"
			!it.id.startsWith("city_") -> "missing id"
			else -> {
				cityId = it.id
				null
			}
		}
	}
	val pinned = cityId
	if (pinned != null) {
		expectOk("cityId", { parse.cityId(pinned) }) { if (it.id == pinned && it.name == "Charlotte") null else "id mismatch" }
	} else {
		check("cityId", false, "skipped, no id from city")
	}

	expectOk("citySearch", { parse.citySearch("char", country = "US", limit = 5) }) { if (it.cities.isNotEmpty()) null else "no results" }
	expectOk("cityNearest", { parse.cityNearest(35.2271, -80.8431) }) { if (it.distance >= 0) null else "no distance" }
	expectOk("postal", { parse.postal("28202", country = "US") }) { if (it.city == "Charlotte") null else "wrong city" }
	expectOk("postalNearby", { parse.postalNearby("28202", country = "US", radius = 40.0) }) { if (it.nearby.isNotEmpty()) null else "no nearby" }
	expectOk("postalDistance", { parse.postalDistance("28202", "10001", country = "US") }) { if (it.distance > 800 && it.distance < 1000) null else "distance ${it.distance}" }
	expectOk("email", { parse.email("hello@gmail.com") }) { if (it.valid) null else "not valid" }
	expectOk("vat", { parse.vat("DE136695976") }) { if (it.valid && it.country == "DE") null else "not valid DE" }
	expectOk("iban", { parse.iban("DE89370400440532013000") }) {
		if (it.valid && it.country == "DE" && it.bank == "37040044") null else "not valid DE"
	}
	expectOk("iban junk", { parse.iban("hello") }) { if (!it.valid) null else "expected invalid" }
	expectOk("npi", { parse.npi("1881018208") }) {
		if (it.valid && it.registered == true) null else "not registered"
	}
	expectOk("npi junk", { parse.npi("hello") }) { if (!it.valid) null else "expected invalid" }
	expectOk("phone", { parse.phone("+14155552671") }) { if (it.phone == "+14155552671") null else "wrong phone" }
	// Metered core siblings: junk numbers answer 200 valid false, free, no vendor dip.
	expectOk("carrier junk free", { parse.carrier("555-0100") }) { if (!it.valid) null else "expected invalid" }
	expectOk("caller junk free", { parse.caller("555-0100") }) { if (!it.valid) null else "expected invalid" }
	expectOk("hlr junk free", { parse.hlr("555-0100") }) { if (!it.valid) null else "expected invalid" }
	expectOk("domain", { parse.domain("gmail.com") }) { if (!it.available) null else "gmail available?" }
	expectOk("mx", { parse.mx("gmail.com") }) { if (it.mx.isNotEmpty()) null else "no mx" }
	expectOk("useragent", {
		parse.useragent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
	}) { if (it.browser == "Chrome") null else "browser ${it.browser}" }
	expectOk("vin", { parse.vin("1HGCM82633A004352") }) { if (it.valid && it.make == "Honda" && it.year == 2003) null else "wrong decode" }
	expectOk("vin junk", { parse.vin("1HGCM82613A004352") }) { if (!it.valid) null else "expected invalid" }
	expectOk("currency", { parse.currency("USD") }) { if (it.symbol == "$") null else "wrong symbol" }
	expectOk("currencyRate", { parse.currencyRate("USD", "EUR") }) { if (it.rate > 0 && it.rate < 10) null else "rate ${it.rate}" }
	expectOk("language", { parse.language("en") }) { if (it.language == "en" && it.name == "English") null else "wrong language" }
	expectOk("name", { parse.name("BILLY O'SHALL") }) { if (it.name == "Billy O'Shall" && it.valid && it.gender == "male") null else "wrong name" }
	expectOk("ofac", { parse.ofac("AEROCARIBBEAN AIRLINES") }) { if (it.sanctioned && it.matches.firstOrNull()?.list == "sdn") null else "expected sdn match" }
	expectOk("ofac clean", { parse.ofac("Jane Smith") }) { if (!it.sanctioned && it.matches.isEmpty()) null else "expected no match" }
	expectOk("timezone", { parse.timezone("America/New_York") }) { if (it.offsetMinutes == -240 || it.offsetMinutes == -300) null else "offset ${it.offsetMinutes}" }
	expectOk("timezoneAt", { parse.timezoneAt(40.7128, -74.006) }) { if (it.timezone == "America/New_York") null else "zone ${it.timezone}" }
	expectOk("holiday", { parse.holiday("US") }) { if (it.holidays.size > 5) null else "too few holidays" }
	expectOk("holidayDate", { parse.holidayDate("US", "2026-12-25") }) { if (it.holiday?.name == "Christmas Day") null else "not christmas" }
	expectOk("holiday null (not a holiday)", { parse.holidayDate("US", "2026-08-12") }) { if (it.holiday == null) null else "expected null" }
	expectOk("elevation", { parse.elevation(35.2271, -80.8431) }) { if (it.elevation != null) null else "no elevation" }
	expectOk("point", { parse.point(36.0726, -79.792) }) { if (it.country == "US") null else "country ${it.country}" }
	expectOk("weather", { parse.weather(40.7128, -74.006) }) { if (it.station?.id != null) null else "no station" }
	expectOk("emoji", { parse.emoji("rocket") }) { if (it.emoji == "\uD83D\uDE80") null else "wrong emoji" }
	expectOk("emojiSearch", { parse.emojiSearch("fire", limit = 5) }) { if (it.emojis.isNotEmpty()) null else "no results" }

	// Deep triad: asked on a free-deep endpoint always yields an object.
	expectOk("point deep triad", { parse.point(36.0726, -79.792, deep = true) }) { if (it.deep != null) null else "deep missing" }

	// Honest 404 and auth errors.
	expectError("honest 404", { parse.city("notarealcityxyz") }, "not_found")
	expectError("bogus key 401", { ParseAPI("parse_bogusbogusbogus0", retries = 0).country("US") }, "invalid_api_key")

	// App key fence, when an app key is in the env. Good id 200, missing or
	// wrong id 403, metered core 403, email deep locked to {}.
	val appKey = System.getenv("PARSEAPI_APP_KEY")
	val goodAppId = System.getenv("PARSEAPI_APP_ID")
	if (appKey != null && goodAppId != null) {
		val app = ParseAPI(appKey, appId = goodAppId)
		expectOk("app key good id", { app.country("US") }) { if (it.iso3 == "USA") null else "wrong iso3" }
		expectOk("app key wedge lookup", { app.postal("28202", country = "US") }) { if (it.city == "Charlotte") null else "wrong city" }

		val noId = ParseAPI(appKey, retries = 0)
		expectError("app key missing id 403", { noId.country("US") }, "permission_denied")

		val wrongId = ParseAPI(appKey, appId = "com.wrong.app", retries = 0)
		expectError("app key wrong id 403", { wrongId.country("US") }, "permission_denied")

		expectError("app key metered 403", { app.carrier("555-0100") }, "permission_denied")

		expectOk("app key email deep locked", { app.email("hello@gmail.com", deep = true) }) {
			val deep = it.deep ?: return@expectOk "deep missing"
			if (deep.deliverable == null && deep.catchall == null) null else "deep not locked"
		}
	} else {
		println("note app key fence checks skipped (set PARSEAPI_APP_KEY + PARSEAPI_APP_ID)")
	}

	println("\n${total - failures}/$total passed")
	exitProcess(if (failures == 0) 0 else 1)
}
