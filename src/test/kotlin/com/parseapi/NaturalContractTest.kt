package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.test.*
@OptIn(ExperimentalSerializationApi::class)
class NaturalContractTest {
 @Test fun approvedPayloadsDecodeAtNaturalHomes() {
  val codec=Json{ignoreUnknownKeys=true;coerceInputValues=true;namingStrategy=JsonNamingStrategy.SnakeCase}
  val rows=Json.parseToJsonElement(javaClass.getResource("/approved-2.1.json")!!.readText()).jsonArray
  fun field(value:JsonElement,path:String):JsonElement? { var item:JsonElement?=value;for(key in path.split('.'))item=(item as? JsonObject)?.get(key);return item?.takeUnless { it is JsonNull } }
  for(row in rows){val obj=row.jsonObject;val body=obj.getValue("body");val encoded=when(obj.getValue("product").jsonPrimitive.content){
"ip"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Ip>(body))
"country"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Country>(body))
"state"->codec.encodeToJsonElement(codec.decodeFromJsonElement<State>(body))
"postal"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Postal>(body))
"email"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Email>(body))
"carrier"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Carrier>(body))
"hlr"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Hlr>(body))
"useragent"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Useragent>(body))
"weather"->codec.encodeToJsonElement(codec.decodeFromJsonElement<Weather>(body))
else->error("Unknown fixture")}
   assertFalse(encoded.jsonObject.containsKey("deep"))
   for(path in obj.getValue("fields").jsonArray.map{it.jsonPrimitive.content}) {
    val want=field(body,path);val got=field(encoded,path);val message=obj.getValue("id").toString()+":"+path
    if(want is JsonPrimitive && !want.isString && want.doubleOrNull!=null && got is JsonPrimitive && !got.isString)assertEquals(want.doubleOrNull,got.doubleOrNull,message) else assertEquals(want,got,message)
   }
  }
 }
}

class EmailVerifyContractTest {
 @Test fun explicitPostDoesNotAutomaticallyRetry() = kotlinx.coroutines.runBlocking {
  val stub=StubTransport(200,"""{"email":"a+tag@example.com","valid":true,"free":false,"disposable":false,"role":false,"deliverable":false,"catchall":null}""")
  val client=ParseAPI("fixture"){transport=stub};val result=client.emailVerify("a+tag@example.com")
  assertEquals(false,result.deliverable);assertNull(result.catchall)
  val request=stub.requests.single();assertEquals("POST",request.method);assertEquals("https://api.parseapi.com/email/verify",request.url)
  assertEquals(Json.parseToJsonElement("""{"email":"a+tag@example.com"}"""),Json.parseToJsonElement(request.body!!))
  val failure=StubTransport(503,"""{"code":"service_unavailable","message":"pending"}""")
  val failing=ParseAPI("fixture"){transport=failure}
  try {failing.emailVerify("a@example.com");fail("Expected a service error")} catch (_:Exception) { }
  assertEquals(1,failure.requests.size)
 }
}
