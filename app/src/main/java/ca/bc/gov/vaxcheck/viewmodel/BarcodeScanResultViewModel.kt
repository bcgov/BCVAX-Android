package ca.bc.gov.vaxcheck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bc.gov.shcdecoder.SHCVerifier
import ca.bc.gov.shcdecoder.model.ImmunizationRecord
import ca.bc.gov.shcdecoder.model.ImmunizationStatus
import ca.bc.gov.shcdecoder.model.SHCData
import ca.bc.gov.shcdecoder.model.VaccinationStatus
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
    private val bcCardVerifier: SHCVerifier
) : ViewModel() {

    private val _status = MutableSharedFlow<Pair<VaccinationStatus,SHCData?>>()
    val status: SharedFlow<Pair<VaccinationStatus,SHCData?>> = _status.shareIn(
        scope = viewModelScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed(5000)
    )

    fun processShcUri(shcUri: String) = viewModelScope.launch {

        withContext(Dispatchers.IO) {
            try {
                if (bcCardVerifier.hasValidSignature(shcUri)) {
                    _status.emit(bcCardVerifier.getStatus(shcUri))
                }
            } catch (e: Exception) {
                Log.e("Error", e.message.toString())
                _status.emit(
                    Pair(VaccinationStatus.INVALID,null)
                )
            }
        }
    }
}
