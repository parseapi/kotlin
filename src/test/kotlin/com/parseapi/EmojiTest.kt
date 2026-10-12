package com.parseapi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*

class EmojiTest {
 @Test fun actualArtwork() = runBlocking {
  val fixture=Json.parseToJsonElement(javaClass.getResource("/emoji-artwork.json")!!.readText()).jsonObject
  val metadata=ParseAPI("test") {transport=StubTransport(200,fixture["metadata"]!!.toString())}.emojiMetadata()
  val artwork=ParseAPI("test") {transport=StubTransport(200,fixture["emoji"]!!.toString())}.emoji("🔥") {deep=true}.deep!!.artwork!!
  assertEquals(3,metadata.artworkSources.size);assertEquals(3,artwork.size)
  assertTrue(metadata.artworkSources[0].notices.isNotEmpty())
  assertEquals("https://cdn.parseapi.com/emoji/openmoji/17.0.0/1f680.svg",artwork["openmoji"]!!.svg)
  assertEquals("https://cdn.parseapi.com/emoji/openmoji/17.0.0/72/1f680.png",artwork["openmoji"]!!.png["72"])
 }

 @Test fun catalogFiltersAndPrivateTextBody() = runBlocking {
  val stub=StubTransport(200,"""{"q":"","emojis":[],"total":0,"next_offset":null}""")
  val client=ParseAPI("test") {transport=stub}
  client.emojiSearch("face") {category="Smileys & Emotion";version="17.0";maxVersion="18.0";includeSkinTones=false;offset=0}
  client.emojiBrowse {includeSkinTones=false;offset=2}
  assertEquals("https://api.parseapi.com/emoji?q=face&category=Smileys%20%26%20Emotion&version=17.0&max_version=18.0&include_skin_tones=false&offset=0",stub.requests[0].url)
  assertEquals("https://api.parseapi.com/emoji?include_skin_tones=false&offset=2",stub.requests[1].url)
  val lookup=StubTransport(200,"""{"emoji":"🐱","name":"cat face","shortcodes":[],"deep":{}}""")
  val resolved=ParseAPI("test") {transport=lookup}.emoji(":cat:") {shortcodeSource="github"}
  assertEquals("https://api.parseapi.com/emoji/%3Acat%3A?shortcode_source=github",lookup.requests[0].url)
  assertNull(resolved.deep?.variants)
  val parse=StubTransport(200,"""{"emojis":[{"emoji":"👍🏿","name":"thumbs up","shortcodes":[],"category":null,"positions":[{"start":2,"end":6},{"start":7,"end":11}],"deep":{"base":"👍","variants":[{"emoji":"👍🏿","tones":["dark"]}],"shortcode_sources":{"cldr":[],"github":[],"slack":[],"discord":[]}}}]}""")
  val result=ParseAPI("test") {transport=parse}.emojiParse("x 👍🏿 👍🏿") {deep=true;lang="fr"}
  assertEquals(1,result.emojis.size);assertEquals(11,result.emojis[0].positions[1].end)
  assertEquals("👍",result.emojis[0].deep?.base);assertEquals(listOf("dark"),result.emojis[0].deep?.variants?.first()?.tones)
  assertEquals("POST",parse.requests[0].method)
  assertEquals("x 👍🏿 👍🏿",Json.parseToJsonElement(parse.requests[0].body!!).jsonObject["text"]!!.jsonPrimitive.content)
  assertEquals("https://api.parseapi.com/emoji/parse?deep=true&lang=fr",parse.requests[0].url)
  assertEquals("2.0.0",parse.requests[0].headers["Parse-Version"])
  val categories=ParseAPI("test") {transport=StubTransport(200,"""{"categories":[{"category":"People & Body","subcategories":["hand-fingers-closed"]}]}""")}.emojiCategories()
  assertEquals(listOf("hand-fingers-closed"),categories.categories[0].subcategories)
 }
 @Test fun fullDownloadMetadataAndArtwork() = runBlocking {
  val text=javaClass.getResource("/emoji-catalog.json")!!.readText()
  val stub=StubTransport(200,text)
  val client=ParseAPI("test") {transport=stub}
  val catalog=client.emojiCatalog()
  assertEquals("https://api.parseapi.com/emoji/catalog?deep=true",stub.requests[0].url)
  assertEquals("❤️",catalog.forms["❤"])
  assertEquals(1,catalog.total)
  assertEquals(true,catalog.scope?.completeInventory)
  assertEquals("fr",catalog.localization?.resolvedLocale)
  assertEquals(listOf("love"),catalog.search?.native?.get("❤️")?.keywords)
  assertEquals(listOf("❤️"),catalog.shortcodes["github"]?.get("heart"))
  assertTrue(catalog.emojis[0].deep?.artwork?.isEmpty() == true)
  assertNull(catalog.emojis[0].deep?.artwork?.get("openmoji")?.svg)
  client.emojiCatalog {deep=false;maxVersion="17.0";includeSkinTones=false}
  assertEquals("https://api.parseapi.com/emoji/catalog?deep=false&max_version=17.0&include_skin_tones=false",stub.requests[1].url)
  val metadataText=Json.parseToJsonElement(text).jsonObject["metadata"]!!.toString()
  val metadataStub=StubTransport(200,metadataText)
  val metadata=ParseAPI("test") {transport=metadataStub}.emojiMetadata()
  assertEquals(1,metadata.total)
  assertEquals("https://unicode.org/emoji",metadata.sources[0].sourceUrl)
  assertEquals("Full source notice",metadata.notices[0].notice)
  assertEquals("https://api.parseapi.com/emoji/metadata",metadataStub.requests[0].url)
 }

 @Test fun privateConversion() = runBlocking {
  val conversion=StubTransport(200,"""{"text":"🐈","unresolved":[]}""")
  assertEquals("🐈",ParseAPI("test") {transport=conversion}.emojiConvert(":cat:","to_emoji","cldr").text)
  assertEquals("POST",conversion.requests[0].method)
  assertEquals(":cat:",Json.parseToJsonElement(conversion.requests[0].body!!).jsonObject["text"]!!.jsonPrimitive.content)
 }


}
