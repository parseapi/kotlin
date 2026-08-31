# parseapi Kotlin

Official parseAPI client for Kotlin. Android and JVM.

```kotlin
// build.gradle.kts dependencies
implementation("com.parseapi:parseapi:0.1.0")
```

```kotlin
import com.parseapi.ParseAPI

val parse = ParseAPI("parse_app_...", appId = BuildConfig.APPLICATION_ID)
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
parse.phone("+14155552671")
parse.postal("SW1A 1AA")
parse.postal("28202", country = "US")
parse.postalNearby("28202", country = "US", radius = 40.0)
parse.postalDistance("28202", "10001", country = "US")
parse.city("charlotte", country = "US")
parse.cityId("city_mb8mbqrkz8zb")
parse.citySearch("char", country = "US", limit = 10)
parse.cityNearest(35.2271, -80.8431)
parse.cityNearby("denver", radius = 8.0, unit = "mi")
parse.country("US")
parse.countryStates("US")
parse.state("colorado")
parse.state("NC", country = "US")
parse.stateDistricts("NC", country = "US")
parse.district("37081")
parse.continent("NA")
parse.continentCountries("NA")
parse.currency("USD")
parse.currencyRate("USD", "EUR")
parse.language("en")
parse.name("BILLY OSHALL")
parse.timezone("America/New_York")
parse.timezoneAt(40.7128, -74.006)
parse.holiday("US", year = 2026)
parse.holidayDate("US", "2026-12-25")
parse.elevation(35.2271, -80.8431)
parse.point(36.0726, -79.792)
parse.weather(40.7128, -74.006)
parse.domain("example.com")
parse.mx("example.com")
parse.useragent(uaString)
parse.vin("1HGCM82633A004352")
parse.emoji("rocket")
parse.emojiSearch("fire")
```

Every response is a typed data class. Nullable fields are nullable properties.

`carrier`, `caller`, and `hlr` are metered lookups for secret keys on a server. App keys answer them with a 403.

## Deep

Pass `deep = true` to include the nested deep object with richer fields.

```kotlin
val ip = parse.ip("52.94.76.10", deep = true)
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
val parse = ParseAPI(
    "parse_app_...",
    appId = "com.example.weather", // sent as X-App-Id
    timeoutMs = 10_000,            // per-attempt timeout
    retries = 2,                   // automatic retries on network errors, 429, and 5xx
)
```

Requires Kotlin 1.9 or later on JVM 11 or Android. Dependencies: kotlinx-coroutines and kotlinx-serialization only.

## Docs

Full field reference for every endpoint: [parseapi.com/docs](https://parseapi.com/docs)
