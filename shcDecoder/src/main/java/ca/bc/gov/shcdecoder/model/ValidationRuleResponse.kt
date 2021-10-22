package ca.bc.gov.shcdecoder.model

/**
 * @author Pinakin Kansara
 */
data class ValidationRuleResponse(
    val publishDateTime: String,
    val ruleSet: List<Rule>
)
