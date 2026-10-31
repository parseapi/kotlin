package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*
@OptIn(ExperimentalSerializationApi::class)
class CurrencyCompatibilityTest {
 @Test fun preservesUnknownZeroAndServerPrecision(){
  val codec=Json{ignoreUnknownKeys=true;namingStrategy=JsonNamingStrategy.SnakeCase}
  val rows=codec.parseToJsonElement(javaClass.getResource("/currency-compatibility.json")!!.readText()).jsonObject
  val p=codec.decodeFromJsonElement<Currency>(rows["profile"]!!);assertEquals(0,p.digits);assertNull(p.symbol);assertEquals(listOf("JP"),p.countries)
  for(n in listOf("locked","nulls")){val p=codec.decodeFromJsonElement<Currency>(rows[n]!!);assertNotNull(p.deep);assertNull(p.isoNumeric);assertNull(p.namePlural);assertNull(p.countries)}
  val r=codec.decodeFromJsonElement<CurrencyRate>(rows["rate"]!!);assertEquals(158.33550065,r.rate);assertEquals(1.005,r.amount);assertEquals(1.0,r.converted)
  val z=codec.decodeFromJsonElement<CurrencyRate>(rows["zero"]!!);assertEquals(0.0,z.rate);assertEquals(0.0,z.amount);assertEquals(0.0,z.converted)
 }
}
