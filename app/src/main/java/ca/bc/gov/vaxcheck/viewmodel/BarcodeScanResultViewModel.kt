package ca.bc.gov.vaxcheck.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.bc.gov.vaxcheck.model.SHCData
import ca.bc.gov.vaxcheck.utils.PayLoadProcessor

class BarcodeScanResultViewModel : ViewModel() {

    private val userName: MutableLiveData<String> = MutableLiveData()
    private val vaccinationStatus: MutableLiveData<PayLoadProcessor.ImmuStatus> = MutableLiveData()

    fun observeUserName(): MutableLiveData<String> {
        return userName
    }

    fun observeVaccinationStatus(): MutableLiveData<PayLoadProcessor.ImmuStatus> {
        return vaccinationStatus
    }

    fun processShcUri(shcData: SHCData) {
        userName.value = PayLoadProcessor().fetchName(shcData)
        vaccinationStatus.value = PayLoadProcessor().fetchImmuStatus(shcData)
    }
}
