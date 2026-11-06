import com.parseapi.Country
import com.parseapi.Email
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
            check(request.headers["X-API-Version"] == "2.1.0")
            check("Parse-Version" !in request.headers)
            val body = when {
                request.url.endsWith("/country/US?deep=true") ->
                    """{"country":"US","name":"United States","continent":"NA","iso_alpha3":"USA","iso_numeric":840,"tax":{"name":"Sales tax","rate":0}}"""
                request.url.endsWith("/state/NC/districts?country=US&deep=true") ->
                    """{"state":"NC","country":"US","districts":[{"district":"37081","name":"Guilford","country":"US","population":0},{"district":"37001","name":"Alamance","country":"US"}]}"""
                request.url.endsWith("/email/verify") -> {
                    check(request.method == "POST")
                    check(Json.parseToJsonElement(request.body ?: error("missing verify body")) ==
                        Json.parseToJsonElement("""{"email":"fixture@example.com"}"""))
                    """{"email":"fixture@example.com","valid":true,"role":false,"disposable":false,"deliverable":null,"catchall":false}"""
                }
                else -> error("Unexpected SDK request: ${request.url}")
            }
            ParseAPIResponse(200, body, emptyMap())
        }
    }
    check(parse.country("US") { deep = true }.tax?.rate == 0.0)
    val districts = parse.stateDistricts("NC") { country = "US"; deep = true }.districts
    check(districts[0].population == 0L)
    check(districts[1].population == null)
    val verified = parse.emailVerify("fixture@example.com")
    check(verified.deliverable == null && verified.catchall == false)
    check(paths.size == 3)

    // Public serializers decode the candidate contract without SDK-private settings.
    val core = Json.decodeFromString(Country.serializer(), """{"country":"US","name":"United States","continent":"NA"}""")
    val absent = Json.decodeFromString(Country.serializer(), """{"country":"US","name":"United States","continent":"NA","iso_alpha3":null,"population":null}""")
    val rich = Json.decodeFromString(Country.serializer(), """{"country":"US","name":"United States","continent":"NA","iso_alpha3":"USA","iso_numeric":840,"population":0}""")
    check(core.iso3 == null && core.population == null && core.name == "United States")
    check(absent.iso3 == null && absent.population == null)
    check(rich.iso3 == "USA" && rich.numeric == 840 && rich.population == 0L)
    val missing = Json.decodeFromString(Email.serializer(), """{"email":"fixture@example.com","valid":false,"role":false,"disposable":false}""")
    val negative = Json.decodeFromString(Email.serializer(), """{"email":"fixture@example.com","valid":false,"role":false,"disposable":false,"deliverable":false,"catchall":false}""")
    check(missing.deliverable == null && missing.catchall == null && !missing.valid)
    check(negative.deliverable == false && negative.catchall == false && !negative.valid)
    println("ADP consumer passed on Java ${System.getProperty("java.version")}")
}
