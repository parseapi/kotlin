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
}

class StateDistrictsOptions internal constructor() {
	var country: String? = null
}

class DistrictOptions internal constructor() {
	var country: String? = null
	var state: String? = null
}

class CityOptions internal constructor() {
	var country: String? = null
	var state: String? = null
}

class CitySearchOptions internal constructor() {
	var country: String? = null
	var state: String? = null
	var limit: Int? = null
}

class CityNearbyOptions internal constructor() {
	var radius: Double? = null
	var unit: String? = null
	var country: String? = null
	var state: String? = null
	var limit: Int? = null
}

class PostalOptions internal constructor() {
	var country: String? = null
}

class PostalNearbyOptions internal constructor() {
	var country: String? = null
	var radius: Double? = null
	var unit: String? = null
}

class PostalDistanceOptions internal constructor() {
	var country: String? = null
}

class EmailOptions internal constructor() {
	var deep: Boolean = false
}

class VatOptions internal constructor() {
	var country: String? = null
	var from: String? = null
	var deep: Boolean = false
}

class IbanOptions internal constructor() {
	var country: String? = null
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
}

class CallerOptions internal constructor() {
	var country: String? = null
}

class HlrOptions internal constructor() {
	var country: String? = null
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
	var deep: Boolean = false
	var origin: String? = null
}

class CurrencyRateOptions internal constructor() {
	var date: String? = null
	var amount: Double? = null
}

class TimezoneOptions internal constructor() {
	var at: String? = null
	var to: String? = null
}

class TimezoneAtOptions internal constructor() {
	var at: String? = null
}

class HolidayOptions internal constructor() {
	var year: Int? = null
}

class DateOptions internal constructor() {
	var format: String? = null
	var to: String? = null
}

class DateTodayOptions internal constructor() {
	var to: String? = null
}

class PointOptions internal constructor() {
	var deep: Boolean = false
}

class WeatherOptions internal constructor() {
	var deep: Boolean = false
	var date: String? = null
}

class EmojiSearchOptions internal constructor() {
	var limit: Int? = null
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
