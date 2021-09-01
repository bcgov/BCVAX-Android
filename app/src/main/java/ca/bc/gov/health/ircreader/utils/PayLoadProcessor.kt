package ca.bc.gov.health.ircreader.utils

import ca.bc.gov.health.ircreader.model.Entry
import ca.bc.gov.health.ircreader.model.SHCData
import com.google.gson.Gson
import java.lang.Exception
import java.time.LocalDate

class PayLoadProcessor {

    /*fun processPayLoad(shcData: SHCData): Int {
        println(Gson().toJson(shcData))

        var entries: List<Entry> = mutableListOf()
        entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

        var code: String?
        var occurrenceDateTime: String = ""
        entries.forEach { entry ->
            if (entry.resource.resourceType.contentEquals("Immunization")) {
                code = entry.resource.vaccineCode?.coding?.get(0)?.code
                if (occurrenceDateTime.isEmpty()
                    || LocalDate.parse(occurrenceDateTime)
                        .isBefore(LocalDate.parse(entry.resource.occurrenceDateTime.toString()))
                )
                    occurrenceDateTime = entry.resource.occurrenceDateTime.toString()
            }
            // TODO: 01/09/21 In progress
            if (entry.resource.resourceType.contentEquals("Patient")) {

            }
        }

        return 0
    }*/

    fun fetchName(shcData: SHCData): String {
        var entries: List<Entry> = mutableListOf()
        entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

        var name: String = ""
        var familyName: String = ""

        try {
            entries.forEach { entry ->
                if (entry.resource.resourceType.contentEquals("Patient")) {
                    name = entry.resource.name?.get(0)?.given?.get(0).toString() + " "
                    familyName = entry.resource.name?.get(0)?.family.toString()
                }
            }
        } catch (e : Exception){
            e.printStackTrace()
        }

        return if (name.isEmpty()) {
            "Dummy name"
        } else {
            "$name $familyName"
        }
    }
}