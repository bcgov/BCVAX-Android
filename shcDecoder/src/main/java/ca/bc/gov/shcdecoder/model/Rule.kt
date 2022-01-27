package ca.bc.gov.shcdecoder.model

/**
 *
 * @author Pinakin Kansara
 */
data class Rule(
    val mixTypesAllowed: Boolean,
    val mixTypesRuRequired: Int,
    val ruRequired: Int,
    val intervalRequired: Boolean,
    val daysSinceLastInterval: Int,
    val ruleTarget: String,
    val version: String,
    val vaccinationRules: List<VaccineRule>,
    val exemptions: List<Exemptions?>?
)
