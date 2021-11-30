package ca.bc.gov.shcdecoder.rule

import ca.bc.gov.shcdecoder.model.Rule

interface RulesManager {

    suspend fun getRule(iss: String): Rule?
}
