package ca.bc.gov.shcdecoder.repository

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class PreferenceRepositoryTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var preferenceRepository: PreferenceRepository

    @Before
    fun setup() {
        preferenceRepository = PreferenceRepository(context)
    }


    @Test
    fun setAndGetTimeStampSuccess() = runBlocking {

        val timeStamp = Calendar.getInstance().timeInMillis

        preferenceRepository.setTimeStamp(timeStamp)

        assertEquals(timeStamp, preferenceRepository.timeStamp.first())
    }

    @Test
    fun setAndGetTimeStampFailure() = runBlocking {

        val timeStamp = Calendar.getInstance().timeInMillis

        val timeToStore = timeStamp + 5000L

        preferenceRepository.setTimeStamp(timeToStore)

        assertNotEquals(timeStamp, preferenceRepository.timeStamp)
    }
}