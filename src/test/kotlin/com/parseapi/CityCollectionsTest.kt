package com.parseapi

import java.net.URI
import java.net.URLDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class CityCollectionsTest {
	private fun client(stub:StubTransport) = ParseAPI("test") { transport=stub; retries=0 }
	private fun query(stub:StubTransport):Map<String,String> = URI(stub.requests.last().url).rawQuery?.split('&')?.associate {
		val pair=it.split('=',limit=2); URLDecoder.decode(pair[0],"UTF-8") to URLDecoder.decode(pair[1],"UTF-8")
	} ?: emptyMap()

	@Test fun collectionControlsAndOpaqueCursorReachAllRoutes() = runBlocking {
		val filters=mapOf("country" to "AT","state" to "9","type" to "city","grain" to "municipality","hierarchy" to "true","min_population" to "0","max_population" to "9007199254740991","timezone" to "Europe/Vienna","bbox" to "170,-10,-170,10","sort" to "population","order" to "desc","cursor" to "opaque+/=?&","deep" to "true","lang" to "de","limit" to "5")
		val list=StubTransport(200,"""{"object":"list","data":[],"next_cursor":"opaque+/=?&"}""")
		val listed=client(list).cityList { country="AT";state="9";type="city";grain="municipality";hierarchy=true;minPopulation=0;maxPopulation=9_007_199_254_740_991;timezone="Europe/Vienna";bbox="170,-10,-170,10";sort="population";order="desc";cursor="opaque+/=?&";deep=true;lang="de";limit=5 }
		assertEquals("opaque+/=?&",listed.nextCursor);assertEquals(filters + mapOf("browse" to "true","format" to "list"),query(list))
		val search=StubTransport(200,"""{"q":"Fi","cities":[],"next_cursor":null}""")
		assertNull(client(search).citySearch("Fi") { country="AT";state="9";type="city";grain="municipality";hierarchy=true;aliases=true;minPopulation=0;maxPopulation=9_007_199_254_740_991;timezone="Europe/Vienna";bbox="170,-10,-170,10";sort="population";order="desc";page=true;cursor="opaque+/=?&";deep=true;lang="de";limit=5 }.nextCursor)
		assertEquals(filters + mapOf("q" to "Fi","aliases" to "true","page" to "true"),query(search))
		val named=StubTransport(200,"""{"city":"Fixture","country":"AT","radius":5,"unit":"mi","nearby":[],"next_cursor":null}""")
		client(named).cityNearby("Fixture") { country="AT";state="9";type="city";grain="municipality";hierarchy=true;aliases=true;ambiguity="error";radius=5.0;unit="mi";minPopulation=0;maxPopulation=9_007_199_254_740_991;timezone="Europe/Vienna";bbox="170,-10,-170,10";sort="population";order="desc";page=true;cursor="opaque+/=?&";deep=true;lang="de";limit=5 }
		assertEquals("/city/Fixture/nearby",URI(named.requests[0].url).path)
		assertEquals(filters + mapOf("radius" to "5","unit" to "mi","aliases" to "true","ambiguity" to "error","page" to "true"),query(named))
		val coords=StubTransport(200,"""{"object":"list","data":[],"latitude":0,"longitude":0,"radius":5,"unit":"mi","next_cursor":"next"}""")
		val result=client(coords).cityNearbyCoordinates(0.0,0.0) { country="AT";state="9";type="city";grain="municipality";hierarchy=true;radius=5.0;unit="mi";minPopulation=0;maxPopulation=9_007_199_254_740_991;timezone="Europe/Vienna";bbox="170,-10,-170,10";sort="population";order="desc";cursor="opaque+/=?&";deep=true;lang="de";limit=5 }
		assertEquals(0.0,result.latitude);assertEquals(0.0,result.longitude);assertEquals("next",result.nextCursor)
		assertEquals(filters + mapOf("lat" to "0","lon" to "0","nearby" to "true","format" to "list","radius" to "5","unit" to "mi"),query(coords))
	}

	@Test fun oldCallsAndFalsePagingPreserveDefaultUrls() = runBlocking {
		val search=StubTransport(200,"""{"q":"Fi","cities":[]}""")
		assertNull(client(search).citySearch("Fi").nextCursor)
		client(search).citySearch("Fi") { page=false }
		assertEquals(mapOf("q" to "Fi"),query(search))
		val nearby=StubTransport(200,"""{"city":"Fixture","country":"AT","radius":40,"unit":"km","nearby":[]}""")
		assertNull(client(nearby).cityNearby("Fixture").nextCursor);assertEquals(emptyMap(),query(nearby))
		val list=StubTransport(200,"""{"object":"list","data":[],"next_cursor":null}""")
		client(list).cityList();assertEquals(mapOf("browse" to "true","format" to "list"),query(list))
		val coords=StubTransport(200,"""{"object":"list","data":[],"latitude":0,"longitude":0,"radius":40,"unit":"km","next_cursor":null}""")
		client(coords).cityNearbyCoordinates(0.0,0.0);assertEquals(mapOf("lat" to "0","lon" to "0","nearby" to "true","format" to "list"),query(coords))
	}

	@Test fun nullCursorUnknownVocabularyAndZeroPopulationStayReadable() = runBlocking {
		val core="""{"id":"city_222222222222","object":"city","name":"Fixture","country":"AT","distance":0,"distance_mi":0,"grain":"future-grain","deep":{"population":0}}"""
		val listed=client(StubTransport(200,"""{"object":"list","data":[$core],"next_cursor":null,"future":true}""")).cityList()
		assertNull(listed.nextCursor);assertEquals("future-grain",listed.data.single().grain);assertEquals(0L,listed.data.single().deep?.population)
		val coords=client(StubTransport(200,"""{"latitude":-90,"longitude":180,"radius":40,"unit":"future-unit","object":"list","data":[$core],"next_cursor":"opaque"}""")).cityNearbyCoordinates(-90.0,180.0)
		assertEquals("future-unit",coords.unit);assertEquals("future-grain",coords.data.single().grain);assertEquals("opaque",coords.nextCursor)
	}
}
