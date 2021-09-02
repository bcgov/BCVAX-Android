package ca.bc.gov.vaxcheck.utils

import ca.bc.gov.vaxcheck.model.SHCData

class PayLoadProcessor {

    companion object {
        const val IMMUNIZATION = "Immunization"
        const val PATIENT = "Patient"
        const val JANSSEN_SNOWMED = "28951000087107" // TODO: 03/09/21 This will be removed in future
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

        var entries: List<Entry> = mutableListOf()
        entries = shcData.payload.vc.credentialSubject.fhirBundle.entry

        // CVX code and number of immunization entries will contribute in deciding the immunization logic
        var code = ""
        var numberOfImmuEntries = 0

        //Iterating through the entries to get code and number of immunization entries
        entries.forEach { entry ->
            try {
                if (entry.resource.resourceType.contentEquals(IMMUNIZATION)) {
                    code = entry.resource.vaccineCode?.coding?.get(0)?.code.toString()
                    numberOfImmuEntries += 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (entries.isEmpty()) {
            return ImmuStatus.NO_RECORD
        }

        if (code.isEmpty()) {
            return ImmuStatus.NO_RECORD
        }

        //If the vaccine is Janssen then only one dose is required
        // TODO: 02/09/21 only CVX will be considered in future
        if (code.contentEquals(JANSSEN_SNOWMED) || code.contentEquals(JANSSEN_CVX)) {
            return ImmuStatus.FULLY_IMMUNIZED
        }

        //If other vaccines then 2 doses are required
        // TODO: 02/09/21 only CVX will be considered in future
        if (!code.contentEquals(JANSSEN_SNOWMED) && !code.contentEquals(JANSSEN_CVX)) {
            if (numberOfImmuEntries == 1) {
                return ImmuStatus.PARTIALLY_IMMUNIZED
            } else {
                return ImmuStatus.FULLY_IMMUNIZED
            }
        }

        return ImmuStatus.NO_RECORD
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