package com.parseapi
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
class ResponseDecodingTest {
 private val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
 private fun body(profile: String, name: String) = javaClass.getResource("/response-fixtures/$profile-$name.json")!!.readText()
 @Test fun actualHandlerShapesAndGetters() {
  for (profile in listOf("standard","clean")) {
   val zones = json.decodeFromString<TimeZones>(body(profile,"timezones"))
   val time = json.decodeFromString<Timezone>(body(profile,"time"))
   val tariff = json.decodeFromString<Tariff>(body(profile,"tariff"))
   val search = json.decodeFromString<TariffSearch>(body(profile,"tariff-search"))
   val metadata = json.decodeFromString<EmojiMetadata>(body(profile,"emoji-metadata"))
   val catalog = json.decodeFromString<EmojiCatalog>(body(profile,"emoji-catalog"))
   val coverage = json.decodeFromString<CompanyCoverage>(body(profile,"company-coverage"))
   val company = json.decodeFromString<CompanyProfile>(body(profile,"company-profile"))
   assertTrue(zones.timezones.isNotEmpty()); assertEquals(1791907200L,time.unix)
   assertEquals("US",time.location?.candidates?.first()?.country)
   assertEquals("0101.21.00.10",tariff.hts); assertTrue(search.lines.isNotEmpty())
   assertTrue(metadata.notices.isNotEmpty()); assertTrue(catalog.emojis.isNotEmpty())
   assertEquals(1L,coverage.companies); assertEquals(12L,company.deep?.employees?.count)
   assertEquals(false,company.deep?.employees?.approximate); assertEquals(if(profile == "standard") "reported" else "",company.deep?.employees?.method)
   if (profile == "standard") {
    assertTrue(zones.timezoneDatabaseVersion.isNotEmpty()); assertTrue(tariff.revision.isNotEmpty()); assertTrue(search.revision.isNotEmpty())
    assertTrue(metadata.sourceVersion.isNotEmpty()); assertTrue(coverage.snapshotAt.isNotEmpty())
   } else {
    assertEquals("",zones.timezoneDatabaseVersion); assertEquals("",tariff.revision); assertEquals("",search.revision)
    assertEquals("",metadata.sourceVersion); assertEquals("",coverage.snapshotAt)
    assertNull(time.location?.candidates?.first()?.id); assertNull(time.location?.candidates?.first()?.timezone)
    assertEquals("",time.location?.source)
   }
  }
  val malformed=body("standard","timezones").replace(Regex("\"timezone_database_version\": \"[^\"]*\""),"\"timezone_database_version\": null")
  assertFailsWith<SerializationException> { json.decodeFromString<TimeZones>(malformed) }
 }
 @Test fun oldClientSignaturesAndDefaultHeaders() = runBlocking {
  val sent=mutableListOf<ParseAPIRequest>()
  val stub=ParseAPITransport { req -> sent.add(req); ParseAPIResponse(200,body("standard","timezones"),emptyMap()) }
  val parse=ParseAPI("fixture") { retries=0; transport=stub }
  assertTrue(parse.timeZones().timezones.isNotEmpty())
  assertEquals(1,sent.size); assertFalse(sent[0].headers.keys.any { it.equals("Parse-Response",ignoreCase=true) })
  assertEquals("2.0.0",sent[0].headers["Parse-Version"])
 }
 @Test fun ordinaryClientDecodesCleanResponse() = runBlocking {
  val sent=mutableListOf<ParseAPIRequest>()
  val stub=ParseAPITransport { req -> sent.add(req); ParseAPIResponse(200,body("clean","timezones"),emptyMap()) }
  val parse=ParseAPI("fixture") { retries=0; transport=stub }
  assertTrue(parse.timeZones().timezones.isNotEmpty()); assertEquals(1,sent.size)
  assertFalse(sent[0].headers.containsKey("Parse-Response")); assertEquals("2.0.0",sent[0].headers["Parse-Version"])
 }

 @Test fun retiredNullMetadataDoesNotChangeMeaningfulUnknowns() = runBlocking {
  val body="""{"timezones":[],"timezone_database_version":null}"""
  val client=ParseAPI("fixture") { retries=0; transport=ParseAPITransport {ParseAPIResponse(200,body,emptyMap())} }
  assertEquals("",client.timeZones().timezoneDatabaseVersion)
 }
}
