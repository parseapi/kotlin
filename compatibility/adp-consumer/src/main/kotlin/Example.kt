import com.parseapi.Country
import com.parseapi.ParseAPI
import com.parseapi.ParseAPIResponse
import com.parseapi.ParseAPITransport
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() = runBlocking {
    val paths = mutableListOf<String>()
    val parse = ParseAPI("test") {
        transport = ParseAPITransport { request ->
            paths += request.url
            val body = when {
                request.url.endsWith("/country/US?deep=true") ->
                    """{"country":"US","name":"United States","continent":"NA","deep":{"iso3":"USA","numeric":840,"tax_rate":0}}"""
                request.url.endsWith("/state/NC/districts?country=US&deep=true") ->
                    """{"state":"NC","country":"US","districts":[{"district":"37081","name":"Guilford","deep":{"population":0}},{"district":"37001","name":"Alamance","deep":{}}]}"""
                else -> error("Unexpected SDK request: ${request.url}")
            }
            ParseAPIResponse(200, body, emptyMap())
        }
    }
    check(parse.country("US") { deep = true }.deep?.taxRate == 0.0)
    val districts = parse.stateDistricts("NC") { country = "US"; deep = true }.districts
    check(districts[0].deep?.population == 0L)
    check(districts[1].deep != null && districts[1].deep?.population == null)
    check(paths.size == 2)

    // Public serializers decode the candidate contract without SDK-private settings.
    val core = Json.decodeFromString(Country.serializer(), """{"country":"US","name":"United States","continent":"NA"}""")
    val locked = Json.decodeFromString(Country.serializer(), """{"country":"US","name":"United States","continent":"NA","deep":{}}""")
    val rich = Json.decodeFromString(Country.serializer(), """{"country":"US","name":"United States","continent":"NA","deep":{"iso3":"USA","numeric":840,"population":0}}""")
    check(core.deep == null && core.name == "United States")
    check(locked.deep != null && locked.deep?.iso3 == null)
    check(rich.deep?.iso3 == "USA" && rich.deep?.numeric == 840 && rich.deep?.population == 0L)
    println("ADP consumer passed on Java ${System.getProperty("java.version")}")
}
