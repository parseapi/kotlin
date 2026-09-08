```sh
git clone --branch 0.3.2 --depth 1 https://github.com/parseapi/kotlin.git ../parseapi-kotlin
```

Use the source checkout as an included Gradle build. Maven Central publication is not available yet.

```kotlin
// settings.gradle.kts
includeBuild("../parseapi-kotlin")
```

```kotlin
// build.gradle.kts dependencies
implementation("com.parseapi:parseapi:0.3.2")
```

```kotlin
import com.parseapi.ParseAPI

val parse = ParseAPI("parse_app_...") { appId = BuildConfig.APPLICATION_ID }
val ip = parse.ip("8.8.8.8")
```

Get a key at [parseapi.com](https://parseapi.com). In an app, mint an App key on the dashboard and list your application id on it. The client sends `appId` as `X-App-Id` on every request. A missing key falls back to the `PARSEAPI_KEY` environment variable.

## Weather from a postal code

Start with the postal code, then pass its coordinates to weather. Reuse the client from the example above.

```kotlin
val place = parse.postal("28202") { country = "US" }
val lat = place.latitude
val lon = place.longitude
if (lat != null && lon != null) {
    val weather = parse.weather(lat, lon)
    println(weather)
}
```

The coordinates represent the postal area. Weather is for that point. Missing coordinates skip the weather lookup. This composition performs two ordinary lookups when coordinates are available, with the retry policy below.

Run the example in your existing async task or suspend function.

## Supply the context you know

Pass `country` when a postal code or national phone number needs disambiguation. A complete international phone number already carries its country context. For a numeric date such as `03/04/2026`, supply the intended `format`. Defaults resolve what the input establishes. Ambiguous input needs your context.

Results are plain data. Pass a returned code or coordinate to another operation when the task needs it. Check nullable values before composing the next call.

## Calls

One method per endpoint, named after the route. Every method is a suspend function.

```kotlin
parse.ip("8.8.8.8")
parse.ipSelf()
parse.email("hello@gmail.com")
parse.vat("DE136695976")
parse.iban("DE89370400440532013000")
parse.npi("1881018208")
parse.phone("+14155552671")
parse.postal("SW1A 1AA")
parse.postal("28202") { country = "US" }
parse.postalNearby("28202") { country = "US"; radius = 40.0 }
parse.postalDistance("28202", "10001") { country = "US" }
parse.city("charlotte") { country = "US" }
parse.cityId("city_mb8mbqrkz8zb")
parse.citySearch("char") { country = "US"; limit = 10 }
parse.cityNearest(35.2271, -80.8431)
parse.cityNearby("denver") { radius = 8.0; unit = "mi" }
parse.country("US")
parse.countryStates("US")
parse.state("colorado")
parse.state("NC") { country = "US" }
parse.stateDistricts("NC") { country = "US" }
parse.district("37081")
parse.continent("NA")
parse.continentCountries("NA")
parse.bloc("EU")
parse.blocCountries("EU")
parse.currency("USD")
parse.currencyRate("USD", "EUR")
parse.language("en")
parse.name("BILLY OSHALL")
parse.name("Andrea") { country = "IT" }
parse.timezone("America/New_York")
parse.timezoneAt(40.7128, -74.006)
parse.date("03/04/2026") { format = "mdy" }
parse.dateToday()
parse.holiday("US") { year = 2026 }
parse.holidayDate("US", "2026-12-25")
parse.elevation(35.2271, -80.8431)
parse.point(36.0726, -79.792)
parse.weather(40.7128, -74.006)
parse.domain("example.com")
parse.asn("AS13335")
parse.mac("00:1B:63:84:45:E6")
parse.mx("example.com")
parse.useragent(uaString)
parse.vin("1HGCM82633A004352")
parse.tariff("8471.30.01.00")
parse.tariffSearch("sunglasses")
parse.emoji("rocket")
parse.emojiSearch("fire")
parse.address("123 Main St") { country = "US" }
parse.addressSearch("123 Main") { postal = "28202"; country = "US" }
parse.company("01234567") { country = "GB" }
```

Every response is a typed, read-only object. Nullable fields are nullable properties. Unknown response fields are ignored.

Reuse a client across calls. Each method performs its own lookup and returns data. `countryStates("US")` fetches the states directly. It does not fetch the country first.

`carrier`, `caller`, and `hlr` are metered lookups for secret keys on a server. App keys answer them with a 403.

## Measurements

```kotlin
val result = parse.measure("5 ft 11 in") { to = "cm" }
val units = parse.measureUnits { unit = "m" }
```

`amount` is a decimal string, such as `"180.34"`. Without `to`, the API returns the canonical unit for the measurement type. Pass `locale` for number formatting and `system` (`us` or `imperial`) when a customary unit needs context. Ambiguous input returns `valid: false`, a `reason`, and available `choices`. Invalid or incompatible target units use the normal API error.

Unit discovery accepts optional `query`, `type`, and `unit` filters. `unit` selects compatible targets. Omit the filters for the reviewed catalog. Both operations use pooled requests.

## Deep

Choose enrichment for the question you need answered.

| Operation | What `deep` requests |
|---|---|
| IP | Richer IP fields included with a paid plan. No separate check meter. |
| Email | A metered deliverability check, using included email checks or enabled on-demand usage. |
| VAT | A metered registry check where supported, using included VAT checks or enabled on-demand usage. |
| Phone | An empty object. Number parsing and formats are already in the core response. |

Carrier, caller, and HLR are separate metered operations. Choose them explicitly when you need their answers. Ordinary lookups retry twice by default. Metered checks use one attempt by default. Setting retries explicitly can repeat paid usage.

Without `deep`, the response omits that key. When requested, it is an empty object if access is locked or the operation has no deep fields. Otherwise it contains the available fields. A missing or null field means unknown.

Metered checks require a secret key on a server. App keys can request paid-plan enrichment when their team has access.

```kotlin
val ip = parse.ip("52.94.76.10") { deep = true }
if (ip.deep?.datacenter == true) {
    // datacenter IP
}
```

## Errors

Every non-2xx response throws a `ParseAPIException` with `status`, `code`, `docs`, and `requestId`. Branch on `code`.

```kotlin
try {
    parse.city("atlantis")
} catch (error: ParseAPIException) {
    if (error.code == "not_found") {
        // no such city
    }
}
```

## Options

```kotlin
val parse = ParseAPI("parse_app_...") {
    appId = "com.example.weather"
    timeoutMs = 10_000
}

val places = parse.postalNearby("28202") {
    country = "US"
    radius = 40.0
}
```

Ordinary lookups retry up to twice on network failures, 429, 500, 502, 503, and 504. Carrier, caller, HLR, and email/VAT deep lookups do not retry automatically. Address deep also uses zero retries, reserved for future verification. Setting `retries` in the client configuration explicitly applies that count to every lookup, including metered requests. A retry may count as another lookup.

Cancellation stops waiting for the response and closes the default connection. Redirects are returned as errors. Your custom transport should cooperate with coroutine cancellation.

The source build uses Kotlin 2.1.20 and targets JVM 11 bytecode for JVM and Android apps. Dependencies: kotlinx-coroutines and kotlinx-serialization only.

## Docs

Full field reference for every endpoint: [parseapi.com/docs](https://parseapi.com/docs)

## Compatibility checks

Run `./gradlew check` before a release. The checked-in `api/parseapi.api` records the public JVM API. Regenerate it with `./gradlew apiDump` only after reviewing an intentional API addition. Response properties can grow without exposing constructor or copy-method signatures. Operation options can grow without changing existing call signatures.

Pushes and pull requests run these checks on Java 11 and 21, then build and run the separate consumer in `compatibility/consumer`. Run that consumer locally with `./gradlew -p compatibility/consumer run`; it uses a test transport and makes no API requests.
