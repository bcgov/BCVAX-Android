package ca.bc.gov.shcdecoder.rule

import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.SUFFIX_ISSUERS
import ca.bc.gov.shcdecoder.TEST_ISS
import ca.bc.gov.shcdecoder.cache.FileManager
import ca.bc.gov.shcdecoder.config
import ca.bc.gov.shcdecoder.defaultRule
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.rule.impl.RulesManagerImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RulesManagerImplTest {

    private lateinit var sut: RulesManager

    @Mock
    private lateinit var fileManager: FileManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        prepareSystemUnderTest(config)
    }

    @Test
    fun onGetRule_ruleTargetWithIss_returnsCorrectRuleSet(): Unit = runBlocking {
        prepareFileManager()
        val resultRule = sut.getRule(TEST_ISS)
        assertEquals(resultRule?.ruleTarget.orEmpty(), TEST_ISS)
    }

    @Test
    fun onGetRule_ruleTargetWithIssuerSuffix_returnsCorrectRuleSet(): Unit = runBlocking {
        val issuer = TEST_ISS.plus(SUFFIX_ISSUERS)
        prepareFileManager(ruleTarget = issuer)
        val resultRule = sut.getRule(TEST_ISS)
        Mockito.verify(fileManager).getIssuers(anyString())
        assertEquals(resultRule?.ruleTarget.orEmpty(), issuer)
    }

    @Test
    fun onGetRule_exceptionIsThrew_returnsDefaultRuleSet(): Unit = runBlocking {
        val resultRule = sut.getRule(TEST_ISS)
        assertEquals(resultRule?.ruleTarget.orEmpty(), TEST_ISS)
    }

    @Test
    fun onGetRule_exceptionIsThrewAndDefaultRuleHasIssuerSuffix_returnsDefaultRuleSet(): Unit = runBlocking {
        val issuer = TEST_ISS.plus(SUFFIX_ISSUERS)
        prepareSystemUnderTest(config = config.copy(
            defaultRules = listOf(
                defaultRule.copy(
                    ruleTarget = issuer
                )
            )
        ))
        val resultRule = sut.getRule(TEST_ISS)
        assertEquals(resultRule?.ruleTarget.orEmpty(), issuer)
    }

    private fun prepareSystemUnderTest(config: SHCConfig) {
        sut = RulesManagerImpl(
            config,
            fileManager
        )
    }

    private fun prepareFileManager(ruleTarget: String = TEST_ISS): Unit = runBlocking {
        Mockito.doReturn(
            listOf(
                defaultRule.copy(
                    ruleTarget = ruleTarget
                )
            )
        ).`when`(fileManager).getRule(anyString())

        Mockito.doReturn(
            listOf(
                Issuer(
                    iss = TEST_ISS,
                    name = "Dev Freshworks"
                )
            )
        ).`when`(fileManager).getIssuers(anyString())
    }

}