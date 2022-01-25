package ca.bc.gov.shcdecoder.cache

import android.util.Log
import ca.bc.gov.shcdecoder.TEST_ISS
import ca.bc.gov.shcdecoder.TEST_ISS_WITH_SUFFIX
import ca.bc.gov.shcdecoder.cache.impl.CacheManagerImpl
import ca.bc.gov.shcdecoder.config
import ca.bc.gov.shcdecoder.model.Issuer
import ca.bc.gov.shcdecoder.repository.PreferenceRepository
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.internal.verification.Times
import org.mockito.junit.MockitoJUnitRunner
import java.util.Calendar

@RunWith(MockitoJUnitRunner::class)
class CacheManagerImplTest {

    private lateinit var sut: CacheManager

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @Mock
    private lateinit var fileManager: FileManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockkStatic(Log::class)
        sut = CacheManagerImpl(
            config,
            preferenceRepository,
            fileManager
        )
    }

    @Test
    fun `given fetch when cache is not expired then do nothing`(): Unit = runBlocking {
        prepareDependencies(isCacheExpired = false)
        sut.fetch()
        Mockito.verifyNoInteractions(fileManager)
    }

    @Test
    fun `given fetch when issuers are correct then time stamp is set and file downloaded`(): Unit = runBlocking {
        prepareDependencies()
        sut.fetch()
        verify(preferenceRepository).setTimeStamp(anyLong())
        verify(fileManager, Times(3)).downloadFile(anyString())
    }

    @Test
    fun `given fetch when issuers are correct and have defined suffix then time stamp is set and file downloaded`(): Unit = runBlocking {
        prepareDependencies()
        sut.fetch()
        verify(preferenceRepository).setTimeStamp(anyLong())
        verify(fileManager, Times(3)).downloadFile(anyString())
    }

    private fun prepareDependencies(isCacheExpired: Boolean = true, isIssuerWithSuffix: Boolean = false): Unit = runBlocking {
        doReturn(
            flow {
                emit (
                    Calendar.getInstance().apply {
                       if (isCacheExpired) {
                           set(2000, 1, 1)
                       } else {
                           set(999999, 1, 1)
                       }
                    }.timeInMillis
                )
            }
        ).`when`(preferenceRepository).timeStamp

        doReturn(
            listOf(
                Issuer(
                    iss = if (isIssuerWithSuffix) TEST_ISS_WITH_SUFFIX else TEST_ISS,
                    name = "Dev Freshworks"
                )
            )
        ).`when`(fileManager).getIssuers(anyString())

        every { Log.e(any(), any(), any()) } returns 0
    }

}