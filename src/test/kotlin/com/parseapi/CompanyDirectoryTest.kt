package com.parseapi

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.*

private const val DIRECTORY_PROFILE = """{"id":"co_222222222222","name":"Example","country":"US","website":null,"listings":[{"exchange":"Future Exchange","symbol":"A/B"}],"address":null,"deep":{"legal_name":"Example Inc.","aliases":[],"jurisdiction":{"country":"US","state":null},"status":"future-status","websites":[{"domain":"example.com","url":null}],"identifiers":[{"type":"registration","authority":"future:registry","value":"0000123"}],"incorporated":null,"addresses":[{"type":"future-role","street":null,"city":"Example City","state":null,"postal":null,"country":"US"}],"industries":[{"type":"future-scheme","code":"001","name":null}],"parent":null,"description":null,"logo":null,"socials":[],"founded":{"value":"2006","precision":"year"},"sources":[{"type":"website","url":"https://example.com/","fields":["founded"],"observed_at":"2026-09-23T17:35:06.956Z","updated_at":null,"future":true}],"future":true},"future":true}"""
private const val DIRECTORY_SEARCH = """{"companies":[{"id":"co_222222222222","name":"Example","country":"US","website":null,"listings":[{"exchange":"Future Exchange","symbol":"A/B"}],"address":null,"deep":{"legal_name":"Example Inc.","aliases":[],"jurisdiction":{"country":"US","state":null},"status":"future-status","websites":[{"domain":"example.com","url":null}],"identifiers":[{"type":"registration","authority":"future:registry","value":"0000123"}],"incorporated":null,"addresses":[{"type":"future-role","street":null,"city":"Example City","state":null,"postal":null,"country":"US"}],"industries":[{"type":"future-scheme","code":"001","name":null}],"parent":null,"description":null,"logo":null,"socials":[],"founded":{"value":"2006","precision":"year"},"sources":[{"type":"website","url":"https://example.com/","fields":["founded"],"observed_at":"2026-09-23T17:35:06.956Z","updated_at":null,"future":true}],"future":true},"future":true,"match":{"field":"future-field","value":"0000123","type":"future-scheme","authority":null,"exchange":null,"future":true}}],"next":"opaque+/="}"""
private const val DIRECTORY_COVERAGE = """{"scope":"sample","label":"Company directory","description":"Edition profiles","snapshot_at":"2026-09-23T17:35:06.956Z","companies":0,"countries":[],"with_website":0,"with_listings":0,"with_address":0,"future":true}"""

class CompanyDirectoryTest {
 @Test fun routesSelectorsAndResponseFields() = runBlocking {
  val stub=StubTransport(mutableListOf(
   ParseAPIResponse(200,DIRECTORY_PROFILE,emptyMap()),ParseAPIResponse(200,DIRECTORY_PROFILE,emptyMap()),
   ParseAPIResponse(200,DIRECTORY_SEARCH,emptyMap()),ParseAPIResponse(200,DIRECTORY_SEARCH,emptyMap()),
   ParseAPIResponse(200,DIRECTORY_SEARCH,emptyMap()),ParseAPIResponse(200,DIRECTORY_SEARCH,emptyMap()),
   ParseAPIResponse(200,DIRECTORY_COVERAGE,emptyMap())))
  val c=ParseAPI("test_key") {transport=stub;retries=0}
  val profile=c.companyId("co_/ ?") {deep=true}
  assertEquals("0000123",profile.deep?.identifiers?.first()?.value)
  assertEquals("future-status",profile.deep?.status)
  assertTrue(profile.deep!!.socials!!.isEmpty())
  assertEquals("year",profile.deep?.founded?.precision)
  assertNull(profile.deep?.sources?.first()?.updatedAt)
  assertNull(profile.deep?.jurisdiction?.state)
  c.companyId("co_222222222222")
  val search=c.companySearch {query="A & B";country="US";limit=2;cursor="opaque+/=";deep=true}
  assertEquals("opaque+/=",search.next)
  assertEquals("future-field",search.companies.first().match.field)
  assertEquals("Example Inc.",search.companies.first().deep?.legalName)
  c.companySearch {domain="https://sub.example.com/a?b=1"}
  c.companySearch {ticker="A/B";exchange="Future Exchange"}
  c.companySearch {identifier="0000123";authority="future:registry"}
  val coverage=c.companyCoverage()
  assertEquals(0L,coverage.companies);assertTrue(coverage.countries.isEmpty())
  val expected=listOf("/company/id/co_%2F%20%3F?deep=true","/company/id/co_222222222222",
   "/company?q=A%20%26%20B&country=US&limit=2&cursor=opaque%2B%2F%3D&deep=true",
   "/company?domain=https%3A%2F%2Fsub.example.com%2Fa%3Fb%3D1",
   "/company?ticker=A%2FB&exchange=Future%20Exchange",
   "/company?identifier=0000123&authority=future%3Aregistry","/company/directory/coverage")
  assertEquals(expected.size,stub.requests.size)
  stub.requests.zip(expected).forEach {(request,path)->
   assertEquals("https://api.parseapi.com"+path,request.url)
   assertEquals("2.0.0",request.headers["Parse-Version"])
  }
 }
 @OptIn(ExperimentalSerializationApi::class)
 @Test fun oldEmptyPartialAndFutureDeep() {
  val json=Json {ignoreUnknownKeys=true;coerceInputValues=true;namingStrategy=JsonNamingStrategy.SnakeCase}
  for(suffix in listOf("", ""","deep":{}""", ""","deep":{"legal_name":"Old name"}""", ""","deep":{"socials":null,"sources":null,"description":null,"employees":null}""")) {
   val value=json.decodeFromString<CompanyProfile>("""{"id":"co_222222222222","name":"Example","listings":[]"""+suffix+"}")
   assertNull(value.deep?.socials);assertNull(value.deep?.sources);assertNull(value.deep?.founded);assertNull(value.deep?.employees)
   if(suffix.isEmpty())assertNull(value.deep)
  }
  val value=json.decodeFromString<CompanyProfile>("""{"id":"co_222222222222","name":"Example","listings":[],"deep":{"socials":[],"sources":[],"founded":{"value":"spring 2006","precision":"season"}}}""")
  assertTrue(value.deep!!.socials!!.isEmpty());assertTrue(value.deep!!.sources!!.isEmpty());assertEquals("season",value.deep?.founded?.precision)
  val empty=json.decodeFromString<CompanySearch>("""{"companies":[],"next":null}""")
  assertTrue(empty.companies.isEmpty());assertNull(empty.next)
 }
 @Test fun serverValidatesSelectors() = runBlocking {
  val stub=StubTransport(400,"""{"code":"invalid_request","message":"Choose one selector"}""")
  val c=ParseAPI("test_key") {transport=stub;retries=0}
  val failure=assertFailsWith<ParseAPIException> {c.companySearch {query="Example";domain="example.com"}}
  assertEquals("invalid_request",failure.code);assertEquals(1,stub.requests.size)
 }
 @Test fun employeeObservationsPreserveZeroFalseDatesAndOpenCodes() = runBlocking {
  val observations=listOf(
   """{"count":0,"as_of":"2025-12-31","scope":"legal_entity","method":"reported","approximate":false}""",
   """{"count":12500,"as_of":"2026-06-30","scope":"consolidated_group","method":"reported","approximate":true}""",
   """{"count":7,"as_of":"2026-01-15","scope":"future_scope","method":"future_method","approximate":false,"future":null}""")
  val counts=listOf(0L,12500L,7L)
  val dates=listOf("2025-12-31","2026-06-30","2026-01-15")
  val scopes=listOf("legal_entity","consolidated_group","future_scope")
  val methods=listOf("reported","reported","future_method")
  observations.forEachIndexed { index,json ->
   val profileJSON="""{"id":"co_222222222222","name":"Example","deep":{"employees":$json}}"""
   val searchJSON="""{"companies":[${profileJSON.dropLast(1)},"match":{"field":"name"}}],"next":null}"""
   val stub=StubTransport(mutableListOf(ParseAPIResponse(200,profileJSON,emptyMap()),ParseAPIResponse(200,searchJSON,emptyMap())))
   val client=ParseAPI("test_key") {transport=stub;retries=0}
   val profile=client.companyId("co_222222222222") {deep=true}
   val page=client.companySearch {query="Example";deep=true}
   for(optional in listOf(profile.deep?.employees,page.companies.first().deep?.employees)) {
    val value=assertNotNull(optional)
    assertEquals(counts[index],value.count);assertEquals(dates[index],value.asOf)
    assertEquals(scopes[index],value.scope);assertEquals(methods[index],value.method)
    assertEquals(index == 1,value.approximate)
   }
  }
 }

 @Test fun countryAndSICDiscoveryPreservesFiltersAndCursor() = runBlocking {
  val payload="""{"companies":[],"next":null}"""
  val stub=StubTransport(MutableList(4) {ParseAPIResponse(200,payload,emptyMap())})
  val client=ParseAPI("test_key") {transport=stub;retries=0}
  client.companySearch {country="US"}
  client.companySearch {industry="0700";industryType="sic"}
  client.companySearch {country="US";industry="0700";industryType="sic";limit=2;cursor="opaque+/=";deep=true}
  client.companySearch {query="Example";industry="0700";industryType="sic";deep=false}
  val expected=listOf(
   "/company?country=US", "/company?industry=0700&industry_type=sic",
   "/company?country=US&industry=0700&industry_type=sic&limit=2&cursor=opaque%2B%2F%3D&deep=true",
   "/company?q=Example&industry=0700&industry_type=sic")
  assertEquals(4,stub.requests.size)
  stub.requests.zip(expected).forEach {(request,path)->assertEquals("https://api.parseapi.com"+path,request.url)}
 }

 @Test fun registrationDiscoveryPreservesExactSourceStringsAndCursor() = runBlocking {
  val stub=StubTransport(MutableList(3) {ParseAPIResponse(200,"""{"companies":[],"next":null}""",emptyMap())})
  val client=ParseAPI("test_key") {transport=stub;retries=0}
  client.companySearch {registrationAuthority="ra000599"}
  client.companySearch {country="US";industry="0700";industryType="sic";registrationAuthority="RA000599";registrationForm="DPC";registrationStatus=" Good Standing ";limit=2;cursor="opaque+/=";deep=true}
  client.companySearch {identifier="00001";authority="SEC";registrationAuthority="RA000599";registrationForm="future/Form";registrationStatus="future+& status"}
  val expected=listOf(
   "/company?registration_authority=ra000599",
   "/company?country=US&industry=0700&industry_type=sic&registration_authority=RA000599&registration_form=DPC&registration_status=%20Good%20Standing%20&limit=2&cursor=opaque%2B%2F%3D&deep=true",
   "/company?identifier=00001&registration_authority=RA000599&registration_form=future%2FForm&registration_status=future%2B%26%20status&authority=SEC")
  assertEquals(expected.size,stub.requests.size)
  stub.requests.zip(expected).forEach {(request,path)->assertEquals("https://api.parseapi.com"+path,request.url)}
 }

 @Test fun registrationsPreserveRolesZerosNullsAndUnknownFields() = runBlocking {
  val item="""{"authority":"RA000599","number":"0001234567","jurisdiction":{"country":"US","state":"CO"},"role":"domestic","legal_form":{"code":"DNC","name":"Domestic Non-profit Corporation"},"status":"Good Standing","formation_date":"2004-02-29","address":{"kind":"principal","line1":"12 Main St.","line2":"Suite 2","city":"Example","state":"CO","postal":"00123-0001","country_raw":"US"},"future":"retained"}"""
  for(deep in listOf("{}","""{"registrations":null}""","""{"registrations":[]}""","""{"registrations":[$item]}""")) {
   val text="""{"id":"co_222222222222","name":"Example","listings":[],"deep":$deep}"""
   val pageText="""{"companies":[${text.dropLast(1)},"match":{"field":"identifier"}}],"next":null}"""
   val stub=StubTransport(mutableListOf(ParseAPIResponse(200,text,emptyMap()),ParseAPIResponse(200,pageText,emptyMap())))
   val client=ParseAPI("test_key") {transport=stub;retries=0};val profile=client.companyId("co_222222222222") {this.deep=true};val page=client.companySearch {identifier="0001234567";authority="RA000599";this.deep=true}
   for(rows in listOf(profile.deep?.registrations,page.companies.first().deep?.registrations)) {
    rows?.firstOrNull()?.let {r->assertEquals("0001234567",r.number);assertEquals("US",r.jurisdiction.country);assertEquals("2004-02-29",r.formationDate);assertEquals("principal",r.address?.kind);assertEquals("00123-0001",r.address?.postal)}
    if(deep=="""{"registrations":[]}""")assertTrue(rows!!.isEmpty())
   }
  }
 }

}
