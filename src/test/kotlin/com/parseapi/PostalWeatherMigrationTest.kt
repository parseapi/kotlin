package com.parseapi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class PostalWeatherMigrationTest {
    private val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }

    @Test fun compactPostalTaxPreservesFactsWithoutInventingMetadata() {
        for (amount in listOf(0.0, 3210.5)) {
            val value = json.decodeFromString<PostalDeep>("""{"currency":"USD","property_tax":{"annual_median":$amount}}""")
            assertEquals(amount, value.propertyTax?.annualMedian)
            assertEquals("USD", value.currency)
            assertNull(value.propertyTax?.currency)
            assertNull(value.propertyTax?.period)
            val tax = json.parseToJsonElement(json.encodeToString(value.propertyTax!!)).jsonObject
            assertEquals(setOf("annual_median"), tax.keys)
        }
        val legacy = """
{"property_tax":{"annual_median":0,"currency":"USD","period":"2020-2024"}}
"""
        val postal = json.decodeFromString<PostalDeep>(legacy)
        assertEquals("USD", postal.propertyTax?.currency)
        assertEquals("2020-2024", postal.propertyTax?.period)
        val district = json.decodeFromString<DistrictDeep>(legacy)
        assertEquals("USD", district.propertyTax?.currency)
        assertEquals("2020-2024", district.propertyTax?.period)
        for (body in listOf("{}", """
{"property_tax":null}
""")) {
            assertNull(json.decodeFromString<PostalDeep>(body).propertyTax)
        }
    }

    @Test fun minuteRateReadersWorkAcrossRolloutAndCanonicalNullWins() {
        val cases = listOf(
            Triple("""
{"precipitation":2.54,"precipitation_in":0.1}
""", 2.54, 0.1),
            Triple("""
{"precipitation_rate":0,"precipitation_rate_in":0}
""", 0.0, 0.0),
            Triple("""
{"precipitation_rate":-1,"precipitation_rate_in":-0.1}
""", -1.0, -0.1),
            Triple("""
{"precipitation":99,"precipitation_in":99,"precipitation_rate":2.54,"precipitation_rate_in":0.1}
""", 2.54, 0.1),
            Triple("""
{"precipitation_rate":2.54,"precipitation_rate_in":0.1,"precipitation":99,"precipitation_in":99}
""", 2.54, 0.1),
            Triple("""
{"precipitation_rate":null,"precipitation_rate_in":null,"precipitation":99,"precipitation_in":99}
""", null, null),
            Triple("""
{"precipitation":99,"precipitation_in":99,"precipitation_rate":null,"precipitation_rate_in":null}
""", null, null),
            Triple("{}", null, null),
        )
        for ((body, metric, imperial) in cases) {
            val minute = json.decodeFromString<WeatherMinute>(body)
            assertEquals(metric, minute.precipitationRate)
            assertEquals(imperial, minute.precipitationRateIn)
            val encoded = json.parseToJsonElement(json.encodeToString(minute)).jsonObject
            assertTrue("precipitation" !in encoded && "precipitation_in" !in encoded)
            assertEquals(metric, encoded["precipitation_rate"]?.jsonPrimitive?.doubleOrNull)
            assertEquals(imperial, encoded["precipitation_rate_in"]?.jsonPrimitive?.doubleOrNull)
        }
        val history = json.decodeFromString<WeatherHistory>("""
{"precipitation":2.54,"precipitation_in":0.1}
""")
        assertEquals(2.54, history.precipitation)
        assertEquals(0.1, history.precipitationIn)
    }
}
