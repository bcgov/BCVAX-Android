package ca.bc.gov.vaxcheck.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.bc.gov.vaxcheck.model.ImmunizationStatus
import ca.bc.gov.vaxcheck.utils.SHCDecoder
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _status: MutableLiveData<Pair<String, ImmunizationStatus>> = MutableLiveData()
    val status: LiveData<Pair<String, ImmunizationStatus>>
        get() = _status

    fun processShcUri(shcUri: String, jwks: String) {
        try {
            _status.value = shcDecoder.getImmunizationStatus(shcUri, jwks)
        } catch (e: Exception) {
            _status.value = Pair("No name found", ImmunizationStatus.INVALID_QR_CODE)
        }
    }
}
