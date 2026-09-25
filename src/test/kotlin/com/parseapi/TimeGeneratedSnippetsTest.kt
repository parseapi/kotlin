package com.parseapi
import kotlin.test.*
import kotlinx.coroutines.runBlocking
private object BuildConfig { const val APPLICATION_ID="com.fixture" }
class TimeGeneratedSnippetsTest {
 @Test fun literalCalls()=runBlocking {
 val stub=StubTransport(200,"""{"timezone": null, "at": null, "unix": null, "targets": null, "timezone_database_version": "2026c", "timezones": [], "zones": [], "location": {"input": {"type": "city", "value": "Paris"}, "status": "not_found", "candidates": [], "truncated": false, "source": "fixture"}, "deep": {"season": null}}""")
run {

val parse = ParseAPI("parse_app_...") {
    appId = BuildConfig.APPLICATION_ID
    transport = stub
    retries = 0
}

// Inside a coroutine.
val result = parse.time() {
    iata = "JFK"
    deep = true
}
println(result)
}
run {

val parse = ParseAPI("parse_app_...") {
    appId = BuildConfig.APPLICATION_ID
    transport = stub
    retries = 0
}

// Inside a coroutine.
val result = parse.timeZones() {
    country = "US"
    dst = false
    observesDst = true
    at = "2026-01-01T00:00:00Z"
    details = true
    sort = "offset"
}
println(result)
}
run {

val parse = ParseAPI("parse_app_...") {
    appId = BuildConfig.APPLICATION_ID
    transport = stub
    retries = 0
}

// Inside a coroutine.
val result = parse.time() {
    at = "2026-09-24T12:00:00Z"
    city = "Paris"
    country = "FR"
    deep = true
    disambiguation = "reject"
    targets = listOf("UTC", "Asia/Tokyo")
}
println(result)
}

 assertEquals(3,stub.requests.size)
 val paths=listOf("/time?iata=JFK&deep=true","/time/zones?country=US&dst=false&observes_dst=true&details=true&sort=offset&at=2026-01-01T00:00:00Z","/time?city=Paris&country=FR&at=2026-09-24T12:00:00Z&targets=UTC,Asia/Tokyo&deep=true&disambiguation=reject")
 for ((request,path) in stub.requests.zip(paths)) {
  val url=java.net.URI(request.url);val expected=java.net.URI(path)
  assertEquals(expected.path,url.path)
  fun query(value:String?):Map<String,String> = (value?:"").split('&').filter{it.isNotEmpty()}.associate{val p=it.split('=',limit=2);java.net.URLDecoder.decode(p[0],"UTF-8") to java.net.URLDecoder.decode(p.getOrElse(1){""},"UTF-8")}
  assertEquals(query(expected.rawQuery),query(url.rawQuery))
 }
 }
}
