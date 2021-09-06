package ca.bc.gov.vaxcheck.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ca.bc.gov.vaxcheck.model.ImmunizationStatus
import ca.bc.gov.vaxcheck.utils.DataStoreRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *[SharedViewModel]
 *
 * @author Amit Metri
 */
class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreRepo = DataStoreRepo(application.applicationContext)

    private val _status: MutableLiveData<Pair<String, ImmunizationStatus>> = MutableLiveData()
    val status: LiveData<Pair<String, ImmunizationStatus>>
        get() = _status

    fun isOnBoardingShown(key: String): LiveData<Boolean> {
        return dataStoreRepo.readFromDataStore(key).asLiveData()
    }

    fun writeFirstLaunch(key: String, value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.saveToDataStore(key, value)
        }
    }

    fun setStatus(status: Pair<String, ImmunizationStatus>) {
        _status.value = status
    }
}