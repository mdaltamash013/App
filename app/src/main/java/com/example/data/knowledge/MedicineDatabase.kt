package com.example.data.knowledge

import com.example.data.model.MedicineItem

object MedicineDatabase {

    val medicines = listOf(
        MedicineItem(
            id = "ors_packet",
            name = "Oral Rehydration Salts (ORS)",
            genericName = "Sodium Chloride + Glucose + Potassium Chloride + Sodium Citrate",
            category = "Electrolyte Replenisher",
            requiresPrescription = false,
            purpose = "Prevents and treats dehydration caused by acute diarrhea, loose stools, or excessive heat sweating.",
            precautions = listOf(
                "Dissolve the ENTIRE packet in 1 liter of clean drinking water.",
                "Do NOT boil the solution after mixing.",
                "Discard any unused reconstituted solution after 24 hours and make fresh."
            ),
            sideEffects = listOf("Very safe. Rarely nausea if consumed in extremely large quantities too fast."),
            verifiedAvailable = true,
            verifiedPharmacy = "Jan Aushadhi Kendra (Block Center, PHC Road)",
            estimatedDelivery = "In Stock — Walk-in pickup or ASHA village supply"
        ),

        MedicineItem(
            id = "paracetamol_500",
            name = "Paracetamol (500mg)",
            genericName = "Acetaminophen / Paracetamol IP",
            category = "Antipyretic & Analgesic (OTC)",
            requiresPrescription = false,
            purpose = "Temporary relief of mild to moderate fever, headaches, body aches, and muscular pain.",
            precautions = listOf(
                "Take after food with water.",
                "Do not exceed 4 tablets (2000mg) in 24 hours without medical supervision.",
                "Patients with known liver disease must consult a doctor first.",
                "Keep out of reach of small children."
            ),
            sideEffects = listOf("Rare at recommended doses. Nausea or allergic rash in sensitive individuals."),
            verifiedAvailable = true,
            verifiedPharmacy = "Jan Aushadhi Kendra (Block Center)",
            estimatedDelivery = "In Stock — 10 strips available"
        ),

        MedicineItem(
            id = "zinc_sulfate_20",
            name = "Zinc Sulfate Dispersible (20mg)",
            genericName = "Zinc Sulfate IP",
            category = "Pediatric Diarrhea Mineral Supplement",
            requiresPrescription = false,
            purpose = "Reduces severity and duration of pediatric diarrhea; taken for 14 consecutive days.",
            precautions = listOf(
                "Disperse tablet in breast milk or clean water before administering to children.",
                "Complete full 14-day course even if diarrhea stops early.",
                "Always give alongside ORS."
            ),
            sideEffects = listOf("Mild tummy upset or vomiting if taken on an empty stomach."),
            verifiedAvailable = true,
            verifiedPharmacy = "Jan Aushadhi Kendra (PHC Campus)",
            estimatedDelivery = "In Stock"
        ),

        MedicineItem(
            id = "cetirizine_10",
            name = "Cetirizine (10mg)",
            genericName = "Cetirizine Hydrochloride IP",
            category = "Antihistamine (Allergy relief)",
            requiresPrescription = false,
            purpose = "Relief of runny nose, seasonal sneezing, skin itching, hives, and mild insect bite reactions.",
            precautions = listOf(
                "May cause mild drowsiness; avoid driving or operating machinery.",
                "Usually taken once daily in the evening.",
                "Avoid alcohol while taking antihistamines."
            ),
            sideEffects = listOf("Drowsiness, dry mouth, mild headache."),
            verifiedAvailable = true,
            verifiedPharmacy = "Jan Aushadhi Kendra (Market Road)",
            estimatedDelivery = "In Stock"
        ),

        MedicineItem(
            id = "pantoprazole_40",
            name = "Pantoprazole (40mg)",
            genericName = "Pantoprazole Gastro-resistant IP",
            category = "Proton Pump Inhibitor (Acidity & Gastritis)",
            requiresPrescription = true,
            purpose = "Treatment of severe gastroesophageal reflux, gastric ulcers, and burning acidity.",
            precautions = listOf(
                "⚠️ Prescription required from a certified doctor.",
                "Take once daily in the morning 30 minutes before breakfast.",
                "Swallow whole; do not crush or chew."
            ),
            sideEffects = listOf("Headache, mild abdominal pain, diarrhea, or flatulence."),
            verifiedAvailable = true,
            verifiedPharmacy = "Jan Aushadhi Kendra & Community Health Centre",
            estimatedDelivery = "Available with valid prescription"
        ),

        MedicineItem(
            id = "amoxicillin_500",
            name = "Amoxicillin (500mg)",
            genericName = "Amoxicillin Trihydrate IP",
            category = "Antibiotic (Prescription Only)",
            requiresPrescription = true,
            purpose = "Bacterial infections diagnosed by a physician (ear, chest, urinary tract, or throat).",
            precautions = listOf(
                "⚠️ STRICTLY PRESCRIPTION ONLY: AI cannot prescribe antibiotics.",
                "Never take for viral infections like common cold or flu.",
                "Never take if allergic to Penicillin.",
                "Must complete the full prescribed course."
            ),
            sideEffects = listOf("Nausea, diarrhea, allergic rash."),
            verifiedAvailable = true,
            verifiedPharmacy = "Community Health Centre / Jan Aushadhi Kendra",
            estimatedDelivery = "Prescription verification required at counter"
        ),

        MedicineItem(
            id = "iron_folic_acid",
            name = "Iron & Folic Acid (IFA)",
            genericName = "Dried Ferrous Sulfate + Folic Acid",
            category = "Anemia Prophylaxis & Maternal Health",
            requiresPrescription = false,
            purpose = "Prevention and treatment of nutritional anemia in pregnant women, adolescent girls, and lactating mothers.",
            precautions = listOf(
                "Take with or after food, preferably with citrus juice/water (Vitamin C enhances absorption).",
                "Do not take with tea, coffee, or milk as they reduce absorption.",
                "May turn stools dark or black (harmless)."
            ),
            sideEffects = listOf("Mild constipation, dark stools, occasional metallic taste."),
            verifiedAvailable = true,
            verifiedPharmacy = "Sub-Centre / ASHA Worker Supply / Jan Aushadhi",
            estimatedDelivery = "Free supply at Govt PHC / Jan Aushadhi"
        )
    )

    fun search(query: String): List<MedicineItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return medicines
        return medicines.filter {
            it.name.lowercase().contains(q) ||
            it.genericName.lowercase().contains(q) ||
            it.category.lowercase().contains(q) ||
            it.purpose.lowercase().contains(q)
        }
    }
}
