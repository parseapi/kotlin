```sh
git clone --branch 0.3.0 --depth 1 https://github.com/parseapi/kotlin.git ../parseapi-kotlin
```

Use the source checkout as an included Gradle build. Maven Central publication is not available yet.

```kotlin
// settings.gradle.kts
includeBuild("../parseapi-kotlin")
```

```kotlin
// build.gradle.kts dependencies
implementation("com.parseapi:parseapi:0.3.0")
```

```kotlin
import com.parseapi.ParseAPI

val parse = ParseAPI("parse_app_...") { appId = BuildConfig.APPLICATION_ID }
val ip = parse.ip("8.8.8.8")
```

Get a key at [parseapi.com](https://parseapi.com). In an app, mint an App key on the dashboard and list your application id on it. The client sends `appId` as `X-App-Id` on every request. A missing key falls back to the `PARSEAPI_KEY` environment variable.

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

## Deep

Pass `deep = true` to include the nested deep object with richer fields.

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
