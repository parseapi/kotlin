package com.parseapi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.test.*
@OptIn(ExperimentalSerializationApi::class)
class IpDeepExpansionTest {
 @Test fun additiveFacts(){val codec=Json{ignoreUnknownKeys=true;namingStrategy=JsonNamingStrategy.SnakeCase};val value=codec.decodeFromString<Ip>("""{"ip":"198.51.100.10","country":"XX","country_name":"Example","continent":"NA","asn":"AS64512","asn_name":"Fixture","city":"Example","state":"EX","state_name":"Example state","postal":"00123","latitude":0,"longitude":-0.000001,"timezone":"Etc/UTC","network":"198.51.100.0/24","asn_type":"education","registry":"arin","datacenter":false,"relay":false,"tor":false,"vpn":false,"proxy":false,"provider":null}""");val d=value;assertEquals(0.0,d.latitude);assertEquals(-0.000001,d.longitude);assertEquals("00123",d.postal);assertEquals(false,d.proxy);assertEquals("Example state",d.stateName);assertEquals("Etc/UTC",d.timezone);assertEquals("198.51.100.0/24",d.network);assertEquals("education",d.asnType);for(text in listOf("""{"ip":"198.51.100.10"}""","""{"ip":"198.51.100.10"}""","""{"ip":"198.51.100.10","latitude":null,"longitude":null,"proxy":null}""")){val old=codec.decodeFromString<Ip>(text);assertNull(old?.latitude);assertNull(old?.proxy)}}
}
