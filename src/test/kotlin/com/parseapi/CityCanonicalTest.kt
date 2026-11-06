package com.parseapi

import java.net.URI
import java.net.URLDecoder
import kotlin.test.*
import kotlinx.coroutines.runBlocking

class CityCanonicalTest {
    private val id = "city_7aj899nvwyku"
    private fun query(stub: StubTransport) = URI(stub.requests.last().url).rawQuery?.split('&')?.associate { val pair=it.split('=',limit=2); URLDecoder.decode(pair[0],"UTF-8") to URLDecoder.decode(pair[1],"UTF-8") } ?: emptyMap()
    private fun client(stub: StubTransport) = ParseAPI("fixture") { transport=stub; retries=0 }

    @Test fun canonicalFiltersAndTypedResources() = runBlocking {
        val stub=StubTransport(200,"""{"object":"list","has_more":true,"url":"/city","next_cursor":"opaque+/=","cities":[{"id":"$id","object":"city","name":"Vienna","country":"AT"}]}""")
        val result=client(stub).cityList { countries=listOf("AT","CH");excludeCountries=listOf("JP");timezones=listOf("Europe/Vienna");minElevation=-10.0;maxElevation=0.0;minPopulation=0;sort="-population,+name";cursor="opaque+/=" }
        assertEquals("Vienna",result.cities.single().name);assertEquals("AT",result.cities.single().country);assertTrue(result.nextCursor != null)
        for((key,value) in mapOf("countries" to "AT,CH","exclude_countries" to "JP","timezones" to "Europe/Vienna","min_elevation" to "-10","max_elevation" to "0","min_population" to "0","sort" to "-population,+name","cursor" to "opaque+/=")) assertEquals(value,query(stub)[key])
        val resource=StubTransport(200,"""{"id":"$id","object":"city","name_local":"Wien","name":"Fixture city","country":"JP"}""")
        assertEquals("Wien",client(resource).city("Vienna") { country="AT" }.nameLocal)
        assertEquals(mapOf("country" to "AT"),query(resource))
        val searchStub=StubTransport(200,"""{"q":"Vi","cities":[]}""");client(searchStub).citySearch("Vi");assertNull(query(searchStub)["format"])
        val nearbyStub=StubTransport(200,"""{"city":"Vienna","state":null,"country":"AT","radius":5,"unit":"mi","nearby":[]}""");client(nearbyStub).cityNearby("Vienna");assertEquals("/city/Vienna/nearby",URI(nearbyStub.requests.last().url).path)
    }

    @Test fun explicitMatchingRetainsSuggestionEvidence() = runBlocking {
        val stub=StubTransport(200,"""{"object":"list","data":[{"id":"$id","object":"city","name":"Vienna","country":"AT","match":{"type":"fuzzy","source":"name","value":"Vienna","language":null,"edit_distance":1}}],"has_more":false,"url":"/city","next_cursor":null}""")
        val result=client(stub).citySearch("Viena") { match="fuzzy";maxDistance=1 }
        assertEquals(1,result.cities[0].match?.editDistance);assertEquals("Vienna",result.cities[0].match?.value)
        assertEquals(mapOf("q" to "Viena","match" to "fuzzy","max_distance" to "1"),query(stub))
    }

    @Test fun partialRelationsAndZeroUnknownDistances() = runBlocking {
        val stub=StubTransport(200,"""{"object":"list","data":[],"has_more":false,"url":"/city","next_cursor":null,"city_id":"$id","relationship":"children","coverage":"partial"}""")
        assertEquals("partial",client(stub).cityChildren(id) { cursor="opaque+/=";limit=2 }.coverage)
        assertEquals("/city/id/$id/children",URI(stub.requests.last().url).path)
        client(stub).cityAncestors(id);assertEquals("/city/id/$id/ancestors",URI(stub.requests.last().url).path)
        client(stub).citySiblings(id);assertEquals("/city/id/$id/siblings",URI(stub.requests.last().url).path)
        for(value in listOf("0","null")) {
            val distance=StubTransport(200,"""{"object":"city_distance","from":"$id","to":"$id","distance":$value,"distance_mi":$value,"method":"great_circle"}""")
            assertEquals(if(value=="0") 0.0 else null,client(distance).cityDistance(id,id).distance)
            assertTrue(query(distance).isEmpty())
        }
    }

    @Test fun bounded409ChoicesAndOtherErrorsUnchanged() = runBlocking {
        for((status,code,present) in listOf(Triple(409,"ambiguous_city",true),Triple(400,"ambiguous_city",false),Triple(409,"invalid_request",false),Triple(429,"rate_limited",false))) {
            val safe="""{"id":"$id","object":"city","name":"Vienna","state":null,"population":99}"""
            val stub=StubTransport(mutableListOf(ParseAPIResponse(status,"""{"code":"$code","message":"Choose","candidates":[$safe,$safe,$safe]}""",mapOf("retry-after" to "60"))))
            val error=assertFailsWith<ParseAPIException> { client(stub).city("Vienna") { ambiguity="error" } }
            assertEquals(code,error.code);assertEquals("60",error.retryAfter);assertEquals(1,stub.requests.size)
            assertEquals(present,error.candidates!=null);if(present) { assertEquals(2,error.candidates!!.size);assertEquals("Vienna",error.candidates!!.first().name);assertNull(error.candidates!!.first().state) }
        }
    }

    @Test fun reviewedRomanizedOptInAndNull() = runBlocking {
        val stub=StubTransport(200,"""{"id":"city_7aj899nvwyku","object":"city","name_romanized":"Kitakyūshū","romanization_language":"ja-Latn","name":"Fixture city","country":"JP"}""");val row=client(stub).cityId(id){romanized=true};assertEquals("Kitakyūshū",row.nameRomanized);assertEquals("ja-Latn",row.romanizationLanguage);assertEquals(mapOf("romanized" to "true"),query(stub))
        val unknown=StubTransport(200,"""{"id":"city_7aj899nvwyku","object":"city","name_romanized":null,"romanization_language":null,"name":"Fixture city","country":"JP"}""");assertNull(client(unknown).cityId(id){romanized=true}.nameRomanized)
    }


    @Test fun spellingRouteAdaptsClassicEnvelopeOnly() = runBlocking {
        val stub=StubTransport(200,"""{"object":"list","q":"Viena","data":[{"id":"$id","object":"city","name":"Vienna","country":"AT"}],"has_more":false,"url":"/city/search","next_cursor":null}""")
        val classic=client(stub).citySearch("Viena") { match="fuzzy";maxDistance=1 }
        assertEquals("Vienna",classic.cities.first().name);assertEquals("Viena",classic.q);assertEquals("/city/search",URI(stub.requests.last().url).path)
        client(stub).citySearch("Viena") { match="fuzzy" };assertEquals("/city/search",URI(stub.requests.last().url).path)
        val old=StubTransport(200,"""{"q":"Vi","cities":[]}""");client(old).citySearch("Vi");assertEquals("/city",URI(old.requests.last().url).path)
    }

}
