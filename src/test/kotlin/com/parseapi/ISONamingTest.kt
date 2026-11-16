package com.parseapi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class ISONamingTest {
 private val json=Json{ignoreUnknownKeys=true;namingStrategy=JsonNamingStrategy.SnakeCase}
 @Test fun releasedAndCanonicalNamesPreserveNativeMembers() {
  for(fields in listOf("\"iso3\":\"USA\",\"numeric\":840","\"iso_alpha3\":\"USA\",\"iso_numeric\":840")) {
   val value=json.decodeFromString<Country>("{\"country\":\"US\",\"name\":\"United States\",\"continent\":\"NA\",$fields}")
   assertEquals("USA",value.iso3);assertEquals(840,value.numeric)
   val encoded=json.parseToJsonElement(json.encodeToString(value)).jsonObject
   assertTrue("iso_alpha3" in encoded && "iso_numeric" in encoded)
   assertTrue("iso3" !in encoded && "numeric" !in encoded)
   val deep=json.decodeFromString<CountryDeep>("{$fields}");assertEquals("USA",deep.iso3);assertEquals(840,deep.numeric)
  }
  for(fields in listOf("\"iso3\":\"hrv\"","\"iso_alpha3\":\"hrv\"")) {
   val value=json.decodeFromString<Language>("{\"language\":\"hr\",\"name\":\"Croatian\",\"direction\":\"ltr\",$fields}");assertEquals("hrv",value.iso3)
  }
  for(fields in listOf("\"iso\":\"US-CA\"","\"iso_3166_2\":\"US-CA\"","\"iso_subdivision\":\"US-CA\"")) {
   val value=json.decodeFromString<State>("{\"state\":\"CA\",\"name\":\"California\",\"country\":\"US\",$fields}");assertEquals("US-CA",value.iso)
  }
  for(fields in listOf("\"numeric\":0","\"iso_numeric\":0")) {
   val value=json.decodeFromString<Currency>("{\"currency\":\"USD\",\"name\":\"US Dollar\",$fields}");assertEquals(0,value.isoNumeric)
  }
 }
 @Test fun releasedDeepMembersRemainReadable() {
  val language=json.decodeFromString<LanguageDeep>("""
{"iso3":"eng","countries":["US"]}
""")
  assertEquals("eng",language.iso3);assertEquals(listOf("US"),language.countries)
  val state=json.decodeFromString<StateDeep>("""
{"fips":"06","capital":"Sacramento","area_codes":["916"],"tax":"Sales tax","tax_rate":0}
""")
  assertEquals("06",state.fips);assertEquals("Sacramento",state.capital);assertEquals(listOf("916"),state.areaCodes)
  assertEquals("Sales tax",state.tax?.name);assertEquals(0.0,state.taxRate)
  val raw=json.parseToJsonElement(json.encodeToString(state)).jsonObject
  assertTrue("fips" !in raw && "capital" !in raw && "area_codes" !in raw && "tax_rate" !in raw)
 }
 @Test fun canonicalNullWinsInEitherKeyOrder() {
  for(fields in listOf("\"iso_alpha3\":null,\"iso_numeric\":null,\"iso3\":\"USA\",\"numeric\":840","\"iso3\":\"USA\",\"numeric\":840,\"iso_alpha3\":null,\"iso_numeric\":null")) {
   val value=json.decodeFromString<Country>("{\"country\":\"US\",\"name\":\"United States\",\"continent\":\"NA\",$fields}");assertNull(value.iso3);assertNull(value.numeric)
   val deep=json.decodeFromString<CountryDeep>("{$fields}");assertNull(deep.iso3);assertNull(deep.numeric)
  }
  val language=json.decodeFromString<Language>("""
{"language":"hr","name":"Croatian","direction":"ltr","iso_alpha3":null,"iso3":"hrv"}
""");assertNull(language.iso3)
  val state=json.decodeFromString<State>("""
{"state":"CA","name":"California","country":"US","iso_subdivision":null,"iso":"US-CA","iso_3166_2":"US-TX"}
""");assertNull(state.iso)
 }
 @Test fun stateCountyISOCodeKeepsCanonicalNullAndExactPlace() {
  for(key in listOf("iso_code","iso_subdivision","iso","iso_3166_2")) {
   val value=json.decodeFromString<State>("{\"state\":\"CO\",\"name\":\"Colorado\",\"country\":\"US\",\"$key\":\"US-CO\"}")
   assertEquals("US-CO",value.isoCode);assertEquals(value.isoCode,value.iso);assertEquals(value.isoCode,value.iso31662)
   val raw=json.parseToJsonElement(json.encodeToString(value)).jsonObject
   assertTrue("iso_code" in raw && "iso_subdivision" !in raw && "iso" !in raw && "iso_3166_2" !in raw)
  }
  for(fields in listOf("\"iso_code\":null,\"iso_subdivision\":\"US-CA\",\"iso\":\"US-TX\",\"iso_3166_2\":\"US-NY\"","\"iso_subdivision\":\"US-CA\",\"iso\":\"US-TX\",\"iso_3166_2\":\"US-NY\",\"iso_code\":null")) {
   val value=json.decodeFromString<State>("{\"state\":\"CO\",\"name\":\"Colorado\",\"country\":\"US\",$fields}")
   assertNull(value.isoCode);assertNull(value.iso);assertNull(value.iso31662)
  }
  val feature=json.parseToJsonElement("""{"type":"Feature","properties":{"county":"E09000033","iso_code":"GB-WSM","name":"Westminster","country":"GB","type":"district"},"geometry":null}""").jsonObject
  val county=json.decodeFromString<County>(feature.getValue("properties").toString())
  assertEquals("GB-WSM",county.isoCode);assertEquals("E09000033",county.county);assertEquals("district",county.type)
  val list=json.decodeFromString<StateCounties>("""{"state":"ENG","country":"GB","counties":[{"county":"29","name":"Finistere","iso_code":"FR-29"},{"county":"08031","name":"Denver County","iso_code":null},{"county":"old","name":"Old response"}]}""")
  assertEquals("FR-29",list.counties[0].isoCode);assertNull(list.counties[1].isoCode);assertNull(list.counties[2].isoCode)
 }
}
