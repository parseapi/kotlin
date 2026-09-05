import com.parseapi.Country
import com.parseapi.ParseAPI
import com.parseapi.ParseAPIResponse
import com.parseapi.ParseAPITransport
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() = runBlocking {
    val parse = ParseAPI("test") {
        appId = "com.example.app"
        transport = ParseAPITransport { request ->
            val body = when {
                request.url.endsWith("/country/US/states") -> """{"country":"US","states":[]}"""
                request.url.endsWith("/date/03%2F04%2F2026?format=mdy") -> """{"date":"2026-03-04","valid":true}"""
                else -> error("Unexpected SDK request: ${request.url}")
            }
            ParseAPIResponse(200, body, emptyMap())
        }
    }
    check(parse.countryStates("US").states.isEmpty())
    check(parse.date("03/04/2026") { format = "mdy" }.valid)
    // Consumers can also use the public serializer without adding hidden dependencies.
    val country = Json.decodeFromString(Country.serializer(), """{"country":"US","iso3":"USA","numeric":840,"name":"United States","continent":"NA"}""")
    check(country.name == "United States")
    println("SDK consumer passed on Java ${System.getProperty("java.version")}")
}
