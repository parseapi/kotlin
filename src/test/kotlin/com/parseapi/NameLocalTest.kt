package com.parseapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

@OptIn(ExperimentalSerializationApi::class)
class NameLocalTest {
 @Test fun decodesEveryNativeNameMember() {
  val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
  val base = """{"country":"DE","state":"BY","name":"Munich","continent":"EU","language":"de","direction":"ltr","date":"2026-12-25","type":"public","regions":[],"substitute":false,"id":"city_test","distance":0,"distance_mi":0}"""
  for (suffix in listOf(""","name_local":"München"}""", ""","name_local":null}""", "}")) {
   val body = base.dropLast(1) + suffix
   val expected = if (suffix.contains("München")) "München" else null
   assertEquals(expected, json.decodeFromString<Country>(body).nameLocal)
   assertEquals(expected, json.decodeFromString<State>(body).nameLocal)
   assertEquals(expected, json.decodeFromString<City>(body).nameLocal)
   assertEquals(expected, json.decodeFromString<CityNearest>(body).nameLocal)
   assertEquals(expected, json.decodeFromString<Language>(body).nameLocal)
   assertEquals(expected, json.decodeFromString<Holiday>(body).nameLocal)
   assertEquals(expected, json.decodeFromString<PointCity>(body).nameLocal)
  }
 }
}
