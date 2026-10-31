package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlin.test.*
@OptIn(ExperimentalSerializationApi::class)
class CapitalCoordinatesTest {
 private val codec=Json{ignoreUnknownKeys=true;coerceInputValues=true;namingStrategy=JsonNamingStrategy.SnakeCase}
 @Test fun oldAndCurrentCoordinates() {
  for (capital in listOf("""{"name":"Example","lat":0,"lon":null}""","""{"name":"Example","latitude":0,"longitude":null}""","""{"name":"Example","lat":99,"lon":99,"latitude":0,"longitude":null}""","""{"name":"Example","latitude":0,"longitude":null,"lat":99,"lon":99}""")) {
   val body="""{"country":"XX","state":"X","name":"Example","continent":"EU","capital":$capital}"""
   val c=assertNotNull(codec.decodeFromString<Country>(body).capital);val s=assertNotNull(codec.decodeFromString<State>(body).capital)
   assertEquals(0.0,c.latitude);assertNull(c.longitude);assertEquals(0.0,s.latitude);assertNull(s.longitude)
   for(raw in listOf(codec.encodeToJsonElement(c).jsonObject,codec.encodeToJsonElement(s).jsonObject)) {
    assertFalse(raw.containsKey("lat"));assertFalse(raw.containsKey("lon"));assertEquals(JsonPrimitive(0.0),raw["latitude"])
   }
  }
 }
}
