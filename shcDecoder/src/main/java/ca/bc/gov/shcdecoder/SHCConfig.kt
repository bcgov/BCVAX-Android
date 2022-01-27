package ca.bc.gov.shcdecoder

import ca.bc.gov.shcdecoder.model.DefaultJWKSKeys
import ca.bc.gov.shcdecoder.model.Rule

data class SHCConfig(
    val issuerEndPoint: String,
    val rulesEndPoint: String,
    val defaultKeys: List<DefaultJWKSKeys>,
    val defaultRules: List<Rule>,
    val cacheExpiryTimeInMilli: Long
)
