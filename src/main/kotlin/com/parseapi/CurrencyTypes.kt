package com.parseapi

import kotlinx.serialization.Serializable

@Serializable
class CurrencyRateDates private constructor(val base: List<String>? = null, val quote: List<String>? = null)
@Serializable
class CurrencyRateUnits private constructor(val base: String? = null, val quote: String? = null)
@Serializable
class CurrencyMissingQuote private constructor(val quote: String, val reason: String)
@Serializable
class CurrencyHistoryMissing private constructor(val requestedDate: String, val quote: String, val reason: String)
@Serializable
class CurrencyQuote private constructor(
 val rate: Double, val date: String, val converted: Double? = null,
 val stale: Boolean? = null,
 val dates: CurrencyRateDates? = null, val units: CurrencyRateUnits? = null,
)
@Serializable
class CurrencyObservation private constructor(val requestedDate: String,
 val rate: Double, val date: String, val converted: Double? = null,
 val stale: Boolean? = null,
 val dates: CurrencyRateDates? = null, val units: CurrencyRateUnits? = null,
)
@Serializable
class CurrencyQuoteObservation private constructor(val quote: String, val requestedDate: String,
 val rate: Double, val date: String, val converted: Double? = null,
 val stale: Boolean? = null,
 val dates: CurrencyRateDates? = null, val units: CurrencyRateUnits? = null,
)
@Serializable
class CurrencyRates private constructor(
 val base: String, val amount: Double? = null, val requestedDate: String? = null,
 val rates: Map<String, CurrencyQuote>, val missing: List<CurrencyMissingQuote> = emptyList(),
)
@Serializable
class CurrencyHistory private constructor(
 val base: String, val quote: String, val from: String, val to: String, val amount: Double? = null,
 val data: List<CurrencyObservation>, val missing: List<String> = emptyList(),
 val interval: String? = null,
)
@Serializable
class CurrencyRatesHistory private constructor(
 val base: String, val quotes: List<String> = emptyList(), val from: String, val to: String, val amount: Double? = null,
 val data: List<CurrencyQuoteObservation>, val missing: List<CurrencyHistoryMissing> = emptyList(),
 val interval: String? = null,
)

@Serializable
class CurrencyResource private constructor(val currency: String, val name: String? = null, val symbol: String? = null, val symbolNative: String? = null, val digits: Int? = null, val deep: CurrencyDeep? = null, val unit: String? = null)
@Serializable
class CurrencyList private constructor(val data: List<CurrencyResource>)

@Serializable
class CurrencySummaryRow private constructor(
 val quote: String, val from: String, val to: String,
 val average: Double, val min: Double, val max: Double, val observationCount: Int,
 val firstDate: String, val lastDate: String, val missingDayCount: Int,
 val stale: Boolean? = null, val units: CurrencyRateUnits? = null,
)
@Serializable
class CurrencySummaryMissing private constructor(val quote: String, val from: String, val to: String, val reason: String, val missingDayCount: Int)
@Serializable
class CurrencyRatesSummary private constructor(
 val data: List<CurrencySummaryRow>, val missing: List<CurrencySummaryMissing> = emptyList(),
 val base: String, val quotes: List<String>, val from: String, val to: String, val interval: String,
)
