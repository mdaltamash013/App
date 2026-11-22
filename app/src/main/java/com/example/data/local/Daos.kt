package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessment_records ORDER BY timestamp DESC")
    fun getAllAssessments(): Flow<List<AssessmentRecordEntity>>

    @Query("SELECT * FROM assessment_records WHERE id = :id")
    suspend fun getAssessmentById(id: Long): AssessmentRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentRecordEntity): Long

    @Query("DELETE FROM assessment_records WHERE id = :id")
    suspend fun deleteAssessment(id: Long)

    @Query("SELECT COUNT(*) FROM assessment_records")
    fun getAssessmentCount(): Flow<Int>
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, id ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)
}
