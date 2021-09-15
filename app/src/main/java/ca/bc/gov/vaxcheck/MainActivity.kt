package ca.bc.gov.vaxcheck

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint


/**
 * [MainActivity]
 *
 * @author Pinakin Kansara
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler(Looper.getMainLooper()).postDelayed({
            setTheme(R.style.Theme_BC_VAX)
            setContentView(R.layout.activity_main)
        }, 3000)
    }
}
