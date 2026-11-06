package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.test.*
@OptIn(ExperimentalSerializationApi::class)
class CountryCorrectedTest {
 private val codec=Json{ignoreUnknownKeys=true;coerceInputValues=true;namingStrategy=JsonNamingStrategy.SnakeCase}
 @Test fun correctedCountryFieldsDecode(){
  val c=codec.decodeFromString<Country>(javaClass.getResource("/country-corrected.json")!!.readText())
  assertEquals("United States of America",c.nameFull);assertEquals("American",c.demonym)
  assertEquals(90026.52,c.gdpPerCapitaUsd);assertEquals(760.0,c.elevationAverage)
  val raw=codec.parseToJsonElement(codec.encodeToString(c)).jsonObject
  assertEquals(JsonPrimitive("United States of America"),raw["name_full"])
  assertFalse(raw.containsKey("deep"));assertTrue(raw.containsKey("population"));assertTrue(raw.containsKey("area"))
 }
 @Test fun canonicalNullAndZero(){
  val c=codec.decodeFromString<Country>("""
{"country":"XX","name":"Example","continent":"NA","name_full":null,"demonym":null,"capital":{"name":null,"latitude":0,"longitude":null},"tax":{"name":null,"rate":0},"population":0}
""")
  assertNull(c.nameFull);assertNull(c.demonym);assertEquals(0.0,c.capital?.latitude);assertNull(c.capital?.longitude);assertEquals(0.0,c.tax?.rate);assertEquals(0L,c.population)
 }
 @Test fun actualAntarcticaNullArray(){val c=codec.decodeFromString<Country>(javaClass.getResource("/country-aq.json")!!.readText());assertEquals(emptyList(),c.languages);assertEquals("Antarctica",c.nameFull);assertEquals(0L,c.population);assertNull(c.currency);assertNull(c.currencyName);assertNull(c.currencySymbol)}
 @Test fun nestedObjectsAndFlag(){
  val us=codec.decodeFromString<Country>(javaClass.getResource("/country-corrected.json")!!.readText());assertEquals("🇺🇸",us.flag);assertEquals("Washington, D.C.",us.capital?.name);assertEquals(38.89,us.capital?.latitude);assertEquals(-77.05,us.capital?.longitude);assertNotNull(us.tax);assertNull(us.tax?.name);assertNull(us.tax?.rate)
  val de=codec.decodeFromString<Country>(javaClass.getResource("/country-de.json")!!.readText());assertEquals("VAT",de.tax?.name);assertEquals(19.0,de.tax?.rate)
  val raw=codec.parseToJsonElement(codec.encodeToString(us)).jsonObject;assertEquals(setOf("name","latitude","longitude"),raw.getValue("capital").jsonObject.keys);assertEquals(setOf("name","rate"),raw.getValue("tax").jsonObject.keys)
 }
 @Test fun coreLeavesOptionalDetailUnknown(){val c=codec.decodeFromString<Country>("""
{"country":"US","name":"US","continent":"NA"}
""");assertNull(c.tax);assertNull(c.population)}
}
