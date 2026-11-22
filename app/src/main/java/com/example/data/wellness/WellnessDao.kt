package com.example.data.wellness

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    @Query("SELECT * FROM wellness_records WHERE personnelId = :personnelId ORDER BY timestamp DESC")
    fun getAllWellnessRecords(personnelId: String = "self"): Flow<List<WellnessRecordEntity>>

    @Query("SELECT * FROM wellness_records WHERE personnelId = :personnelId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWellnessRecord(personnelId: String = "self"): Flow<WellnessRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessRecord(record: WellnessRecordEntity): Long

    @Delete
    suspend fun deleteWellnessRecord(record: WellnessRecordEntity)

    @Query("DELETE FROM wellness_records WHERE personnelId = :personnelId")
    suspend fun clearAllWellnessRecords(personnelId: String = "self")

    @Query("SELECT * FROM welfare_support_requests ORDER BY timestamp DESC")
    fun getAllSupportRequests(): Flow<List<SupportRequestEntity>>

    @Query("SELECT * FROM welfare_support_requests WHERE personnelId = :personnelId ORDER BY timestamp DESC")
    fun getSupportRequestsForPersonnel(personnelId: String = "self"): Flow<List<SupportRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportRequest(request: SupportRequestEntity): Long

    @Update
    suspend fun updateSupportRequest(request: SupportRequestEntity)

    @Query("SELECT * FROM wellness_consent_records WHERE personnelId = :personnelId LIMIT 1")
    fun getConsent(personnelId: String = "self"): Flow<WellnessConsentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConsent(consent: WellnessConsentEntity)

    @Insert
    suspend fun insertAuditLog(log: WelfareAuditLogEntity): Long

    @Query("SELECT * FROM welfare_audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAuditLogs(): Flow<List<WelfareAuditLogEntity>>
}
