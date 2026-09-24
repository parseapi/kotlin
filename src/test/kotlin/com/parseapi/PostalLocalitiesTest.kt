package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class PostalLocalitiesTest {
	@Test
	fun choicesPreserveObservationWithoutInferringCity() = runBlocking {
		val choice = """{"city":"SYDNEY","state":"NSW","state_name":"New South Wales","future":true}"""
		val other = """{"city":"HAYMARKET","state":"NSW","state_name":"New South Wales"}"""
		val fields = listOf("", """, "localities":null""", """, "localities":[]""", """, "localities":[$choice]""", """, "localities":[$choice,$other]""")
		for ((index, field) in fields.withIndex()) {
			val client = ParseAPI("test") { transport = StubTransport(200, """{"postal":"2000","country":"AU","city":null$field}""") }
			val result = client.postal("2000") { country = "AU" }
			assertNull(result.city)
			assertEquals(if (index < 2) null else index - 2, result.localities?.size)
			result.localities?.firstOrNull()?.let {
				assertEquals("SYDNEY", it.city)
				assertEquals("NSW", it.state)
				assertEquals("New South Wales", it.stateName)
			}
		}
	}
}
