package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlin.test.*

/** Full bodies produced by the local API serializer over labelled synthetic observations. */
class CurrencyReportingTest {
 private fun fixture(name: String) = javaClass.getResource("/currency/$name")!!.readText()
 @Test fun sampledHistoryAndCompleteReportsKeepOriginalDatesAndGaps() = runBlocking {
  val pair=StubTransport(200,fixture("sampled-history.json"))
  val history=ParseAPI("test") {transport=pair}.currencyHistory("USD","EUR","1971-01-04","2026-10-02") {interval="month"}
  assertEquals("month",history.interval);assertEquals("2024-01-07",history.data[1].date)
  assertEquals(true,history.data[1].stale);assertEquals(listOf("2024-02-15"),history.missing)
  assertEquals("https://api.parseapi.com/currency/USD/EUR/history?from=1971-01-04&to=2026-10-02&interval=month",pair.requests[0].url)
  val summary=StubTransport(200,fixture("period-summary.json"))
  val report=ParseAPI("test") {transport=summary}.currencyRatesSummary("U/S",listOf("EUR","JPY","XCG"),"2024-01-01","2024-02-15") {interval="month"}
  assertEquals("month",report.interval);assertEquals(0.825,report.data[0].average)
  assertEquals(0.8,report.data[0].min);assertEquals(0.85,report.data[0].max);assertEquals(2,report.data[0].observationCount)
  assertEquals("2024-01-02",report.data[0].firstDate);assertEquals("2024-01-07",report.data[0].lastDate)
  assertEquals(1,report.data[0].missingDayCount);assertNull(report.data[0].stale);assertNull(report.data[0].units)
  assertEquals(4,report.missing.size);assertEquals("no_observations",report.missing[1].reason);assertEquals(31,report.missing[0].missingDayCount)
  assertEquals("https://api.parseapi.com/currency/U%2FS/rates/summary?quotes=EUR%2CJPY%2CXCG&from=2024-01-01&to=2024-02-15&interval=month",summary.requests[0].url)
  assertEquals("2.0.0",summary.requests[0].headers["Parse-Version"])
  assertFalse(CurrencySummaryRow::class.java.declaredMethods.any {it.name=="copy" || it.name.startsWith("component")})
 }
 @Test fun oldHistoryFunctionReferencesRemainAvailable() = runBlocking {
  val stub=StubTransport(200,fixture("sampled-history.json"));val client=ParseAPI("test") {transport=stub}
  val old: suspend (String,String,String,String)->CurrencyHistory=client::currencyHistory
  val configured: suspend (String,String,String,String,CurrencyHistoryOptions.()->Unit)->CurrencyHistory=client::currencyHistory
  old("USD","EUR","2024-01-01","2024-01-31");configured("USD","EUR","2024-01-01","2024-01-31") {}
  assertTrue(stub.requests.all {!it.url.contains("interval=")})
 }
}
