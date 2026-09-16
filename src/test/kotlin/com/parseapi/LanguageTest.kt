package com.parseapi

import java.net.URI
import java.net.URLDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class LanguageTest {
 private data class Operation(
  val name: String,
  val path: String,
  val query: String,
  val call: suspend (ParseAPI, String?) -> Any,
 )

 private fun queryPairs(raw: String?): List<Pair<String, String>> =
  raw?.takeIf { it.isNotEmpty() }?.split('&')?.map {
   val pair = it.split('=', limit = 2)
   URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair.getOrElse(1) { "" }, "UTF-8")
  } ?: emptyList()

 @Test
 fun languageIsPerRequestForEverySupportedOperation() = runBlocking {
  val operations = listOf(
   Operation("IP", "/ip/8.8.8.8", "deep=true") { parse, language -> parse.ip("8.8.8.8") { this.deep = true; this.lang = language } },
   Operation("IPSelf", "/ip", "deep=true") { parse, language -> parse.ipSelf { this.deep = true; this.lang = language } },
   Operation("Continent", "/continent/EU", "") { parse, language -> parse.continent("EU") { this.lang = language } },
   Operation("ContinentCountries", "/continent/EU/countries", "") { parse, language -> parse.continentCountries("EU") { this.lang = language } },
   Operation("BlocCountries", "/bloc/EU/countries", "") { parse, language -> parse.blocCountries("EU") { this.lang = language } },
   Operation("Country", "/country/DE", "deep=true") { parse, language -> parse.country("DE") { this.deep = true; this.lang = language } },
   Operation("CountryStates", "/country/DE/states", "") { parse, language -> parse.countryStates("DE") { this.lang = language } },
   Operation("State", "/state/CA", "country=US") { parse, language -> parse.state("CA") { this.country = "US"; this.lang = language } },
   Operation("StateDistricts", "/state/CA/districts", "country=US&deep=true") { parse, language -> parse.stateDistricts("CA") { this.country = "US"; this.deep = true; this.lang = language } },
   Operation("District", "/district/37081", "country=US&state=NC") { parse, language -> parse.district("37081") { this.country = "US"; this.state = "NC"; this.lang = language } },
   Operation("City", "/city/M%C3%BCnchen", "country=DE") { parse, language -> parse.city("München") { this.country = "DE"; this.lang = language } },
   Operation("CityID", "/city/id/city_fixture", "deep=true") { parse, language -> parse.cityId("city_fixture") { this.deep = true; this.lang = language } },
   Operation("CitySearch", "/city", "limit=2&q=M%C3%BCn") { parse, language -> parse.citySearch("Mün") { this.limit = 2; this.lang = language } },
   Operation("CityNearest", "/city", "lat=0&lon=0") { parse, language -> parse.cityNearest(0.0, 0.0) { this.lang = language } },
   Operation("CityNearby", "/city/M%C3%BCnchen/nearby", "radius=8&unit=km") { parse, language -> parse.cityNearby("München") { this.radius = 8.0; this.unit = "km"; this.lang = language } },
   Operation("Postal", "/postal/SW1A%201AA", "country=GB") { parse, language -> parse.postal("SW1A 1AA") { this.country = "GB"; this.lang = language } },
   Operation("PostalNearby", "/postal/28202/nearby", "country=US&radius=8") { parse, language -> parse.postalNearby("28202") { this.country = "US"; this.radius = 8.0; this.lang = language } },
   Operation("PostalDistance", "/postal/28202/distance/10001", "country=US") { parse, language -> parse.postalDistance("28202", "10001") { this.country = "US"; this.lang = language } },
   Operation("Company", "/company/732829320", "country=FR&deep=true") { parse, language -> parse.company("732829320") { this.country = "FR"; this.deep = true; this.lang = language } },
   Operation("NPI", "/npi/1881018208", "deep=true") { parse, language -> parse.npi("1881018208") { this.deep = true; this.lang = language } },
   Operation("ASN", "/asn/AS13335", "") { parse, language -> parse.asn("AS13335") { this.lang = language } },
   Operation("Currency", "/currency/USD", "deep=true") { parse, language -> parse.currency("USD") { this.deep = true; this.lang = language } },
   Operation("Language", "/language/ja", "") { parse, language -> parse.language("ja") { this.lang = language } },
   Operation("Time", "/time/America%2FNew_York", "at=2026-01-01T12%3A00&deep=true&to=UTC") { parse, language -> parse.time("America/New_York") { this.at = "2026-01-01T12:00"; this.to = "UTC"; this.deep = true; this.lang = language } },
   Operation("TimeAt", "/time", "at=2026-01-01T12%3A00Z&lat=0&lon=0") { parse, language -> parse.timeAt(0.0, 0.0) { this.at = "2026-01-01T12:00Z"; this.lang = language } },
   Operation("Timezone", "/timezone/UTC", "deep=true") { parse, language -> parse.timezone("UTC") { this.deep = true; this.lang = language } },
   Operation("TimezoneAt", "/timezone", "deep=true&lat=0&lon=0") { parse, language -> parse.timezoneAt(0.0, 0.0) { this.deep = true; this.lang = language } },
   Operation("Date", "/date/03%2F04%2F2026", "deep=true&format=dmy&to=2026-05-01") { parse, language -> parse.date("03/04/2026") { this.format = "dmy"; this.to = "2026-05-01"; this.deep = true; this.lang = language } },
   Operation("DateToday", "/date", "to=2026-05-01") { parse, language -> parse.dateToday { this.to = "2026-05-01"; this.lang = language } },
   Operation("Point", "/point", "deep=true&lat=0&lon=0") { parse, language -> parse.point(0.0, 0.0) { this.deep = true; this.lang = language } },
   Operation("Emoji", "/emoji/%F0%9F%98%80", "deep=true") { parse, language -> parse.emoji("😀") { this.deep = true; this.lang = language } },
   Operation("EmojiSearch", "/emoji", "limit=2&q=visage") { parse, language -> parse.emojiSearch("visage") { this.limit = 2; this.lang = language } },
   Operation("MeasureUnits", "/measure/units", "q=meter&unit=m") { parse, language -> parse.measureUnits { this.query = "meter"; this.unit = "m"; this.lang = language } },
  )
  assertEquals(33, operations.size)
  for (operation in operations) {
   // An API error avoids conflating this URL test with each product decoder.
   val stub = StubTransport(400, """{"code":"fixture_error","message":"Fixture"}""")
   val parse = ParseAPI("test") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }
   for (language in listOf("fr-CA", null)) {
    val failure = assertFailsWith<ParseAPIException>(operation.name) { operation.call(parse, language) }
    assertEquals(400, failure.status, operation.name)
    assertEquals("fixture_error", failure.code, operation.name)
    val uri = URI(stub.requests.last().url)
    val pairs = queryPairs(uri.rawQuery)
    val expected = queryPairs(operation.query).toMap().toMutableMap()
    if (language != null) expected["lang"] = language
    assertEquals(operation.path, uri.rawPath, operation.name)
    assertEquals(expected.size, pairs.size, "${operation.name}: duplicate or leaked parameter")
    assertEquals(expected, pairs.toMap(), operation.name)
   }
   assertEquals(2, stub.requests.size, operation.name)
  }
 }

 @Test
 fun languagePreservesNativeNullAndInputControls() = runBlocking {
  val stub = StubTransport(mutableListOf(
   ParseAPIResponse(200, """{"country":"DE","name":"Allemagne","continent":"EU","name_local":"Deutschland","currency_name":null,"future":true}""", emptyMap()),
   ParseAPIResponse(200, """{"date":"2026-04-03","valid":true}""", emptyMap()),
   ParseAPIResponse(200, """{"measure":"1,5 m","valid":true,"amount":"150","unit":"cm"}""", emptyMap()),
  ))
  val parse = ParseAPI("test") { transport = stub; retries = 0; baseUrl = "https://api.parseapi.com" }
  val country = parse.country("DE") { lang = "fr" }
  assertEquals("DE", country.country)
  assertEquals("Allemagne", country.name)
  assertEquals("Deutschland", country.nameLocal)
  assertNull(country.currencyName)
  assertNull(country.deep)
  val date = parse.date("03/04/2026") { format = "dmy"; lang = "en-US" }
  assertEquals("2026-04-03", date.date)
  assertEquals("https://api.parseapi.com/date/03%2F04%2F2026?format=dmy&lang=en-US", stub.requests[1].url)
  val measure = parse.measure("1,5 m") { locale = "de-DE"; to = "cm" }
  assertEquals("150", measure.amount)
  assertEquals("https://api.parseapi.com/measure/1%2C5%20m?to=cm&locale=de-DE", stub.requests[2].url)
  for (type in listOf(CurrencyRateOptions::class.java, MeasureOptions::class.java, HolidayOptions::class.java,
   NameOptions::class.java, EmailOptions::class.java, PhoneOptions::class.java, AddressOptions::class.java)) {
   assertTrue(type.methods.none { it.name in listOf("getLang", "setLang") }, type.simpleName)
  }
 }
}
