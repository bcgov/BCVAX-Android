package ca.bc.gov.shcdecoder.model

import com.google.gson.annotations.SerializedName

data class TrustedIssuersResponse(
    @SerializedName("participating_issuers")
    val trustedIssuers: List<Issuer>
)