```sh
git clone --branch 1.10.0 --depth 1 https://github.com/parseapi/kotlin.git ../parseapi-kotlin
```

Use the source checkout as an included Gradle build. Maven Central publication is not available yet.

```kotlin
// settings.gradle.kts
includeBuild("../parseapi-kotlin")
```

```kotlin
// build.gradle.kts dependencies
implementation("com.parseapi:parseapi:1.10.0")
```

```kotlin
import com.parseapi.ParseAPI

val parse = ParseAPI("parse_app_...") { appId = BuildConfig.APPLICATION_ID }
val ip = parse.ip("8.8.8.8")
```

Get a key at [parseapi.com](https://parseapi.com). In an app, mint an App key on the dashboard and list your application id on it. The client sends `appId` as `X-App-Id` on every request. A missing key falls back to the `PARSEAPI_KEY` environment variable.

## API versions

This SDK sends `Parse-Version: 2.0.0` on every request, including retries. Its response types match API `2.0.0`, and the client selects that contract automatically. No extra constructor setting or key change is needed. This behavior requires the matching API request-version release.

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
parse.card("424242")
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
parse.industry("541511")
parse.industrySearch("coffee shop") { limit = 5 }
parse.emoji("rocket")
parse.emojiSearch("fire")
parse.address("123 Main St") { country = "US" }
parse.addressSearch("123 Main") { postal = "28202"; country = "US" }
parse.company("01234567") { country = "GB" }
```

Paid Industry `deep` includes full definitions, child categories and classification `exclusions`, each with a description and linked codes. Generic exclusions can have no linked codes. Omitted or null exclusions in older responses remain unknown. Search results keep `country` and `year` on the envelope and optional depth on each result. They also include core `match`: the matched `field` (`name`, `term` or `naics`) and `text`, plus `corrections` with `from` and `to` tokens for typo fallback. Corrections are empty for exact, plural and prefix matches. Direct code lookups omit `match`. Older responses may omit it.

Every response is a typed, read-only object. Nullable fields are nullable properties. Unknown response fields are ignored.

Reuse a client across calls. Each method performs its own lookup and returns data. `countryStates("US")` fetches the states directly. It does not fetch the country first.

`carrier`, `caller`, and `hlr` are metered lookups for secret keys on a server. App keys answer them with a 403.

DNS uses pooled requests on every plan. Omit `type` to check A, AAAA, CNAME, MX, NS, TXT, SOA, CAA, SRV and PTR. Records contain `name`, `type`, `ttl` in seconds and a DNS presentation `value`. TXT values retain quoting and chunk boundaries. A selected question can include its CNAME chain. Empty records mean no records. Lookup failures remain errors.

## Company directory

`companyId`, `companySearch`, `companyCoverage` retrieve directory profiles, search candidates and edition coverage. Existing national company-number validation stays unchanged. Use at most one of query (sent as `q`), domain, ticker or identifier, or discover by country or exact industry; country filters the candidates, exchange narrows a ticker and authority narrows an identifier. The API validates combinations. Each call makes one request and does not automatically resolve candidates or fetch linked assets.

```kotlin
val page = parse.companySearch { domain = "cloudflare.com"; deep = true }
page.companies.firstOrNull()?.let { candidate ->
    val profile = parse.companyId(candidate.id) { deep = true }
    // profile.deep and each CompanyProfileDeep member are nullable.
}
val coverage = parse.companyCoverage()

// Continue a name search with the same selector, filters and limit.
val first = parse.companySearch { query = "Example"; country = "US"; limit = 10 }
first.next?.let { nextCursor ->
    val next = parse.companySearch { query = "Example"; country = "US"; limit = 10; cursor = nextCursor }
}
```

Directory `deep` adds detail to each profile in the same pooled request on every plan. Omitted deep, empty deep and partial detail remain distinct; description, logo, social_profiles, phone_numbers, email_addresses, domains, founded, employees, registrations and sources may be absent on older server releases. Sources attribute only their listed selected enrichment fields. Founding precision is preserved separately from incorporation. Missing listings do not establish private ownership, and a domain match does not prove legal identity. Coverage describes the returned edition, not every company worldwide.

Company directory social profiles contain `platform`, `url` and `handle`; unknown metadata stays null. Phone and email records use `type` for the source-reported purpose. Earlier response fields remain readable by the client.

Discovery example: `parse.companySearch { country = "US"; industry = "0700"; industryType = "sic" }`

Supply `industry` and `industryType` together. The supported namespace is `sic`, with an exact four-digit string such as `0700`; leading zeros are meaningful. Country-only discovery is also supported. Filters intersect and may narrow an existing selector. Country matches the profile country, not a headquarters or operating-presence claim. Unknown values do not match a requested filter. Filter-only candidates use `match.field: "filters"` and `match.value: null`; reuse the same filters and limit with a returned cursor. Counts describe this directory edition, not complete country coverage.


## Time

`time` returns local ISO `at` with its UTC offset and integer Unix seconds in `unix`. Request `deep` for the display name, exact `offset_seconds`, whole `offset_minutes` and next clock change. A conversion target has its own optional `deep` without a next-change field. Historical offsets and ISO times can include offset seconds. Omitted `at` means now. With `to` or `targets`, an offsetless `at` is source wall time. Otherwise it is UTC. Include an offset for repeated local times around a clock change. Current time and conversion use pooled requests on every plan. Coordinate clock fields can be null when the timezone is unknown. Existing `timezone` methods remain supported.

For an offsetless `at` with `to` or `targets`, choose how to handle a clock change with `disambiguation`. It applies to named-zone and coordinate Time calls.

| Value | Repeated time | Skipped time |
| --- | --- | --- |
| `compatible` (default) | Earlier occurrence | Shift forward by the clock change |
| `earlier` | Earlier occurrence | Shift backward by the clock change |
| `later` | Later occurrence | Shift forward by the clock change |
| `reject` | `400 ambiguous_time` | `400 nonexistent_time` |

An explicit UTC offset selects an instant directly. For example, `2026-11-01T01:30:00-04:00` and `2026-11-01T01:30:00-05:00` identify the two New York occurrences. A valid `disambiguation` value has no effect on explicit instants, current-time requests or lookups without `to` or `targets`. For user-entered appointment times, start with `reject`. Handle `ambiguous_time` or `nonexistent_time` by collecting an explicit offset or an earlier/later choice from the user. Other malformed input still uses `invalid_request`.

```kotlin
val result = parse.time("America/New_York") {
    at = "2026-11-01T01:30:00"
    to = "UTC"
    disambiguation = "later"
}
println(result.to?.at) // 2026-11-01T06:30:00+00:00
```

Canonical Time `deep` includes the pinned rule edition in `deep.timezoneDatabaseVersion` and source-wall resolution in `deep.resolution`. Resolution records `kind` (`unique`, `overlap` or `gap`), the selected policy, signed `adjustment_seconds`, and chronological alternatives with exact `at`, Unix seconds and UTC offset. Unique times have an empty alternatives list. Explicit instants, current time and lookups without conversion have null resolution. Destination detail stays compact.

Search serving IANA IDs by city or region, or omit the query to list all (Go and Rust use an empty string). Discovery returns `timezone_database_version` and sorted `timezones`. No search matches returns `timezones: []`.

Pass `targets` to convert one instant to 1-10 zones in a single pooled request. The native list preserves order and duplicates. Use `targets` instead of `to`. The response adds `targets`, with optional detail inside each target. Unknown source coordinates return `targets: null`. An unknown destination rejects the whole request with `not_found`. Omission keeps the original response shape.

```kotlin
val zones = parse.timeZones("New York")
val result = parse.time("UTC") {
    at = "2026-09-24T12:00:00Z"
    targets = listOf("America/New_York", "Asia/Tokyo")
}
println(zones.timezones)
result.targets?.forEach { println("${it.timezone} ${it.at}") }
```

### Location inputs and timezone filters

`parse.time { iata = "JFK"; deep = true }` and `parse.timeZones { country = "US"; dst = false; observesDst = true; details = true }`.

Choose one explicit location input: IP, exact city name or stable city ID, country, IATA airport, ICAO airport, port UN/LOCODE, or address. Country and state can narrow a city or address. State requires country. Address lookup requires US country context and a strict address-point match. Port lookup covers the reviewed port subset, not every assigned UN/LOCODE. IP lookup always uses the supplied IP.

Location calls add `location` with `status`, `candidates`, `truncated`, `source` and the typed input. Check `status` before using the clock: ambiguous or missing locations retain null time fields. Candidate coordinates and IDs can also be null. A country with multiple timezones does not silently choose one. Named-zone and coordinate calls retain their existing signatures.

Timezone discovery accepts country, IANA area, exact signed offset, abbreviation, DST-at-instant and observes-DST-during-year filters. `at` selects the common instant, `sort` selects timezone or offset order, and `details` adds `zones` rows plus the evaluation `at`. The default `timezones` list stays compact. False DST filters are sent explicitly. An abbreviation returns candidate zones rather than choosing one. Observes-DST uses the UTC calendar year containing `at`.

Source deep adds `standard_offset`, `standard_offset_seconds`, signed `dst_offset_seconds` and `season`. Seasonal adjustments can be negative. `season` describes the current DST-flag interval, or the next within 400 days, with actual before/after transition facts and signed `change_seconds`. Unknown boundaries remain null. These fields are optional and nullable, and destination deep stays compact.

## Measurements

```kotlin
val result = parse.measure("5 ft 11 in") { to = "cm" }
val units = parse.measureUnits { unit = "m" }
```

`amount` is a decimal string, such as `"180.34"`. Without `to`, the API returns the canonical unit for the measurement type. Pass `locale` for number formatting and `system` (`us` or `imperial`) when a customary unit needs context. Ambiguous input returns `valid: false`, a `reason`, and available `choices`. Invalid or incompatible target units use the normal API error.

Successful HTTP JSON contains `measure`, `valid`, `type`, `amount`, and `unit`. Invalid input adds `reason`. `choices` appears only when unit alternatives are available. Typed results use `null` or the language-native empty value for an omitted reason and an empty list for omitted choices.

Unit discovery accepts optional `query`, `type`, and `unit` filters. `unit` selects compatible targets. Omit the filters for the reviewed catalog. Both operations use pooled requests.

## Place statistics and optional detail

Australian postal lookups include core `localities` with suburb choices (`city`, `state`, `stateName`) on every plan. Null or an omitted field means unknown, while `[]` means the reviewed reference has no eligible choices. `city` stays null when the source is ambiguous, even if there is only one eligible choice. Let the user select their suburb and keep manual entry available. These are geographic choices, not mailing-address verification. [G-NAF source, adaptations and licence](https://parseapi.com/legal/attribution#postal-au).

Postal and District paid profiles include `deep.property_tax` where supported. It contains `annual_median`, `currency` and `period`: median annual property tax payable on owner-occupied homes in the statistical area. The amount is adjusted to the final year of the reporting period (`YYYY-YYYY`). This is an area statistic, not a rate or an individual property bill. Unsupported, missing and censored estimates are null.

```kotlin
val place = parse.postal("28202") { country = "US"; deep = true }
val propertyTax = place.deep?.propertyTax
```

Read `population` as the source’s count for that place boundary. Keep missing or null values unknown and preserve a known zero. State district lists include each district’s population. Postal nearby and distance detail remains metropolitan associations only. Continent population stays in core.

Point returns the timezone ID with the core location. Its optional deep detail adds terrain and compact nearest-city context on every plan. A nearest city is null when none is within 200 km.

Weather returns current conditions by default. Paid deep adds specialist current measurements, forecasts and related detail. A past `date` is a UTC day and requires deep: it adds `deep.history` alongside current conditions. Date alone does not request history.

```kotlin
parse.weather(40.7128, -74.006) { deep = true; date = "2026-08-15" }
```


Address search uses context from the form: prefer postal, or city and state. An optional end-user `ip` is a locality hint for server-side calls. An empty result explains itself with `reason`: `more_input`, `missing_context` or `no_matches`. With suggestions, reason is null. Older responses may omit it, and future reasons remain strings. Catalog and lookup failures use the existing API errors.

HLR reports status at the last check. `live` means assigned and `connected` means reachable at that check. Cached results may be returned. Null means unconfirmed. Deep diagnostics stay within the same metered lookup.

## Deep

The default call returns the common answer. Request more detail with `parse.country("US") { deep = true }`. Read those fields from the optional deep member; this does not change the core answer.

| Operation | What `deep` requests |
|---|---|
| IP | Richer IP fields included with a paid plan. No separate check meter. |
| Domain | Registration dates, registrar, status and DNSSEC, included with a paid plan. Use `dns` for DNS records and `mx` for mail routing. |
| Email | A metered mailbox check with deliverability, catch-all, status, reason and address hints, using included email checks or enabled on-demand usage. |
| Country, State, City, District, Postal | Reference profiles included with a paid plan; place identity and coordinates stay core. |
| Industry, Company (national number) | Paid technical or registration profiles. Industry hierarchy stays core. |
| Name, Weather | Paid name context or weather detail; parsing and current conditions stay core. |
| Phone | Numbering-plan or bank structure detail in the same pooled request on every plan. |
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

Ordinary lookups retry up to twice on network failures, 429, 500, 502, 503, and 504. Carrier, caller, HLR, and email deep lookups do not retry automatically. Address deep also uses zero retries, reserved for future verification. Setting `retries` in the client configuration explicitly applies that count to every lookup, including metered requests. A retry may count as another lookup.

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

## Emoji catalog additions

Resolve Unicode emoji or a named shortcode, search or browse a bounded catalog,
parse emoji positions in text, and convert between Unicode and a selected shortcode source.
The ordinary lookup remains four fields:

```http
GET /emoji/rocket
```

```json
{"emoji":"🚀","name":"Rocket","shortcodes":[":rocket:","rocket"],"category":"Travel & Places"}
```

Use `deep=true` for Unicode facts, keywords, tone families and available artwork.
Artwork is keyed by style: `deep.artwork.openmoji.svg` is the SVG URL, and
`deep.artwork.openmoji.png["72"]` is the 72×72 PNG URL (use your language’s map
access syntax). The same shape applies to `noto` and `twemoji`. Missing styles
and formats are omitted. URLs pin immutable artwork editions and retain their bytes.
Core values stay unchanged. Text parsing returns `emojis` with UTF-16 `positions`;
conversion returns `text` and unresolved tokens with positions and reasons.

The compact JSON catalog contains offline identity, qualification-form and
shortcode indexes. Request `profile=full` when artwork is needed and `lang` when
localized names and keywords are needed. `/emoji/metadata` carries dataset
coverage and shared source notices once. Artwork supports Google Noto, Twemoji
and OpenMoji, with real SVG/PNG availability and immutable first-party URLs.
A device's emoji font remains the ordinary display.

These additions are a local release candidate until the matching SDK and
backend are published. Existing lookup and search contracts are preserved.


## Canonical City operations

Classic lookup, search, nearest and named-nearby methods retain their default envelopes and required-field models. Browse and coordinate-nearby use canonical lists: `object`, `data` and nullable `next_cursor` (camelCase properties in Swift/Kotlin). Keep all query options unchanged when continuing a page; no client fetches pages automatically.

```kotlin
val page = parse.citySearchList("Viena") {
    match = "fuzzy"; maxDistance = 1
}
val children = parse.cityChildren("city_7aj899nvwyku")
val distance = parse.cityDistance("city_7aj899nvwyku", "city_7aj899nvwyku")
```

Explicit resource and search-list/nearby-list methods return complete City resources with `id` and `object`. Existing classic methods keep their published return types. Country inclusion/exclusion sets, timezone sets, inclusive population and meter-elevation bounds, stored-center `bbox` and signed multi-key sorting are available for collections. Unknown numeric facts do not match range filters.

Search defaults to prefix matching. Exact and fuzzy matching are explicit; fuzzy accepts maximum edit distance 1 or 2 and returns `match` evidence (`type`, `source`, `value`, `language`, `edit_distance`). Review a fuzzy choice before storing its ID.

Reviewed children, ancestors and siblings use canonical lists with partial relationship coverage. Empty data does not prove there are no geographic relatives. Children/siblings accept page controls; ancestors do not. Distance reports great-circle kilometers and miles between stored centers: zero is known zero, null is unknown, and it does not imply travel time or containment.

Strict `ambiguity=error` can return HTTP 409 `ambiguous_city` with up to two safe candidate identities. The existing error constructor and unrelated error metadata are preserved; candidates are retained only for that status/code and never contain Deep. GeoJSON is available through HTTP; these typed City list methods do not return GeoJSON.


Reviewed name forms:

```kotlin
val city = parse.cityIdResource("city_7aj899nvwyku") { provenance = true; romanized = true }
```

Coverage by country and available fields is described at https://parseapi.com/city#coverage.

`romanized=true` requests reviewed official name forms as `name_romanized` and `romanization_language` (camelCase in Swift/Kotlin). Current reviewed forms cover 20 Japanese whole-city aliases in ja-Latn. Missing or conflicting forms stay null; ASCII folding is not substituted for an official form.


Conditional `If-None-Match` reads and GeoJSON currently use HTTP directly. A matching City ETag returns 304 without a body; ordinary billing and rate limits apply. These JSON client methods do not supply conditional-cache handling.
