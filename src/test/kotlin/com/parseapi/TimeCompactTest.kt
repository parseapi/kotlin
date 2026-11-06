package com.parseapi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*

@OptIn(ExperimentalSerializationApi::class)
class TimeCompactTest {
 @Test fun approvedCompactTimePlacesAndChoicesDecode() {
  val codec = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
  val rows = Json.parseToJsonElement(javaClass.getResource("/time-compact.json")!!.readText()).jsonArray
  for (row in rows) {
   val body = row.jsonObject.getValue("body").jsonObject
   val decoded = codec.decodeFromJsonElement<Timezone>(body)
   val expected = body["location"] as? JsonObject
   if (expected == null) { assertNull(decoded.location); continue }
   val actual = assertNotNull(decoded.location)
   assertEquals(expected["name"]?.jsonPrimitive?.contentOrNull, actual.name)
   assertEquals(expected["id"]?.jsonPrimitive?.contentOrNull, actual.id)
   assertEquals(expected["country"]?.jsonPrimitive?.contentOrNull, actual.country)
   assertEquals(expected["state"]?.jsonPrimitive?.contentOrNull, actual.state)
   assertEquals(expected["latitude"]?.jsonPrimitive?.doubleOrNull, actual.latitude)
   assertEquals(expected["longitude"]?.jsonPrimitive?.doubleOrNull, actual.longitude)
   assertEquals(expected["status"]?.jsonPrimitive?.contentOrNull, actual.status)
  }
 }
}
