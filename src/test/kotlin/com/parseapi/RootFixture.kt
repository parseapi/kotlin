package com.parseapi
import kotlinx.serialization.json.*
/** Historical test observations served in the single 2.1 wire layout. */
fun rootFixture(body: String): String {
 fun lift(value: JsonElement): JsonElement = when(value) {
  is JsonArray -> JsonArray(value.map(::lift))
  is JsonObject -> { val fields=value.filterKeys { it!="deep" || value[it] !is JsonObject }.toMutableMap(); (value["deep"] as? JsonObject)?.let { fields.putAll(it) }; JsonObject(fields.mapValues { lift(it.value) }) }
  else -> value
 }
 return try { lift(Json.parseToJsonElement(body)).toString() } catch (_: Exception) { body }
}
