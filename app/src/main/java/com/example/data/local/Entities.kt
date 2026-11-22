package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessment_records")
data class AssessmentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mainComplaint: String,
    val symptomsJson: String,
    val riskLevel: String,
    val guidanceTitle: String,
    val guidanceJson: String,
    val isEmergency: Boolean,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relation: String,
    val phone: String,
    val isPrimary: Boolean = false
)
