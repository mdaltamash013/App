package com.example.data.knowledge

import com.example.data.model.DoctorInfo

object DoctorDatabase {

    val doctors = listOf(
        DoctorInfo(
            id = "doc_1",
            name = "Dr. Rajeshwar Sharma",
            specialty = "General Physician & Rural Health Specialist",
            experienceYears = 14,
            qualifications = "MBBS, MD (Internal Medicine) — AIIMS New Delhi",
            languages = listOf("Hindi", "English"),
            isAvailableNow = true,
            consultationModes = listOf("Voice Call", "Video Call", "Chat")
        ),
        DoctorInfo(
            id = "doc_2",
            name = "Dr. Sunita Patel",
            specialty = "Pediatrician (Child Specialist)",
            experienceYears = 10,
            qualifications = "MBBS, DCH (Pediatrics) — KGMU Lucknow",
            languages = listOf("Hindi", "English", "Bhojpuri"),
            isAvailableNow = true,
            consultationModes = listOf("Voice Call", "Video Call")
        ),
        DoctorInfo(
            id = "doc_3",
            name = "Dr. Ananya Mukherjee",
            specialty = "Obstetrician & Gynecologist (Maternal Health)",
            experienceYears = 12,
            qualifications = "MBBS, MS (OBG) — CMC Vellore",
            languages = listOf("Hindi", "English", "Bengali"),
            isAvailableNow = false,
            consultationModes = listOf("Video Call", "Chat")
        ),
        DoctorInfo(
            id = "doc_4",
            name = "Dr. Vikram Singh",
            specialty = "Emergency Medical Officer (Trauma & Critical)",
            experienceYears = 8,
            qualifications = "MBBS, MEM (Emergency Medicine)",
            languages = listOf("Hindi", "English"),
            isAvailableNow = true,
            consultationModes = listOf("Voice Call", "Priority Emergency")
        )
    )
}
