package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.*

private const val FX_PAIR = """{"base":"USD","quote":"JPY","rate":148.99,"date":"2026-10-09","converted":null,"requested_date":"2026-10-10","stale":true,"dates":{"base":["2026-10-08","2026-10-09"]},"units":{"quote":"indexed_unit"},"future":true}"""

class CurrencyReferenceTest {
 private fun captured(name: String) = javaClass.getResource("/currency/$name.json")!!.readText()
 @Test fun capturedCollectionsAndNonCashUnitsDecode() = runBlocking {
  val catalog=StubTransport(200,captured("currencies"))
  assertEquals(2,ParseAPI("test") {transport=catalog}.currencyList {q="dollar";country="US";deep=true;lang="fr"}.data.size)
  assertEquals("https://api.parseapi.com/currency?q=dollar&country=US&deep=true&lang=fr",catalog.requests[0].url)
  val rates=StubTransport(200,captured("rates"))
  assertEquals(46,ParseAPI("test") {transport=rates}.currencyRates("USD") {}.rates.size)
  assertEquals("https://api.parseapi.com/currency/USD/rates",rates.requests[0].url)
  val metal=StubTransport(200,captured("metal"))
  val gold=ParseAPI("test") {transport=metal}.currencyRate("USD","XAU") {amount=100.0}
  assertNull(gold.converted);assertEquals("troy_ounce",gold.units?.quote)
 }
 @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
 @Test fun oldSerializerFieldsKeepTheirPositionsAndDefaults() {
  val descriptor=CurrencyRate.serializer().descriptor
  assertEquals(listOf("base","quote","rate","date","amount","converted","source"),(0..6).map(descriptor::getElementName))
  assertTrue((4..6).all(descriptor::isElementOptional))
  assertTrue((7 until descriptor.elementsCount).all(descriptor::isElementOptional))
  assertFalse(CurrencyRate::class.java.declaredMethods.any {it.name=="copy" || it.name.startsWith("component")})
  assertFalse(CurrencyRate::class.java.declaredConstructors.any {java.lang.reflect.Modifier.isPublic(it.modifiers) && !it.isSynthetic})
 }
 @Test fun oldOverloadsAndFunctionReferencesRemainAvailable() = runBlocking {
  val stub=StubTransport(200,"""{"base":"USD","quote":"EUR","rate":0.85,"date":"2026-10-09"}""")
  val client=ParseAPI("test") {transport=stub}
  val old: suspend (String,String)->CurrencyRate=client::currencyRate
  val configurable: suspend (String,String,CurrencyRateOptions.()->Unit)->CurrencyRate=client::currencyRate
  assertNull(old("USD","EUR").source)
  assertNull(configurable("USD","EUR") {}.stale)
  assertEquals(2,stub.requests.size)
  assertTrue(stub.requests.all {it.url=="https://api.parseapi.com/currency/USD/EUR"})
 }
 @Test fun sourceEvidenceAndNullableUnknownsArePreserved() = runBlocking {
  val stub=StubTransport(200,FX_PAIR)
  val rate=ParseAPI("test") {transport=stub}.currencyRate("U/S","JPY") {date="2026-10-10";amount=0.0}
  assertEquals("https://api.parseapi.com/currency/U%2FS/JPY?date=2026-10-10&amount=0",stub.requests[0].url)
  assertNull(rate.converted);assertEquals("2026-10-09",rate.date);assertEquals(true,rate.stale)
  assertEquals(listOf("2026-10-08","2026-10-09"),rate.dates?.base);assertEquals("indexed_unit",rate.units?.quote)
 }
 @Test fun batchKeepsMissingAndNullableDate() = runBlocking {
  val stub=StubTransport(200,"""{"base":"USD","rates":{},"missing":[{"quote":"XCG","reason":"rate_unavailable"}]}""")
  val result=ParseAPI("test") {transport=stub}.currencyRates("USD",listOf("JPY","XCG")) {amount=0.0}
  assertNull(result.requestedDate);assertEquals("XCG",result.missing[0].quote);assertTrue(result.rates.isEmpty())
  assertEquals("https://api.parseapi.com/currency/USD/rates?quotes=JPY%2CXCG&amount=0",stub.requests[0].url)
 }
 @Test fun historyKeepsCalendarDayAndExplicitGaps() = runBlocking {
  val pair=StubTransport(200,"""{"base":"USD","quote":"JPY","from":"2026-10-09","to":"2026-10-10","data":[$FX_PAIR],"missing":["2026-10-09"]}""")
  val history=ParseAPI("test") {transport=pair}.currencyHistory("USD","JPY","2026-10-09","2026-10-10") {}
  assertEquals("2026-10-10",history.data[0].requestedDate);assertEquals(listOf("2026-10-09"),history.missing)
  assertEquals("https://api.parseapi.com/currency/USD/JPY/history?from=2026-10-09&to=2026-10-10",pair.requests[0].url)
  val multi=StubTransport(200,"""{"base":"USD","quotes":["JPY","XCG"],"from":"2026-10-09","to":"2026-10-10","data":[],"missing":[{"requested_date":"2026-10-10","quote":"XCG","reason":"future-reason"}]}""")
  val result=ParseAPI("test") {transport=multi}.currencyRatesHistory("USD",listOf("JPY","XCG"),"2026-10-09","2026-10-10") {amount=0.0}
  assertEquals("2026-10-10",result.missing[0].requestedDate);assertEquals("future-reason",result.missing[0].reason)
  assertEquals("https://api.parseapi.com/currency/USD/rates/history?quotes=JPY%2CXCG&from=2026-10-09&to=2026-10-10&amount=0",multi.requests[0].url)
 }
}
