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
 }
 @Test fun releasedDeepMembersRemainReadable() {
  val language=json.decodeFromString<LanguageDeep>("""{"iso3":"eng","countries":["US"]}""")
  assertEquals("eng",language.iso3);assertEquals(listOf("US"),language.countries)
  val state=json.decodeFromString<StateDeep>("""{"fips":"06","capital":"Sacramento","area_codes":["916"],"tax":"Sales tax","tax_rate":0}""")
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
  val language=json.decodeFromString<Language>("""{"language":"hr","name":"Croatian","direction":"ltr","iso_alpha3":null,"iso3":"hrv"}""");assertNull(language.iso3)
  val state=json.decodeFromString<State>("""{"state":"CA","name":"California","country":"US","iso_subdivision":null,"iso":"US-CA","iso_3166_2":"US-TX"}""");assertNull(state.iso)
 }
}
