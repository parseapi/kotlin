```sh
git clone --branch 1.3.0 --depth 1 https://github.com/parseapi/kotlin.git ../parseapi-kotlin
```

Use the source checkout as an included Gradle build. Maven Central publication is not available yet.

```kotlin
// settings.gradle.kts
includeBuild("../parseapi-kotlin")
```

```kotlin
// build.gradle.kts dependencies
implementation("com.parseapi:parseapi:1.3.0")
```

```kotlin
import com.parseapi.ParseAPI

val parse = ParseAPI("parse_app_...") { appId = BuildConfig.APPLICATION_ID }
val ip = parse.ip("8.8.8.8")
```

Get a key at [parseapi.com](https://parseapi.com). In an app, mint an App key on the dashboard and list your application id on it. The client sends `appId` as `X-App-Id` on every request. A missing key falls back to the `PARSEAPI_KEY` environment variable.

## API versions

Version 1.3.0 sends `Parse-Version: 2.0.0` on every request, including retries. Its response types match API `2.0.0`, and the client selects that contract automatically. No extra constructor setting or key change is needed. This behavior requires the matching API request-version release.

The team setting in [Dashboard API version](https://parseapi.com/dashboard/versions) is the default for requests without a version header. This SDK's header takes precedence without changing that saved default. Existing published packages keep their documented behavior.

Test the new SDK dependency in staging, then deploy the same locked dependency with your application code and existing production key. Future major SDK upgrades can deliberately select a newer API contract, so review their migration notes before upgrading. Rolling back the code and dependency restores the contract selected by that SDK release. If the older SDK does not send a version header, its requests use the team default, which must stay unchanged through that rollback window.

Keep the package version locked in your dependency configuration or lockfile. The selected API contract stays fixed across releases within this planned SDK major. See [API versions and migration](https://parseapi.com/docs/versioning).

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

Name paid deep includes flat `short`, `directory`, and `initials` fields beside `gender` and `salutation`. `nameLocale` selects CLDR formatting rules and defaults to `en`. It changes formatting only. Country remains gender context, and unavailable formatting is null. Older responses may omit these fields.

## Display language

This source candidate accepts an optional language for supported display fields.
It requires the matching API localization release and data.

```kotlin
val country = parse.country("DE") { lang = "fr" }
println(country.name) // Allemagne
```

`lang` applies to this request. The next call uses its usual default unless it
also supplies a language. Codes, native names, numeric facts and response
structure stay unchanged. Missing translations keep the API's documented
fallback. Existing `deep` rules still apply; Date `format` and Measure input
`locale` retain their parsing meanings.

## Calls

One method per endpoint, named after the route. Every method is a suspend function.

```kotlin
parse.ip("8.8.8.8")
parse.ipSelf()
parse.email("hello@gmail.com")
parse.vat("DE136695976")
parse.bank("DE89370400440532013000")
parse.card("424242")
parse.provider("1881018208")
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
parse.name("Robert James Smith") { deep = true; nameLocale = "en" }
parse.time() // UTC now
parse.time("America/New_York")
parse.time("America/New_York") { at = "2026-09-05T15:00:00"; to = "Asia/Tokyo" }
parse.timeAt(40.7128, -74.006)
parse.date("03/04/2026") { format = "mdy" }
parse.dateToday()
parse.holiday("US") { year = 2026 }
parse.holidayDate("US", "2026-12-25")
parse.elevation(35.2271, -80.8431)
parse.point(36.0726, -79.792)
parse.weather(40.7128, -74.006)
parse.domain("example.com")
parse.stack("example.com")
parse.asn("AS13335")
parse.mac("00:1B:63:84:45:E6")
parse.mx("example.com")
parse.dns("example.com")
parse.dns("_dmarc.example.com") { type = "TXT" }
parse.useragent(uaString)
parse.vin("1HGCM82633A004352")
parse.naics("541511")
parse.naicsSearch("coffee shop") { limit = 5 }
parse.tariff("8471.30.01.00")
parse.tariffSearch("sunglasses")
parse.emoji("rocket")
parse.emojiSearch("fire")
parse.address("123 Main St") { country = "US" }
parse.addressSearch("123 Main") { postal = "28202"; country = "US" }
parse.company("01234567") { country = "GB" }
```

Paid NAICS `deep` includes full definitions, child categories and classification `exclusions`, each with a description and linked codes. Generic exclusions can have no linked codes. Omitted or null exclusions in older responses remain unknown. Search results keep `country` and `year` on the envelope and optional depth on each result. They also include core `match`: the matched `field` (`name`, `term` or `naics`) and `text`, plus `corrections` with `from` and `to` tokens for typo fallback. Corrections are empty for exact, plural and prefix matches. Direct code lookups omit `match`. Older responses may omit it.

Every response is a typed, read-only object. Nullable fields are nullable properties. Unknown response fields are ignored.

Reuse a client across calls. Each method performs its own lookup and returns data. `countryStates("US")` fetches the states directly. It does not fetch the country first.

`carrier`, `caller`, and `hlr` are metered lookups for secret keys on a server. App keys answer them with a 403.

DNS uses pooled requests on every plan. Omit `type` to check A, AAAA, CNAME, MX, NS, TXT, SOA, CAA, SRV and PTR. Records contain `name`, `type`, `ttl` in seconds and a DNS presentation `value`. TXT values retain quoting and chunk boundaries. A selected question can include its CNAME chain. Empty records mean no records. Lookup failures remain errors.

## Time

`time` returns local ISO `at` with its UTC offset and integer Unix seconds in `unix`. Request `deep` for the display name, exact `offset_seconds`, whole `offset_minutes` and next clock change. A conversion target has its own optional `deep` without a next-change field. Historical offsets and ISO times can include offset seconds. Omitted `at` means now. With `to`, an offsetless `at` is source wall time. Otherwise it is UTC. Include an offset for repeated local times around a clock change. Current time and conversion use pooled requests on every plan. Coordinate clock fields can be null when the timezone is unknown. Existing `timezone` methods remain supported.

## Measurements

```kotlin
val result = parse.measure("5 ft 11 in") { to = "cm" }
val units = parse.measureUnits { unit = "m" }
```

`amount` is a decimal string, such as `"180.34"`. Without `to`, the API returns the canonical unit for the measurement type. Pass `locale` for number formatting and `system` (`us` or `imperial`) when a customary unit needs context. Ambiguous input returns `valid: false`, a `reason`, and available `choices`. Invalid or incompatible target units use the normal API error.

Unit discovery accepts optional `query`, `type`, and `unit` filters. `unit` selects compatible targets. Omit the filters for the reviewed catalog. Both operations use pooled requests.

## Place statistics and optional detail

Australian postal lookups include core `localities` with suburb choices (`city`, `state`, `stateName`) on every plan. Null or an omitted field means unknown, while `[]` means the reviewed reference has no eligible choices. `city` stays null when the source is ambiguous, even if there is only one eligible choice. Let the user select their suburb and keep manual entry available. These are geographic choices, not mailing-address verification. [G-NAF source, adaptations and licence](https://parseapi.com/legal/attribution#postal-au).

Postal and District paid profiles include `deep.property_tax` where supported. It contains `annual_median`, `currency` and `period`: median annual property tax payable on owner-occupied homes in the statistical area. The amount is adjusted to the final year of the reporting period (`YYYY-YYYY`). This is an area statistic, not a rate or an individual property bill. Unsupported, missing and censored estimates are null.

```kotlin
val place = parse.postal("28202") { country = "US"; deep = true }
val propertyTax = place.deep?.propertyTax
```

Read `population_period` alongside `population`: a reporting year (`YYYY`) or period (`YYYY-YYYY`), null when unknown or unverifiable. Keep missing or null values unknown and preserve a known zero. These fields belong to full place profiles. State district lists include each district's population and period. Postal nearby and distance detail remains metropolitan associations only. Continent population stays in core; Continent has no `population_period` field.

Point returns the timezone ID with the core location. Its optional deep detail adds terrain and compact nearest-city context on every plan. A nearest city is null when none is within 200 km.

Weather returns current conditions by default. Paid deep adds specialist current measurements, forecasts and related detail. A past `date` is a UTC day and requires deep: it adds `deep.history` alongside current conditions. Date alone does not request history.

```kotlin
parse.weather(40.7128, -74.006) { deep = true; date = "2026-08-15" }
```

Tariff starts with the general schedule line. Paid deep adds units and the special and other schedule columns. An optional origin then resolves country-specific measures. The three calls below show those successive choices. Without origin, schedule detail is still returned and origin-dependent fields are null. A null effective rate is not a zero rate.

```kotlin
parse.tariff("8471.30.01.00")
parse.tariff("8471.30.01.00") { deep = true }
parse.tariff("8471.30.01.00") { deep = true; origin = "CN" }
```

Address search uses context from the form: prefer postal, or city and state. An optional end-user `ip` is a locality hint for server-side calls. An empty result explains itself with `reason`: `more_input`, `missing_context` or `no_matches`. With suggestions, reason is null. Older responses may omit it, and future reasons remain strings. Catalog and lookup failures use the existing API errors.

HLR reports status at the last check. `live` means assigned and `connected` means reachable at that check. Cached results may be returned. Null means unconfirmed. Deep diagnostics stay within the same metered lookup.

## Provider lookup

```kotlin
val provider = parse.provider("1881018208")
val profile = parse.provider("1881018208") { deep = true }
```

Pass the original NPI as a string. `valid` checks its format and checksum; `registered` means a match in the stored NPPES snapshot. `active` reflects recorded NPI deactivation, not licensure. `excluded` is an NPI-only OIG LEIE match; `false` is not a complete exclusion clearance. These directory facts do not verify credentials, current practice contact or payment eligibility.

Invalid input returns `valid: false` with unknown provider fields. A checksum-valid number missing from the snapshot returns `registered: false`; unavailable storage remains an API error. Preserve `null` as unknown.

The default pooled lookup includes provider identity, specialty and practice contact where held. Paid `deep` adds `deactivated_at`, `medicare`, `opt_out` and `enrollments` from stored source files, with no separate check meter or live verification. `enrollments: null` means unavailable; `[]` means no enrollment rows are returned. The API omits unrequested `deep` and returns `{}` when requested on Free.

Paid Deep also returns `taxonomies` in published order, with taxonomy code, specialty label, primary flag and provider-reported license number/state, plus `enumerated_at`, `updated_at` and `reactivated_at` record dates. Reported licenses are not verified licenses. Null lists mean unavailable; empty lists mean the edition contains no entries. Core `sources` is available on every plan: NPPES, LEIE, PECOS and opt-out each have nullable edition metadata (`edition`, `published_at`, `through`, `imported_at`). Provider record dates are separate from source publication and completed import dates. Older responses may omit these additions. Edition details remain null until a verified source is served.

## Deep

The default call returns the common answer. Request more detail with `parse.country("US") { deep = true }`. Read those fields from the optional deep member; this does not change the core answer.

| Operation | What `deep` requests |
|---|---|
| IP | Richer IP fields included with a paid plan. No separate check meter. |
| Domain | Registration dates, registrar, status and DNSSEC, included with a paid plan. Use `dns` for DNS records and `mx` for mail routing. |
| Email | A metered mailbox check with deliverability, catch-all, status, reason and address hints, using included email checks or enabled on-demand usage. |
| VAT | A metered registry check where supported, using included VAT checks or enabled on-demand usage. |
| Country, State, City, District, Postal | Reference profiles included with a paid plan; place identity and coordinates stay core. |
| NPI | Deactivation date, Medicare enrollment, opt-out and enrollment rows from stored sources on paid plans. Exclusion evidence stays core. |
| VIN, NAICS, Company | Paid technical or registration profiles. NPI exclusion status and NAICS hierarchy stay core. |
| Tariff | Paid schedule columns and units; add origin for applicable measures. |
| Name, Weather | Paid name context or weather detail; parsing and current conditions stay core. |
| Phone, Bank | Numbering-plan or bank structure detail in the same pooled request on every plan. |
| Time, Date, Currency, Language, Emoji, Point | Optional reference detail in the same pooled request on every plan. |
| Carrier, HLR | Available place or network detail from the same metered core unit, including Free included units. |

Email deep includes mailbox status and the reason for the result, plus a suggested first name, no-reply flag, plus-address tag and mail service. The suggested name is not a verified identity. Unavailable details are null.

Reasons include `accepted`, `invalid_format`, `invalid_domain`, `no_mail_server`, `mailbox_not_found`, `mailbox_disabled`, `mailbox_full`, `catchall`, `disposable`, `temporary_failure`, `rejected` and `unconfirmed`.

Carrier, caller, and HLR are separate metered operations. Choose them explicitly when you need their answers. Ordinary lookups retry twice by default. Metered checks use one attempt by default. Setting retries explicitly can repeat paid usage.

Without `deep`, the response omits that key. When requested, it is an empty object if access is locked or the operation has no deep fields. Otherwise it contains the available fields. A missing or null field means unknown.

Metered checks require a secret key on a server. App keys can request paid-plan enrichment when their team has access.

```kotlin
val ip = parse.ip("52.94.76.10") { deep = true }
if (ip.deep?.datacenter == true) {
    // datacenter IP
}
```

## Bank validation

Bank results include nullable `checks` and `issues` (`BankChecks` and `BankIssue`). Check statuses and issue codes are open strings; handle unknown future values. `not_supported` means the national check did not run, not that it passed. `issues: []` means no applicable check failed; a missing/null value supports older responses. These findings do not establish account existence or ownership. `deep.account` remains a string so leading zeros are preserved.


Bank lookups send raw input in a JSON body (`POST /bank`), preserving leading zeros, separators and forbidden characters for server validation. `bank` keeps its existing call signature and IBAN result. Optional `deep.directory` identifies the directory edition, country and open-string match grain; absent data remains unknown.

```kotlin
val requirements = parse.bankRequirements("US", "us_ach")
val result = parse.bankUsAch(BankUsAchInput("021000021", "000123456789"))
```

US ACH checks the routing checksum and supported account format, not account existence, ownership or ACH eligibility. Account checksum status stays `not_supported`; bank names are nullable partial-directory references. Account text is preserved, including letter case, spaces and hyphens. Requirements describe this validation workflow; they are not every field needed to initiate a payment. Unsupported country/format combinations return `supported: false`. Omit the format argument for IBAN requirements. The sample is synthetic, not an account to pay.

## Errors

Every non-2xx response throws a `ParseAPIException` with `status`, `code`, `docs`, and `requestId`, plus nullable `retryAfter` header metadata. Branch on `code`.

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

Automatic retries honor numeric and HTTP-date `Retry-After` values up to five seconds. A longer server wait returns the original API error immediately, with the raw header in `retryAfter`, so the application can schedule a later attempt. Missing or invalid headers use ordinary backoff.

Cancellation stops waiting for the response and closes the default connection. Redirects are returned as errors. Your custom transport should cooperate with coroutine cancellation.

The source build uses Kotlin 2.1.20 and targets JVM 11 bytecode for JVM and Android apps. Dependencies: kotlinx-coroutines and kotlinx-serialization only.

## Docs

Full field reference for every endpoint: [parseapi.com/docs](https://parseapi.com/docs)

## Compatibility checks

Run `./gradlew check` before a release. The checked-in `api/parseapi.api` records the public JVM API. Regenerate it with `./gradlew apiDump` only after reviewing an intentional API addition. Response properties can grow without exposing constructor or copy-method signatures. Operation options can grow without changing existing call signatures.

Pushes and pull requests run these checks on Java 11 and 21, then build and run the separate consumer in `compatibility/adp-consumer`. Run that consumer locally with `./gradlew -p compatibility/adp-consumer run`. It uses a test transport and makes no API requests.

## Card

Send 2–11 leading digits as a string. Core returns `bin`, `brand`, `brand_name`
and a CDN SVG `logo`. Brand detection uses reviewed network rules independently
of issuer records. Unknown or ambiguous prefixes return null brand fields and a
generic logo; a known network without reviewed artwork also uses the generic logo.

Optional Deep adds `prefix`, `issuer`, `country`, `type` and `prepaid`, included
in the same pooled request on every plan. Six or more digits enable directory
matching. Fewer digits return all-null Deep fields. Compare `deep.prefix` with
`bin`: equal is an exact recorded match; shorter is broader; null is no match.
The longest row wins, including null fields. `prepaid: null` means unknown, not
false. This is partial reference data, not card validity or payment acceptance.

```kotlin
val card = parse.card("51")
println(card.logo)
val details = parse.card("43737400") { deep = true }
println(details.deep?.prefix)
```

Leading zeros are preserved. Only ASCII spaces, tabs, CR, LF and hyphens are
removed; raw input is limited to 64 characters. Invalid prefixes are rejected
before dispatch, accepted input is forwarded unchanged. Never send a full card number.

## Stack

```kotlin
val result = parse.stack("example.com")
```

Pass a public hostname without a scheme, path, port or IP address. Stack returns the homepage URL and `checked_at` time, then eight technology arrays: `cms`, `servers`, `frameworks`, `ecommerce`, `analytics`, `chat`, `payments` and `hosting`. Each entry contains a `technology` code, name and nullable version. Multiple CMSs or servers remain separate entries. Empty arrays mean no matches in the checked pages. An unsuccessful check returns null arrays and a null `checked_at`.

`scope` identifies `homepage` or `site` coverage. `pages` counts successfully checked HTML pages. `partial` is true for a homepage-only or incomplete bounded site check, false when the known in-scope candidates finished, and null when no check succeeded. False does not guarantee that every page on the website was discovered.

The complete technology result is included in the core response. The generic `deep=true` option adds only an empty object and is unnecessary for Stack. Successful checks may be reused for up to 24 hours. `pretty` optionally formats the wire JSON. Each lookup uses one request and API version 2.0.0 selected by this client.

Stack defaults to a 35-second transport timeout so a first scan has time to finish. Other lookups retain their 10-second default. An explicit client timeout takes precedence.
