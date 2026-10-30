package com.parseapi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class CurrentStackWireTest {
 @Test fun capturedCurrentWireUsesOneRequest() = runBlocking {
  val body=javaClass.getResource("/current-stack.json")!!.readText()
  val stub=StubTransport(200,body)
  val client=ParseAPI("fixture") { transport=stub;retries=0 }
  val result=client.stack("stripe.com") {pretty=true}
  assertEquals(1,stub.requests.size)
  assertEquals("https://api.parseapi.com/stack/stripe.com?pretty=true",stub.requests[0].url)
  assertNotNull(result.checkedAt)
  assertTrue(result.cms!!.isEmpty())
  assertEquals(1,result.servers!!.size)
  assertTrue(result.javaClass.declaredFields.none { it.name in setOf("scope","pages","partial") })
 }
}
