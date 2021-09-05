package ca.bc.gov.vaxcheck.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ca.bc.gov.vaxcheck.utils.DataStoreRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreRepo = DataStoreRepo(application.applicationContext)

    fun isOnBoardingShown(key: String): LiveData<Boolean> {
        return dataStoreRepo.readFromDataStore(key).asLiveData()
    }

    fun writeFirstLaunch(key: String , value: Boolean){
        viewModelScope.launch(Dispatchers.IO){
            dataStoreRepo.saveToDataStore(key, value)
        }
    }
}