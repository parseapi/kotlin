package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*

@OptIn(ExperimentalSerializationApi::class)
class CountyTest {
 private val codec=Json {ignoreUnknownKeys=true;namingStrategy=JsonNamingStrategy.SnakeCase}
 @Test fun canonicalRoutesAndHistoricalReaders()=runBlocking {
  val stub=StubTransport(200,"""{"county":"08031","name":"Denver County","country":"US","type":"county","population":0}""")
  val county=ParseAPI("test"){transport=stub}.county("Denver County"){country="US";state="CO";deep=true}
  assertEquals("08031",county.county);assertEquals("08031",county.district);assertEquals(0L,county.population)
  assertEquals(1,stub.requests.size);assertTrue(stub.requests.single().url.endsWith("/county/Denver%20County?country=US&state=CO&deep=true"))
  assertEquals("2.2.0",stub.requests.single().headers["X-API-Version"])
  val listing=StubTransport(200,"""{"state":"CO","country":"US","counties":[{"county":"08031","name":"Denver County","population":0}]}""")
  val result=ParseAPI("test"){transport=listing}.stateCounties("CO"){country="US"}
  assertEquals("08031",result.counties.single().county);assertEquals(1,result.districts.size)
  assertTrue(listing.requests.single().url.endsWith("/state/CO/counties?country=US"))
  val old=codec.decodeFromString<County>("""{"district":"75","name":"Paris","country":"FR","type":"department"}""")
  assertEquals("75",old.county);assertEquals("department",old.type)
  val postal=codec.decodeFromString<Postal>("""{"postal":"08001","country":"US","district":"08031","district_name":"Denver","district_name_local":null}""")
  assertEquals("08031",postal.county);assertEquals("Denver",postal.districtName);assertNull(postal.countyNameLocal)
  val fields=codec.encodeToJsonElement(postal).jsonObject
  assertNull(fields["district"]);assertEquals(JsonPrimitive("08031"),fields["county"])
  val city=codec.decodeFromString<City>("""{"id":"city_testparis001","name":"Paris","country":"FR","county":"75","type":"district","capital_of":"county"}""")
  assertEquals("district",city.type);assertEquals("county",city.capitalOf)
 }
}
