package ca.bc.gov.health.ircreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.bc.gov.health.ircreader.utils.SHCDecoder

class BarcodeScanResultViewModel : ViewModel() {

    private val userName: MutableLiveData<String> = MutableLiveData()
    private val vaccinationStatus: MutableLiveData<Int> = MutableLiveData()

    fun observeUserName(): MutableLiveData<String> {
        return userName
    }

    fun observeVaccinationStatus(): MutableLiveData<Int> {
        return vaccinationStatus
    }

    fun processShcUri(shcUri: String) {
        SHCDecoder().decode(shcUri, onSuccess = {
            println("SHC ${it.payload}")
        }, onError = {
            println("SHC ${it.message}")
        })
        // TODO: 01/09/21 Need to process the decoded data and update live data 
        userName.value = "Dummy User"
        vaccinationStatus.value = 2
    }
}