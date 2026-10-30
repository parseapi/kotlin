package com.parseapi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*

@OptIn(ExperimentalSerializationApi::class)
class NetworkContractsTest {
    private val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
    private val fixtures = codec.parseToJsonElement(javaClass.getResource("/network-contracts.json")!!.readText()).jsonObject

    @Test fun currentAndFrozenNetworkContracts() {
        for (name in listOf("ip_current", "ip_frozen", "ip_locked")) {
            val deep = codec.decodeFromJsonElement<Ip>(fixtures.getValue(name)).deep!!
            if (name == "ip_current") {
                assertEquals("27053", deep.district)
                assertEquals("Hennepin County", deep.districtName)
                assertEquals("West North Central", deep.censusDivision)
                assertEquals("Midwest", deep.censusRegion)
            } else { assertNull(deep.district); assertNull(deep.censusRegion) }
        }
        for (name in listOf("stack_success", "stack_unavailable", "stack_empty")) {
            val result = codec.decodeFromJsonElement<Stack>(fixtures.getValue(name))
            val scope: String = result.scope
            val pages: Int = result.pages
            assertEquals("", scope); assertEquals(0, pages); assertNull(result.partial)
            if (name == "stack_unavailable") { assertNull(result.cms); assertNull(result.checkedAt) }
            else { assertNotNull(result.cms); assertNotNull(result.frameworks) }
        }
        val oldBody = JsonObject(fixtures.getValue("stack_success").jsonObject + mapOf(
            "scope" to JsonPrimitive("site"), "pages" to JsonPrimitive(2), "partial" to JsonPrimitive(false),
            "analytics" to JsonNull,
        ))
        val oldResult = codec.decodeFromJsonElement<Stack>(oldBody)
        val encoded = codec.encodeToJsonElement(oldResult).jsonObject
        assertEquals(JsonPrimitive("site"), encoded["scope"])
        assertEquals(JsonPrimitive(2), encoded["pages"])
        assertEquals(JsonPrimitive(false), encoded["partial"])
        val roundtrip = codec.decodeFromJsonElement<Stack>(encoded)
        assertEquals("wordpress", roundtrip.cms?.first()?.technology)
        assertEquals(emptyList(), roundtrip.frameworks); assertNull(roundtrip.analytics)
        val domain = codec.decodeFromJsonElement<Domain>(fixtures.getValue("domain_unregistered"))
        assertTrue(domain.available); assertEquals(false, domain.deep?.registration?.registered)
        assertNull(domain.deep?.registration?.dnssec)
    }
}
