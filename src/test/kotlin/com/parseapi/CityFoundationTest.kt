package com.parseapi

import java.net.URI
import java.net.URLDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CityFoundationTest {
	private val core = """{"id":"city_222222222222","name":"Fixture","country":"JP","distance":0,"distance_mi":0}"""
	private val rich = """{"id":"city_222222222222","name":"Fixture","country":"JP","distance":0,"distance_mi":0,"grain":"subdivision","parent":{"id":"city_333333333333","name":null,"name_local":"市","country":"JP","state":"14","type":"city","grain":"municipality","future":true},"deep":{"population":0,"population_observed_on":"2025-12-31","population_basis":"registered_population"},"future":true}"""
	private fun client(stub:StubTransport) = ParseAPI("test") { transport=stub; retries=0 }
	private fun query(url:String):Map<String,String> = URI(url).rawQuery?.split('&')?.associate {
		val pair=it.split('=',limit=2); URLDecoder.decode(pair[0],"UTF-8") to URLDecoder.decode(pair[1],"UTF-8")
	} ?: emptyMap()

	@Test fun oldAndNewBodiesRetainNullableParentAndDatedPopulation() = runBlocking {
		val old=client(StubTransport(200,core)).city("Fixture")
		assertNull(old.grain); assertNull(old.parent)
		val city=client(StubTransport(200,rich)).city("Fixture")
		assertEquals("subdivision",city.grain)
		assertEquals("city_333333333333",city.parent?.id)
		assertNull(city.parent?.name)
		assertEquals("市",city.parent?.nameLocal)
		assertEquals(0L,city.deep?.population)
		assertEquals("2025-12-31",city.deep?.populationObservedOn)
		assertEquals("registered_population",city.deep?.populationBasis)
		val nearest=client(StubTransport(200,rich)).cityNearest(0.0,0.0)
		assertEquals("municipality",nearest.parent?.grain)
		val explicitNull=rich.replace("\"grain\":\"subdivision\"","\"grain\":null")
		assertNull(client(StubTransport(200,explicitNull)).city("Fixture").grain)
	}

	@Test fun routeOptionsRemainOptionalAndEncodeOnlyRequestedControls() = runBlocking {
		val city=StubTransport(200,rich)
		client(city).city("Fixture") { country="JP";state="14";type="ward";grain="subdivision";aliases=true;hierarchy=true;ambiguity="error";deep=true;lang="ja" }
		val exact=mapOf("country" to "JP","state" to "14","type" to "ward","grain" to "subdivision","aliases" to "true","hierarchy" to "true","ambiguity" to "error","deep" to "true","lang" to "ja")
		assertEquals(exact,query(city.requests[0].url))
		val search=StubTransport(200,"""{"q":"Fi","cities":[],"type":"ward","grain":"subdivision","aliases":true}""")
		val result=client(search).citySearch("Fi") { country="JP";state="14";type="ward";grain="subdivision";aliases=true;hierarchy=true;deep=true;lang="ja";limit=5 }
		assertEquals(exact - "ambiguity" + mapOf("q" to "Fi","limit" to "5"),query(search.requests[0].url))
		assertEquals(true,result.aliases)
		val nearest=StubTransport(200,rich)
		client(nearest).cityNearest(0.0,0.0) { country="JP";state="14";type="ward";grain="subdivision";hierarchy=true;deep=true;lang="ja" }
		assertEquals(exact - setOf("ambiguity","aliases") + mapOf("lat" to "0","lon" to "0"),query(nearest.requests[0].url))
		val id=StubTransport(200,rich)
		client(id).cityId("city_222222222222") { hierarchy=true;deep=true;lang="ja" }
		assertEquals(mapOf("hierarchy" to "true","deep" to "true","lang" to "ja"),query(id.requests[0].url))
		val nearby=StubTransport(200,"""{"city":"Fixture","country":"JP","radius":10,"unit":"km","nearby":[]}""")
		client(nearby).cityNearby("Fixture") { country="JP";state="14";type="ward";grain="subdivision";aliases=true;hierarchy=true;ambiguity="error";deep=true;lang="ja";radius=10.0;unit="km";limit=5 }
		assertEquals(exact + mapOf("radius" to "10","unit" to "km","limit" to "5"),query(nearby.requests[0].url))
		val basic=StubTransport(200,core)
		client(basic).city("Fixture") {}
		client(basic).cityNearest(0.0,0.0) {}
		assertEquals(emptyMap(),query(basic.requests[0].url))
		assertEquals(mapOf("lat" to "0","lon" to "0"),query(basic.requests[1].url))
	}
}
