package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFalse
@OptIn(ExperimentalSerializationApi::class)
class CountryCorrectedTest {
 private val codec=Json{ignoreUnknownKeys=true;coerceInputValues=true;namingStrategy=JsonNamingStrategy.SnakeCase}
 @Test fun correctedCountryFieldsDecode(){val c=codec.decodeFromString<Country>(javaClass.getResource("/country-corrected.json")!!.readText());assertEquals("United States of America",c.nameFull);assertEquals(c.nameFull,c.fullName);assertEquals("American",c.demonym);assertNull(c.deep?.demonym);assertNull(c.deep?.fullName);assertEquals(90026.52,c.deep?.gdpPerCapitaUsd);assertEquals(760.0,c.deep?.elevationAverage);val raw=codec.parseToJsonElement(codec.encodeToString(c)).jsonObject;assertEquals(JsonPrimitive("United States of America"),raw["name_full"]);assertFalse(raw.containsKey("full_name"));assertFalse(raw["deep"]!!.jsonObject.containsKey("demonym"));assertFalse(raw.containsKey("population"));assertFalse(raw.containsKey("area"))}
 @Test fun legacyNamesAndCanonicalNull(){for(key in listOf("name_full","full_name")){val c=codec.decodeFromString<Country>("""{"country":"US","name":"US","continent":"NA","languages":[],"$key":"Full"}""");assertEquals("Full",c.nameFull);assertEquals(c.nameFull,c.fullName)};val c=codec.decodeFromString<Country>("""{"country":"US","name":"US","continent":"NA","languages":[],"name_full":null,"full_name":"Legacy","deep":{"full_name":"Earlier","demonym":"Earlier"}}""");assertNull(c.nameFull);assertNull(c.fullName);assertEquals("Earlier",c.deep?.fullName);assertEquals("Earlier",c.deep?.demonym)}
 @Test fun actualAntarcticaNullArray(){val c=codec.decodeFromString<Country>(javaClass.getResource("/country-aq.json")!!.readText());assertEquals(emptyList(),c.languages);assertEquals("Antarctica",c.nameFull);assertEquals(0L,c.deep?.population);assertNull(c.currency);assertNull(c.currencyName);assertNull(c.currencySymbol)}
 @Test fun nestedObjectsAndFlag(){
  val us=codec.decodeFromString<Country>(javaClass.getResource("/country-corrected.json")!!.readText());assertEquals("🇺🇸",us.flag);assertEquals(us.flag,us.emoji);assertEquals("Washington, D.C.",us.capital?.name);assertEquals(38.89,us.capital?.lat);assertEquals(-77.05,us.capital?.lon);kotlin.test.assertNotNull(us.deep?.tax);assertNull(us.deep?.tax?.name);assertNull(us.deep?.tax?.rate)
  val de=codec.decodeFromString<Country>(javaClass.getResource("/country-de.json")!!.readText());assertEquals("VAT",de.deep?.tax?.name);assertEquals(19.0,de.deep?.tax?.rate);assertNull(de.deep?.taxIdFormat);assertNull(de.deep?.taxIdRegex)
  val raw=codec.parseToJsonElement(codec.encodeToString(us)).jsonObject;assertFalse(raw.containsKey("emoji"));assertFalse(raw.containsKey("capital_lat"));assertFalse(raw.containsKey("capital_lon"));assertEquals(setOf("name","lat","lon"),raw.getValue("capital").jsonObject.keys);val deep=raw.getValue("deep").jsonObject;assertFalse(deep.containsKey("tax_rate"));assertFalse(deep.containsKey("tax_id_format"));assertFalse(deep.containsKey("tax_id_regex"));assertEquals(setOf("name","rate"),deep.getValue("tax").jsonObject.keys)
 }
 @Test fun legacyGroupsPreserveZeroAndCanonicalNull(){
  val legacy=codec.decodeFromString<Country>("""{"country":"XX","name":"Example","continent":"NA","emoji":"old","capital":null,"capital_lat":0,"capital_lon":0,"deep":{"tax":null,"tax_rate":0,"tax_id_format":"9","tax_id_regex":"^9$","population":0,"area":0}}""");assertEquals("old",legacy.flag);assertNull(legacy.capital?.name);assertEquals(0.0,legacy.capital?.lat);assertNull(legacy.deep?.tax?.name);assertEquals(0.0,legacy.deep?.tax?.rate);assertEquals("9",legacy.deep?.taxIdFormat)
  val current=codec.decodeFromString<Country>("""{"country":"XX","name":"Example","continent":"NA","flag":null,"emoji":"old","capital":{"name":null,"lat":null,"lon":0},"capital_lat":9,"capital_lon":9,"deep":{"tax":{"name":null,"rate":null},"tax_rate":99,"tax_id_format":"stale"}}""");assertNull(current.flag);assertNull(current.capital?.name);assertNull(current.capital?.lat);assertEquals(0.0,current.capital?.lon);assertNull(current.deep?.tax?.rate);assertNull(current.deep?.taxIdFormat)
 }
 @Test fun compactAndLocked(){
  for(key in listOf("flag","emoji")){val item=codec.decodeFromString<ContinentCountryItem>("""{"country":"US","name":"US","$key":"🇺🇸"}""");assertEquals("🇺🇸",item.flag);assertEquals(item.flag,item.emoji)}
  val item=codec.decodeFromString<ContinentCountryItem>("""{"country":"US","name":"US","flag":null,"emoji":"old"}""");assertNull(item.flag)
  val core=codec.decodeFromString<Country>("""{"country":"US","name":"US","continent":"NA"}""");assertNull(core.deep);val locked=codec.decodeFromString<Country>("""{"country":"US","name":"US","continent":"NA","deep":{}}""");assertNull(locked.deep?.tax)
 }

}
