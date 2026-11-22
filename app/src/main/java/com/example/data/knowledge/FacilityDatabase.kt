package com.example.data.knowledge

import com.example.data.model.FacilityType
import com.example.data.model.HealthcareFacility

object FacilityDatabase {

    val facilities = listOf(
        HealthcareFacility(
            id = "fac_1",
            name = "Community Health Centre (CHC) Rampur",
            type = FacilityType.CHC,
            distanceKm = 2.4,
            address = "Main Hospital Road, Near Tehsil Office, Rampur",
            phone = "108",
            isOpen24x7 = true,
            services = listOf("24x7 Emergency", "OPD", "Maternity & Delivery", "Labor Room", "Pathology Lab", "Jan Aushadhi Pharmacy", "Ambulance"),
            latitude = 28.8030,
            longitude = 79.0250,
            isEmergencyCapable = true
        ),

        HealthcareFacility(
            id = "fac_2",
            name = "Primary Health Centre (PHC) Chandpur",
            type = FacilityType.PHC,
            distanceKm = 4.1,
            address = "Gram Panchayat Bhawan Road, Chandpur Village",
            phone = "+91 595 2341001",
            isOpen24x7 = false,
            services = listOf("General OPD", "Immunization", "Maternal Care", "Basic First Aid", "Free Generic Medicines"),
            latitude = 28.8150,
            longitude = 79.0400,
            isEmergencyCapable = false
        ),

        HealthcareFacility(
            id = "fac_3",
            name = "District Civil Hospital & Trauma Centre",
            type = FacilityType.DISTRICT_HOSPITAL,
            distanceKm = 12.8,
            address = "Civil Lines, Near Collectorate, District Headquarters",
            phone = "112",
            isOpen24x7 = true,
            services = listOf("24x7 Trauma & Critical Care", "ICU", "Blood Bank", "Surgical Theatre", "Pediatric Ward", "CT Scan & X-Ray", "Dialysis"),
            latitude = 28.7900,
            longitude = 79.0100,
            isEmergencyCapable = true
        ),

        HealthcareFacility(
            id = "fac_4",
            name = "Ayushman Arogya Mandir (Health & Wellness)",
            type = FacilityType.AYUSHMAN_MANDIR,
            distanceKm = 1.2,
            address = "Sub-Centre Campus, Shivpur Ward 3",
            phone = "+91 595 2349090",
            isOpen24x7 = false,
            services = listOf("CHO Consultation", "Tele-consultation with MD", "NCD Screening (BP/Sugar)", "Yoga & Wellness", "Essential Drug Dispensing"),
            latitude = 28.8080,
            longitude = 79.0310,
            isEmergencyCapable = false
        ),

        HealthcareFacility(
            id = "fac_5",
            name = "Pradhan Mantri Jan Aushadhi Kendra (Block #4)",
            type = FacilityType.JAN_AUSHADHI,
            distanceKm = 2.5,
            address = "Shop 12, Bus Stand Complex, Main Bazaar",
            phone = "+91 98765 43210",
            isOpen24x7 = false,
            services = listOf("Affordable Generic Medicines (50-90% discount)", "ORS & Zinc Packets", "Surgical Bandages", "Diabetic Supplies"),
            latitude = 28.8040,
            longitude = 79.0260,
            isEmergencyCapable = false
        ),

        HealthcareFacility(
            id = "fac_6",
            name = "24x7 Emergency Trauma Unit & Ambulance Base",
            type = FacilityType.EMERGENCY_TRAUMA,
            distanceKm = 11.5,
            address = "National Highway 24 Bypass Junction",
            phone = "108",
            isOpen24x7 = true,
            services = listOf("24x7 Advanced Life Support Ambulance", "Severe Trauma Resuscitation", "Accident Care", "Snakebite Anti-venom Available"),
            latitude = 28.7820,
            longitude = 79.0190,
            isEmergencyCapable = true
        )
    )

    fun getSortedFacilities(typeFilter: FacilityType? = null): List<HealthcareFacility> {
        return if (typeFilter == null) {
            facilities.sortedBy { it.distanceKm }
        } else {
            facilities.filter { it.type == typeFilter }.sortedBy { it.distanceKm }
        }
    }
}
