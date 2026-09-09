package com.parseapi

/** Client defaults. Configure once when constructing ParseAPI. */
class ParseAPIOptions internal constructor() {
	var appId: String? = null
	var baseUrl: String? = null
	/** Connection and socket-read timeout in milliseconds. */
	var timeoutMs: Int = 10_000
	/** null uses endpoint defaults. An explicit count also applies to metered lookups. */
	var retries: Int? = null
	var transport: ParseAPITransport? = null
}

// Each operation owns its options, so new optional fields leave call signatures stable.
class IpOptions internal constructor() {
	var deep: Boolean = false
}

class IpSelfOptions internal constructor() {
	var deep: Boolean = false
}

class StateOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class StateDistrictsOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class DistrictOptions internal constructor() {
	var country: String? = null
	var state: String? = null
	var deep: Boolean = false
}

class CityOptions internal constructor() {
	var country: String? = null
	var state: String? = null
	var deep: Boolean = false
}

class CitySearchOptions internal constructor() {
	var country: String? = null
	var state: String? = null
	var limit: Int? = null
	var deep: Boolean = false
}

class CityNearbyOptions internal constructor() {
	var radius: Double? = null
	var unit: String? = null
	var country: String? = null
	var state: String? = null
	var limit: Int? = null
	var deep: Boolean = false
}

/** Country is an ISO2 context for gender, not a nationality claim. */
class NameOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class PostalOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class PostalNearbyOptions internal constructor() {
	var country: String? = null
	var radius: Double? = null
	var unit: String? = null
	var deep: Boolean = false
}

class PostalDistanceOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class EmailOptions internal constructor() {
	var deep: Boolean = false
}

class VatOptions internal constructor() {
	var country: String? = null
	var from: String? = null
	var deep: Boolean = false
}

/** Deep requests an empty object on every plan. */
class BinOptions internal constructor() {
	var deep: Boolean = false
}

class IbanOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class NpiOptions internal constructor() {
	var deep: Boolean = false
}

class PhoneOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class CarrierOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class CallerOptions internal constructor() {
	var country: String? = null
}

class HlrOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

/** Type selects the DNS question, including its CNAME chain. Omit for all supported types. */
class DnsOptions internal constructor() {
	var type: String? = null
}

class DomainOptions internal constructor() {
	var deep: Boolean = false
}

class UseragentOptions internal constructor() {
	var deep: Boolean = false
}

class VinOptions internal constructor() {
	var deep: Boolean = false
}

class TariffOptions internal constructor() {
	/** Add units and the special and other schedule columns on paid plans. */
	var deep: Boolean = false
	/** ISO 3166-1 alpha-2 origin. With paid deep, resolves country-specific measures. Optional for schedule detail. */
	var origin: String? = null
}

class CurrencyRateOptions internal constructor() {
	var date: String? = null
	var amount: Double? = null
}

class TimeOptions internal constructor() {
	var at: String? = null
	var to: String? = null
	var deep: Boolean = false
}

class TimeAtOptions internal constructor() {
	var at: String? = null
	var to: String? = null
	var deep: Boolean = false
}

class TimezoneOptions internal constructor() {
	var at: String? = null
	var to: String? = null
	var deep: Boolean = false
}

class TimezoneAtOptions internal constructor() {
	var at: String? = null
	var deep: Boolean = false
}

class HolidayOptions internal constructor() {
	var year: Int? = null
}

class DateOptions internal constructor() {
	var format: String? = null
	var to: String? = null
	var deep: Boolean = false
}

class DateTodayOptions internal constructor() {
	var to: String? = null
	var deep: Boolean = false
}

class PointOptions internal constructor() {
	/** Add terrain and compact nearest-city context on every plan. The timezone ID stays in core. */
	var deep: Boolean = false
}

class WeatherOptions internal constructor() {
	/** Add specialist current measurements, forecasts and related detail on paid plans. */
	var deep: Boolean = false
	/** Past UTC day (YYYY-MM-DD). Requires paid deep and adds deep.history alongside current conditions. */
	var date: String? = null
}

class EmojiSearchOptions internal constructor() {
	var limit: Int? = null
	var deep: Boolean = false
}

class AddressOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

class AddressSearchOptions internal constructor() {
	var country: String? = null
	var postal: String? = null
	var city: String? = null
	var state: String? = null
	var ip: String? = null
}

class CompanyOptions internal constructor() {
	var country: String? = null
	var deep: Boolean = false
}

/** Options for measurement conversion. System accepts us or imperial. */
class MeasureOptions internal constructor() {
	var to: String? = null
	var locale: String? = null
	var system: String? = null
}

/** Filters for the reviewed unit catalog. Unit selects compatible targets. */
class MeasureUnitsOptions internal constructor() {
	var query: String? = null
	var type: String? = null
	var unit: String? = null
}

class NaicsSearchOptions internal constructor() {
	/** Maximum results, 1-50. Default 10. */
	var limit: Int? = null
	var deep: Boolean = false
}

class CountryOptions internal constructor() {
	var deep: Boolean = false
}

class CityIdOptions internal constructor() {
	var deep: Boolean = false
}

class CityNearestOptions internal constructor() {
	var deep: Boolean = false
}

class NaicsOptions internal constructor() {
	var deep: Boolean = false
}

class CurrencyOptions internal constructor() {
	var deep: Boolean = false
}

class LanguageOptions internal constructor() {
	var deep: Boolean = false
}

class EmojiOptions internal constructor() {
	var deep: Boolean = false
}
