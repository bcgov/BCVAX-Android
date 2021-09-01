package ca.bc.gov.health.ircreader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.bc.gov.health.ircreader.utils.PayLoadProcessor
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
            userName.value = PayLoadProcessor().fetchName(it)
            vaccinationStatus.value = 2 // TODO: 01/09/21 hard coded value for testing 
        }, onError = {
            println("SHC ${it.message}")
        })
    }
}