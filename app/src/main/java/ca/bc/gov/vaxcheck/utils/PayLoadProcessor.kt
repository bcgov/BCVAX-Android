package ca.bc.gov.vaxcheck.utils

import ca.bc.gov.vaxcheck.model.SHCData

class PayLoadProcessor {

    companion object {
        const val IMMUNIZATION = "Immunization"
        const val PATIENT = "Patient"
        const val JANSSEN_SNOWMED =
            "28951000087107" // TODO: 03/09/21 This will be removed in future
        const val JANSSEN_CVX = "212"
    }

    enum class ImmuStatus {
        FULLY_IMMUNIZED,
        PARTIALLY_IMMUNIZED,
        NO_RECORD;
    }

    /*
    * Below code snippet provides immunization status
    * No immunization entry is no records
    * Jannssen 1 dose is fully immunized
    * Other vaccines 2 doses is fully immunized
    * Other vaccines 1 dose is partially immunized
    * */
    fun fetchImmuStatus(shcData: SHCData): ImmuStatus {
        println(Gson().toJson(shcData))

        var vaccines = 0
        var onDoseVaccines = 0

        shcData.payload.vc.credentialSubject.fhirBundle.entry.forEach { entry ->
            try {
                if (entry.resource.resourceType.contentEquals(IMMUNIZATION)) {
                    val code: String = entry.resource.vaccineCode?.coding?.get(0)?.code.toString()
                    if (code.contentEquals(JANSSEN_CVX) || code.contentEquals(JANSSEN_SNOWMED)) {
                        onDoseVaccines++
                    } else {
                        vaccines++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return if (onDoseVaccines > 0 || vaccines > 1)
            ImmuStatus.FULLY_IMMUNIZED
        else if (vaccines > 0)
            ImmuStatus.PARTIALLY_IMMUNIZED
        else
            ImmuStatus.NO_RECORD
    }

    /*
    * Below code snippet provides user name
    * */
    fun fetchName(shcData: SHCData): String {
        var entries: List<Entry> = mutableListOf()
        entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

        var name = ""

        try {
            entries.forEach { entry ->
                if (entry.resource.resourceType.contentEquals(PATIENT)) {
                    name = entry.resource.name?.get(0)?.given?.get(0).toString().plus(" ")
                        .plus(entry.resource.name?.get(0)?.family.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (name.isEmpty()) {
            "Name not found!"
        } else {
            name
        }
    }
}
