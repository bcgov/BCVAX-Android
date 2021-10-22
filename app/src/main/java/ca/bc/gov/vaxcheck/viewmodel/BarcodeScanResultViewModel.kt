package ca.bc.gov.vaxcheck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bc.gov.shcdecoder.BcCardVerifier
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.model.ImmunizationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [BarcodeScanResultViewModel]
 *
 *
 * @author Pinakin Kansara
 */
@HiltViewModel
class BarcodeScanResultViewModel @Inject constructor(
    private val bcCardVerifier: BcCardVerifier
) : ViewModel() {

    private val _status = MutableSharedFlow<ImmunizationRecord>()
    val status: SharedFlow<ImmunizationRecord> = _status.shareIn(
        scope = viewModelScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun processShcUri(shcUri: String) = viewModelScope.launch {

        withContext(Dispatchers.IO) {
            try {
                _status.emit(bcCardVerifier.verify(shcUri))
            } catch (e: Exception) {
                Log.e("Error", e.message.toString())
                _status.emit(
                    ImmunizationRecord(
                        "record.first()",
                        "record.first().second",
                        ImmunizationStatus.INVALID_QR_CODE
                    )
                )
            }
        }
    }
}
