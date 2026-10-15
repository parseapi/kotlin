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
        val stub=StubTransport(200,"""{"object":"list","data":[{"id":"$id","object":"city","name":"Vienna"}],"has_more":true,"url":"/city","next_cursor":"opaque+/="}""")
        val result=client(stub).cityList { countries=listOf("AT","CH");excludeCountries=listOf("JP");timezones=listOf("Europe/Vienna");minElevation=-10.0;maxElevation=0.0;minPopulation=0;sort="-population,+name";cursor="opaque+/=" }
        assertEquals("city",result.data.single().`object`);assertEquals("Vienna",result.data.single().name);assertNull(result.data.single().country);assertTrue(result.nextCursor != null)
        for((key,value) in mapOf("format" to "list","countries" to "AT,CH","exclude_countries" to "JP","timezones" to "Europe/Vienna","min_elevation" to "-10","max_elevation" to "0","min_population" to "0","sort" to "-population,+name","cursor" to "opaque+/=")) assertEquals(value,query(stub)[key])
        val resource=StubTransport(200,"""{"id":"$id","object":"city","name_local":"Wien"}""")
        assertEquals("Wien",client(resource).cityResource("Vienna") { country="AT" }.nameLocal)
        assertEquals(mapOf("format" to "resource","country" to "AT"),query(resource))
        client(stub).citySearchList("Vi");assertEquals("list",query(stub)["format"])
        client(stub).cityNearbyList("Vienna");assertEquals("/city/Vienna/nearby",URI(stub.requests.last().url).path)
    }

    @Test fun explicitMatchingRetainsSuggestionEvidence() = runBlocking {
        val stub=StubTransport(200,"""{"object":"list","data":[{"id":"$id","object":"city","match":{"type":"fuzzy","source":"name","value":"Vienna","language":null,"edit_distance":1}}],"has_more":false,"url":"/city","next_cursor":null}""")
        val result=client(stub).citySearchList("Viena") { match="fuzzy";maxDistance=1 }
        assertEquals(1,result.data[0].match?.editDistance);assertEquals("Vienna",result.data[0].match?.value)
        assertEquals(mapOf("q" to "Viena","format" to "list","match" to "fuzzy","max_distance" to "1"),query(stub))
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
            val safe="""{"id":"$id","object":"city","name":"Vienna","state":null,"deep":{"population":99}}"""
            val stub=StubTransport(mutableListOf(ParseAPIResponse(status,"""{"code":"$code","message":"Choose","candidates":[$safe,$safe,$safe]}""",mapOf("retry-after" to "60"))))
            val error=assertFailsWith<ParseAPIException> { client(stub).city("Vienna") { ambiguity="error" } }
            assertEquals(code,error.code);assertEquals("60",error.retryAfter);assertEquals(1,stub.requests.size)
            assertEquals(present,error.candidates!=null);if(present) { assertEquals(2,error.candidates!!.size);assertEquals("Vienna",error.candidates!!.first().name);assertNull(error.candidates!!.first().state) }
        }
    }

    @Test fun reviewedRomanizedOptInAndNull() = runBlocking {
        val stub=StubTransport(200,"""{"id":"city_7aj899nvwyku","object":"city","name_romanized":"Kitakyūshū","romanization_language":"ja-Latn"}""");val row=client(stub).cityIdResource(id){romanized=true};assertEquals("Kitakyūshū",row.nameRomanized);assertEquals("ja-Latn",row.romanizationLanguage);assertEquals(mapOf("format" to "resource","romanized" to "true"),query(stub))
        val unknown=StubTransport(200,"""{"id":"city_7aj899nvwyku","object":"city","name_romanized":null,"romanization_language":null}""");assertNull(client(unknown).cityIdResource(id){romanized=true}.nameRomanized)
    }


    @Test fun spellingRouteAdaptsClassicEnvelopeOnly() = runBlocking {
        val stub=StubTransport(200,"""{"object":"list","q":"Viena","data":[{"id":"$id","object":"city","name":"Vienna","country":"AT"}],"has_more":false,"url":"/city/search","next_cursor":null}""")
        val classic=client(stub).citySearch("Viena") { match="fuzzy";maxDistance=1 }
        assertEquals("Vienna",classic.cities.first().name);assertEquals("Viena",classic.q);assertEquals("/city/search",URI(stub.requests.last().url).path)
        client(stub).citySearchList("Viena") { match="fuzzy" };assertEquals("/city/search",URI(stub.requests.last().url).path)
        val old=StubTransport(200,"""{"q":"Vi","cities":[]}""");client(old).citySearch("Vi");assertEquals("/city",URI(old.requests.last().url).path)
    }

}
