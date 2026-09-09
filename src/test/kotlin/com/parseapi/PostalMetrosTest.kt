package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

private fun metroClient(body: String): ParseAPI = ParseAPI("test") {
	baseUrl = "https://api.parseapi.com"
	retries = 0
	transport = StubTransport(200, body)
}

class PostalMetrosTest {
	@Test
	fun observationStatesSurviveEveryPostalResponse() = runBlocking {
		val fields = listOf("", """, "metros":null""", """, "metros":[]""", """, "metros":[{"code":"12345","name":"Example area","type":"future-area-type","share":0.75,"residential_share":0,"business_share":1,"other_share":null,"future":true}]""")
		for ((index, field) in fields.withIndex()) {
			val detail = """, "deep":{${field.trimStart(',')}}"""
			val member = """{"postal":"12345","country":"US","city":null,"distance":0,"distance_mi":0,"future":true$detail}"""
			val postal = metroClient(member).postal("12345")
			val nearby = metroClient("""{"postal":"12345","country":"US","radius":10,"unit":"km","nearby":[$member]$detail}""").postalNearby("12345")
			val distance = metroClient("""{"country":"US","distance":0,"distance_mi":0,"from":$member,"to":$member}""").postalDistance("12345", "12345")
			for (value in listOf(postal.deep?.metros, nearby.deep?.metros, nearby.nearby[0].deep?.metros, distance.from.deep?.metros, distance.to.deep?.metros)) {
				assertEquals(if (index < 2) null else if (index == 2) 0 else 1, value?.size)
				value?.firstOrNull()?.let {
					assertEquals("12345", it.code)
					assertEquals("Example area", it.name)
					assertEquals("future-area-type", it.type)
					assertEquals(0.75, it.share)
					assertEquals(0.0, it.residentialShare)
					assertEquals(1.0, it.businessShare)
					assertNull(it.otherShare)
				}
			}
		}
	}

	@Test
	fun missingSharesRemainUnknown() = runBlocking {
		val result = metroClient("""{"postal":"12345","country":"US","deep":{"metros":[{"code":"12345","name":"Example area","type":"metropolitan"}]}}""").postal("12345")
		val metro = result.deep?.metros!!.single()
		assertNull(metro.share)
		assertNull(metro.residentialShare)
		assertNull(metro.businessShare)
		assertNull(metro.otherShare)
	}
}
