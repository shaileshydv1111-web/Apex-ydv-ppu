package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PpuDao {

    // --- Notices ---
    @Query("SELECT * FROM notices ORDER BY isImportant DESC, timestamp DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE isBookmarked = 1 ORDER BY isImportant DESC, timestamp DESC")
    fun getBookmarkedNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("UPDATE notices SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateNoticeBookmark(id: String, isBookmarked: Boolean)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: String)

    @Query("DELETE FROM notices WHERE isBookmarked = 0")
    suspend fun clearUnbookmarkedNotices()

    // --- Results ---
    @Query("SELECT * FROM results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<ResultEntity>>

    @Query("SELECT * FROM results WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedResults(): Flow<List<ResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ResultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultEntity)

    @Query("UPDATE results SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateResultBookmark(id: String, isBookmarked: Boolean)

    @Query("DELETE FROM results WHERE id = :id")
    suspend fun deleteResult(id: String)

    // --- PYQs ---
    @Query("SELECT * FROM pyqs ORDER BY timestamp DESC")
    fun getAllPyqs(): Flow<List<PyqEntity>>

    @Query("SELECT * FROM pyqs WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedPyqs(): Flow<List<PyqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyqs(pyqs: List<PyqEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyq(pyq: PyqEntity)

    @Query("UPDATE pyqs SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updatePyqBookmark(id: String, isBookmarked: Boolean)

    @Query("DELETE FROM pyqs WHERE id = :id")
    suspend fun deletePyq(id: String)

    // --- Admissions ---
    @Query("SELECT * FROM admissions ORDER BY timestamp DESC")
    fun getAllAdmissions(): Flow<List<AdmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmissions(admissions: List<AdmissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmission(admission: AdmissionEntity)

    // --- Scholarships ---
    @Query("SELECT * FROM scholarships ORDER BY timestamp DESC")
    fun getAllScholarships(): Flow<List<ScholarshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScholarships(scholarships: List<ScholarshipEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScholarship(scholarship: ScholarshipEntity)

    // --- Banners ---
    @Query("SELECT * FROM banners")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanners(banners: List<BannerEntity>)

    // --- Notifications ---
    @Query("SELECT * FROM user_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<UserNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: UserNotificationEntity)

    @Query("UPDATE user_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("DELETE FROM user_notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}
