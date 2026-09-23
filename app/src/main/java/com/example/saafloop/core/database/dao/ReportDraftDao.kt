package com.example.saafloop.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saafloop.core.database.entity.ReportDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDraftDao {

    @Query("SELECT * FROM report_drafts ORDER BY updatedAt DESC")
    fun observeAllDrafts(): Flow<List<ReportDraftEntity>>

    @Query("SELECT * FROM report_drafts WHERE draftId = :draftId")
    suspend fun getDraftById(draftId: String): ReportDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDraft(draft: ReportDraftEntity)

    @Query("DELETE FROM report_drafts WHERE draftId = :draftId")
    suspend fun deleteDraftById(draftId: String)

    @Query("UPDATE report_drafts SET status = :status, updatedAt = :updatedAt WHERE draftId = :draftId")
    suspend fun updateDraftStatus(draftId: String, status: String, updatedAt: Long)

    @Query("SELECT photoPath FROM report_drafts WHERE photoPath IS NOT NULL")
    suspend fun getAllPhotoPaths(): List<String>
}
