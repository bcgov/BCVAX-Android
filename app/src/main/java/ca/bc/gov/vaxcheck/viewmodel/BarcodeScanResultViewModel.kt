package ca.bc.gov.vaxcheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bc.gov.vaxcheck.model.ImmunizationStatus
import ca.bc.gov.vaxcheck.utils.SHCDecoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [BarcodeScanResultViewModel]
 *
 *
 * @author Pinakin Kansara
 */
@HiltViewModel
class BarcodeScanResultViewModel @Inject constructor(
    private val shcDecoder: SHCDecoder
) : ViewModel() {

    private val _status = MutableSharedFlow<Pair<String, ImmunizationStatus>>()
    val status: SharedFlow<Pair<String, ImmunizationStatus>> = _status.shareIn(
        scope = viewModelScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed(5000)
    )


    fun processShcUri(shcUri: String, jwks: String) = viewModelScope.launch {
        try {
            _status.emit(shcDecoder.getImmunizationStatus(shcUri, jwks))
        } catch (e: Exception) {
            _status.emit(Pair("No name found", ImmunizationStatus.INVALID_QR_CODE))
        }
    }

}
