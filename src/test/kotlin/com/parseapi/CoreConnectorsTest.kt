package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
@OptIn(ExperimentalSerializationApi::class)
class CoreConnectorsTest {
 private val codec=Json{ignoreUnknownKeys=true;namingStrategy=JsonNamingStrategy.SnakeCase}
 private val fixtures=codec.parseToJsonElement(javaClass.getResource("/core-connectors.json")!!.readText()).jsonObject
 @Test fun coreReferencesPreserveDeepTypes(){
  val c=codec.decodeFromJsonElement<Country>(fixtures.getValue("country"));assertEquals("USA",c.iso3);assertEquals(0.0,c.capitalLat);assertEquals(listOf("CA","MX"),c.borders)
  val l=codec.decodeFromJsonElement<Language>(fixtures.getValue("language"));assertEquals("eng",l.iso3);assertEquals(listOf("GB","US"),l.countries)
  val u=codec.decodeFromJsonElement<Currency>(fixtures.getValue("currency"));assertEquals(listOf("US"),u.countries)
  val p=codec.decodeFromJsonElement<Postal>(fixtures.getValue("postal"));assertEquals("USD",p?.currency);assertEquals("00501",p.postal)
  val point=codec.decodeFromJsonElement<Point>(fixtures.getValue("point"));assertEquals("city_7aj899nvwyku",point?.city?.id)
  assertEquals("US",p.country);assertEquals(listOf("00544"),p?.neighbors);assertNotNull(p.currency)
  val n=codec.decodeFromJsonElement<Industry>(fixtures.getValue("industry"));assertEquals("111",n?.children?.first()?.naics)
  val search=codec.decodeFromJsonElement<NAICSSearch>(fixtures.getValue("industry_search"));assertEquals("722515",search.results.first().naics);assertEquals("Coffee shops, on premise brewing",search.results.first().match?.text)
  val e=codec.decodeFromJsonElement<Emoji>(fixtures.getValue("emoji"));assertEquals("👍",e?.base);assertEquals("👍🏽",e?.variants?.first()?.emoji)
  val co=codec.decodeFromJsonElement<CompanyProfile>(fixtures.getValue("company"));assertEquals("0000320193",co?.identifiers?.first()?.value);assertEquals("US",co?.phoneNumbers?.first()?.country);assertEquals("+14089961010",co?.phoneNumbers?.first()?.e164)
 }
 @Test fun olderUnknownAndEmptyStayCompatible(){
  val old=JsonObject(fixtures.getValue("country").jsonObject.filterKeys{it !in setOf("iso_alpha3","borders","capital")})
  val c=codec.decodeFromJsonElement<Country>(old);assertNull(c.iso3);assertNull(c.borders)
  val point=JsonObject(fixtures.getValue("point").jsonObject+("city_id" to JsonPrimitive("unpublished-alias")));assertEquals("city_7aj899nvwyku",codec.decodeFromJsonElement<Point>(point)?.city?.id)
  val postal=fixtures.getValue("postal").jsonObject;assertNull(codec.decodeFromJsonElement<PostalDeep>(JsonObject(postal.filterKeys{it!="neighbors"})).neighbors)
  assertNull(codec.decodeFromJsonElement<PostalDeep>(JsonObject(postal+("neighbors" to JsonNull))).neighbors)
  assertEquals(emptyList(),codec.decodeFromJsonElement<PostalDeep>(JsonObject(postal+("neighbors" to JsonArray(emptyList())))).neighbors)
  val lang=fixtures.getValue("language").jsonObject
  assertNull(codec.decodeFromJsonElement<Language>(JsonObject(lang+ ("countries" to JsonNull))).countries)
  assertEquals(emptyList(),codec.decodeFromJsonElement<Language>(JsonObject(lang+("countries" to JsonArray(emptyList())))).countries)
 }
}
