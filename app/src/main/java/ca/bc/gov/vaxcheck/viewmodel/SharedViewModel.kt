package ca.bc.gov.vaxcheck.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.bc.gov.shcdecoder.model.SHCData
import ca.bc.gov.shcdecoder.model.VaccinationStatus
import ca.bc.gov.vaxcheck.data.local.DataStoreRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *[SharedViewModel]
 *
 * @author Amit Metri
 */
@HiltViewModel
class SharedViewModel @Inject constructor(
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    val isOnBoardingShown = dataStoreRepo.isOnBoardingShown.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    private val _status: MutableLiveData<Pair<VaccinationStatus, SHCData?>> = MutableLiveData()
    val status: LiveData<Pair<VaccinationStatus, SHCData?>>
        get() = _status

    fun setStatus(status: Pair<VaccinationStatus, SHCData?>) {
        _status.value = status
    }

    fun setOnBoardingShown(shown: Boolean) = viewModelScope.launch {
        dataStoreRepo.setOnBoardingShown(shown)
    }
}
