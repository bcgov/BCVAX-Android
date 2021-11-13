package ca.bc.gov.shcdecoder

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
open abstract class DecoderTest {

    abstract fun prepare()
    abstract fun clear()

    @Before
    fun setup() {
        prepare()
    }

    @After
    fun tearDown() {
        clear()
    }
}
