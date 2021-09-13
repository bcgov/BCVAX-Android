package ca.bc.gov.vaxcheck.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ca.bc.gov.vaxcheck.data.local.DataStoreRepo
import ca.bc.gov.vaxcheck.model.ImmunizationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *[SharedViewModel]
 *
 * @author Amit Metri
 */
@HiltViewModel
class SharedViewModel @Inject constructor(private val dataStoreRepo: DataStoreRepo) : ViewModel() {

    private val _status: MutableLiveData<Pair<String, ImmunizationStatus>> = MutableLiveData()
    val status: LiveData<Pair<String, ImmunizationStatus>>
        get() = _status

    fun isOnBoardingShown(): LiveData<Boolean> {
        return dataStoreRepo.isOnBoardingShown.asLiveData()
    }

    fun writeFirstLaunch() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.writeFirstLaunch()
        }
    }

    fun setStatus(status: Pair<String, ImmunizationStatus>) {
        _status.value = status
    }
}
