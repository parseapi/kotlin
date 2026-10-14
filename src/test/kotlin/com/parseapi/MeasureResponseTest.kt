package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.test.Test
import kotlin.test.assertEquals

class MeasureResponseTest {
 @Test fun cleanAndLegacyResponsesDecodeThroughClient() = runBlocking {
  val cases = Json.parseToJsonElement(javaClass.getResource("/measure-response.json")!!.readText()).jsonArray
  for (body in cases) {
   val fields = body.jsonObject
   val stub = StubTransport(200, body.toString())
   val client = ParseAPI("fixture") { transport = stub; retries = 0 }
   val answer = client.measure(fields.getValue("measure").jsonPrimitive.content)
   assertEquals(fields["amount"]?.jsonPrimitive?.contentOrNull, answer.amount)
   assertEquals(fields["reason"]?.jsonPrimitive?.contentOrNull, answer.reason)
   assertEquals(fields["choices"]?.jsonArray?.size ?: 0, answer.choices.size)
  }
 }
}
