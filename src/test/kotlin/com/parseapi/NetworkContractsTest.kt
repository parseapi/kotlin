package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*

@OptIn(ExperimentalSerializationApi::class)
class NetworkContractsTest {
    private val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
    private val fixtures = codec.parseToJsonElement(javaClass.getResource("/network-contracts.json")!!.readText()).jsonObject

    @Test fun currentAndFrozenNetworkContracts() = runBlocking {
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
            if (name == "stack_unavailable") { assertNull(result.cms); assertNull(result.checkedAt) }
            else { assertNotNull(result.cms); assertNotNull(result.frameworks) }
        }
        val oldBody = JsonObject(fixtures.getValue("stack_success").jsonObject + mapOf(
            "scope" to JsonPrimitive("site"), "pages" to JsonPrimitive(2), "partial" to JsonPrimitive(false),
            "analytics" to JsonNull,
        ))
        val oldResult = codec.decodeFromJsonElement<Stack>(oldBody)
        val encoded = codec.encodeToJsonElement(oldResult).jsonObject
        assertNull(encoded["scope"])
        assertNull(encoded["pages"])
        assertNull(encoded["partial"])
        val roundtrip = codec.decodeFromJsonElement<Stack>(encoded)
        assertEquals("wordpress", roundtrip.cms?.first()?.technology)
        assertEquals(emptyList(), roundtrip.frameworks); assertNull(roundtrip.analytics)
        val stateStub = StubTransport(200, fixtures.getValue("state_current").toString())
        val state = ParseAPI("test") { transport = stateStub }.state("CA") { country = "US" }
        assertEquals(listOf("America/Los_Angeles"), state.timezones)
        assertTrue(stateStub.requests.single().url.endsWith("/state/CA?country=US"))
        val districtStub = StubTransport(200, fixtures.getValue("district_current").toString())
        val district = ParseAPI("test") { transport = districtStub }.district("36061") { country = "US"; this.state = "NY" }
        assertEquals(listOf("America/New_York"), district.timezones)
        assertTrue(districtStub.requests.single().url.endsWith("/district/36061?country=US&state=NY"))
        val domain = codec.decodeFromJsonElement<Domain>(fixtures.getValue("domain_unregistered"))
        assertEquals(true, domain.available); assertEquals(false, domain.deep?.registration?.registered)
        assertNull(domain.deep?.registration?.dnssec)
    }
}
