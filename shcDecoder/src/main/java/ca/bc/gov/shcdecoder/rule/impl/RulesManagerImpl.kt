package ca.bc.gov.shcdecoder.rule.impl

import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.model.Rule
import ca.bc.gov.shcdecoder.rule.RulesManager

class RulesManagerImpl(
    private val shcConfig: SHCConfig,
    private val fileManager: FileManager
) : RulesManager {

    override suspend fun getRule(iss: String): Rule? {
        try {
            val rules = fileManager.getRule(shcConfig.rulesEndPoint)

            var ruleSet: Rule? = null
            rules.forEach { rule ->
                when {
                    rule.ruleTarget.endsWith("issuers.json") -> {
                        val issuers = fileManager.getIssuers(rule.ruleTarget)
                        val issuerUrls = issuers.map { issuer -> issuer.iss }
                        if (issuerUrls.contains(iss)) {
                            ruleSet = rule
                        }
                    }

                    rule.ruleTarget.startsWith(iss) -> {
                        ruleSet = rule
                    }
                }
            }
            return ruleSet ?: getDefaultRule(iss)
        }catch (e: Exception){
            return getDefaultRule(iss)
        }
    }

    private fun getDefaultRule(iss: String): Rule? {
        var ruleSet: Rule? = null
        shcConfig.defaultRules.forEach { rule ->
            when {
                rule.ruleTarget.endsWith("issuers.json") -> {
                    val issUrls =
                        shcConfig.defaultKeys.map { defaultJWKSKeys -> defaultJWKSKeys.iss }
                    if (issUrls.contains(iss)) {
                        ruleSet = rule
                    }
                }

                rule.ruleTarget.startsWith(iss) -> {
                    ruleSet = rule
                }
            }
        }

        return ruleSet
    }
}