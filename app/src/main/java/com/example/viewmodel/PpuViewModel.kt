package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserProfileState(
    val isLoggedIn: Boolean = true,
    val email: String = "student@ppup.ac.in",
    val name: String = "Patliputra Student",
    val course: String = "UG / PG / Vocational",
    val rollNo: String = "2026PPU10492",
    val isAdmin: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val isDarkMode: Boolean = false
)

data class PdfDocumentState(
    val title: String = "",
    val subtitle: String = "",
    val pdfUrl: String = "",
    val category: String = "",
    val date: String = "",
    val totalPages: Int = 12,
    val currentPage: Int = 1,
    val zoomLevel: Float = 1.25f,
    val searchQuery: String = "",
    val isBookmarked: Boolean = false
)

class PpuViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PpuRepository

    // State flows
    val notices: StateFlow<List<NoticeEntity>>
    val results: StateFlow<List<ResultEntity>>
    val pyqs: StateFlow<List<PyqEntity>>
    val admissions: StateFlow<List<AdmissionEntity>>
    val scholarships: StateFlow<List<ScholarshipEntity>>
    val banners: StateFlow<List<BannerEntity>>
    val notifications: StateFlow<List<UserNotificationEntity>>

    val bookmarkedNotices: StateFlow<List<NoticeEntity>>
    val bookmarkedResults: StateFlow<List<ResultEntity>>
    val bookmarkedPyqs: StateFlow<List<PyqEntity>>

    // Search and filters
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _noticeCategoryFilter = MutableStateFlow("All")
    val noticeCategoryFilter: StateFlow<String> = _noticeCategoryFilter.asStateFlow()

    private val _resultCourseFilter = MutableStateFlow("All")
    val resultCourseFilter: StateFlow<String> = _resultCourseFilter.asStateFlow()

    private val _pyqCourseFilter = MutableStateFlow("All")
    val pyqCourseFilter: StateFlow<String> = _pyqCourseFilter.asStateFlow()

    // User Profile & Settings
    private val _userState = MutableStateFlow(UserProfileState())
    val userState: StateFlow<UserProfileState> = _userState.asStateFlow()

    fun updateUserRole(isAdmin: Boolean) {
        if (isAdmin) {
            _userState.value = _userState.value.copy(
                isAdmin = true,
                name = "Administrator",
                email = "PPUAPP",
                course = "Super Admin"
            )
        } else {
            _userState.value = _userState.value.copy(
                isAdmin = false,
                name = "Patliputra Student",
                email = "student@ppup.ac.in",
                course = "UG / PG / Vocational"
            )
        }
    }

    // Active PDF Document in Viewer
    private val _activePdfState = MutableStateFlow<PdfDocumentState?>(null)
    val activePdfState: StateFlow<PdfDocumentState?> = _activePdfState.asStateFlow()

    // Status Message / Toast feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Live Notice Refresh & Error States
    private val _isNoticesRefreshing = MutableStateFlow(false)
    val isNoticesRefreshing: StateFlow<Boolean> = _isNoticesRefreshing.asStateFlow()

    private val _noticeErrorMessage = MutableStateFlow<String?>(null)
    val noticeErrorMessage: StateFlow<String?> = _noticeErrorMessage.asStateFlow()

    // Telegram PPU Updates Refresh & Error States
    private val _ppuUpdates = MutableStateFlow<List<PpuUpdateEntity>>(emptyList())
    val ppuUpdates: StateFlow<List<PpuUpdateEntity>> = _ppuUpdates.asStateFlow()

    private val _isUpdatesRefreshing = MutableStateFlow(false)
    val isUpdatesRefreshing: StateFlow<Boolean> = _isUpdatesRefreshing.asStateFlow()

    private val _updatesErrorMessage = MutableStateFlow<String?>(null)
    val updatesErrorMessage: StateFlow<String?> = _updatesErrorMessage.asStateFlow()

    init {
        val database = PpuDatabase.getDatabase(application)
        repository = PpuRepository(database.ppuDao())

        // ViewModel scope initial setup
        viewModelScope.launch {
            val cached = NoticeFetcher.loadFromCache(application)
            if (cached.isNotEmpty()) {
                repository.syncNotices(cached)
            }
            val cachedUpdates = PpuUpdateFetcher.loadFromCache(application)
            if (cachedUpdates.isNotEmpty()) {
                _ppuUpdates.value = cachedUpdates
            }
            repository.seedInitialDataIfEmpty()
            refreshNotices()
            refreshPpuUpdates()
        }

        notices = repository.allNotices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        results = repository.allResults.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        pyqs = repository.allPyqs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        admissions = repository.allAdmissions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        scholarships = repository.allScholarships.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        banners = repository.allBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        bookmarkedNotices = repository.bookmarkedNotices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        bookmarkedResults = repository.bookmarkedResults.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        bookmarkedPyqs = repository.bookmarkedPyqs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun refreshNotices() {
        viewModelScope.launch {
            _isNoticesRefreshing.value = true
            _noticeErrorMessage.value = null
            try {
                val fetched = NoticeFetcher.fetchNoticesFromWeb()
                if (fetched.isNotEmpty()) {
                    NoticeFetcher.saveToCache(getApplication(), fetched)
                    repository.syncNotices(fetched)
                    _noticeErrorMessage.value = null
                } else {
                    val cached = NoticeFetcher.loadFromCache(getApplication())
                    if (cached.isEmpty() && notices.value.isEmpty()) {
                        _noticeErrorMessage.value = "कोई नया नोटिस उपलब्ध नहीं है।"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val cached = NoticeFetcher.loadFromCache(getApplication())
                if (cached.isNotEmpty()) {
                    repository.syncNotices(cached)
                }
                if (notices.value.isEmpty() && cached.isEmpty()) {
                    _noticeErrorMessage.value = "इंटरनेट कनेक्शन उपलब्ध नहीं है। कृपया पुनः प्रयास करें।"
                } else {
                    showStatus("ऑफ़लाइन मोड: कैश्ड नोटिस दिखाए जा रहे हैं")
                }
            } finally {
                _isNoticesRefreshing.value = false
            }
        }
    }

    fun refreshPpuUpdates() {
        viewModelScope.launch {
            _isUpdatesRefreshing.value = true
            _updatesErrorMessage.value = null
            try {
                val fetched = PpuUpdateFetcher.fetchUpdatesFromTelegram()
                if (fetched.isNotEmpty()) {
                    PpuUpdateFetcher.saveToCache(getApplication(), fetched)
                    _ppuUpdates.value = fetched
                    _updatesErrorMessage.value = null
                } else {
                    val cached = PpuUpdateFetcher.loadFromCache(getApplication())
                    if (cached.isNotEmpty()) {
                        _ppuUpdates.value = cached
                    } else if (_ppuUpdates.value.isEmpty()) {
                        _updatesErrorMessage.value = "अभी कोई नया अपडेट उपलब्ध नहीं है।"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val cached = PpuUpdateFetcher.loadFromCache(getApplication())
                if (cached.isNotEmpty()) {
                    _ppuUpdates.value = cached
                }
                if (_ppuUpdates.value.isEmpty()) {
                    _updatesErrorMessage.value = "इंटरनेट कनेक्शन उपलब्ध नहीं है। कृपया पुनः प्रयास करें।"
                } else {
                    showStatus("ऑफ़लाइन मोड: कैश्ड अपडेट्स दिखाए जा रहे हैं")
                }
            } finally {
                _isUpdatesRefreshing.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun setNoticeCategoryFilter(category: String) {
        _noticeCategoryFilter.value = category
    }

    fun setResultCourseFilter(course: String) {
        _resultCourseFilter.value = course
    }

    fun setPyqCourseFilter(course: String) {
        _pyqCourseFilter.value = course
    }

    fun toggleNoticeBookmark(id: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleNoticeBookmark(id, isBookmarked)
            showStatus(if (!isBookmarked) "Notice added to Bookmarks" else "Notice removed from Bookmarks")
        }
    }

    fun toggleResultBookmark(id: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleResultBookmark(id, isBookmarked)
            showStatus(if (!isBookmarked) "Result added to Bookmarks" else "Result removed from Bookmarks")
        }
    }

    fun togglePyqBookmark(id: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.togglePyqBookmark(id, isBookmarked)
            showStatus(if (!isBookmarked) "PYQ added to Bookmarks" else "PYQ removed from Bookmarks")
        }
    }

    fun openPdfViewer(title: String, subtitle: String, pdfUrl: String, category: String = "", date: String = "") {
        _activePdfState.value = PdfDocumentState(
            title = title,
            subtitle = subtitle,
            pdfUrl = pdfUrl,
            category = category,
            date = date
        )
    }

    fun closePdfViewer() {
        _activePdfState.value = null
    }

    fun updatePdfPage(delta: Int) {
        _activePdfState.value?.let { current ->
            val newPage = (current.currentPage + delta).coerceIn(1, current.totalPages)
            _activePdfState.value = current.copy(currentPage = newPage)
        }
    }

    fun updatePdfZoom(delta: Float) {
        _activePdfState.value?.let { current ->
            val newZoom = (current.zoomLevel + delta).coerceIn(0.5f, 3.0f)
            _activePdfState.value = current.copy(zoomLevel = newZoom)
        }
    }

    fun updatePdfSearchQuery(query: String) {
        _activePdfState.value?.let { current ->
            _activePdfState.value = current.copy(searchQuery = query)
        }
    }

    // Auth actions
    fun loginAsGuest() {
        _userState.value = _userState.value.copy(
            isLoggedIn = true,
            email = "guest@ppu.ac.in",
            name = "Guest Student",
            isAdmin = false
        )
        showStatus("Logged in as Guest")
    }

    fun loginWithEmail(email: String, name: String, rollNo: String) {
        _userState.value = _userState.value.copy(
            isLoggedIn = true,
            email = email,
            name = name,
            rollNo = rollNo,
            isAdmin = false
        )
        showStatus("Welcome, $name!")
    }

    fun loginAsAdmin(adminKey: String) {
        if (adminKey == "admin123" || adminKey == "ppu2026") {
            _userState.value = _userState.value.copy(
                isLoggedIn = true,
                email = "admin@ppup.ac.in",
                name = "PPU Portal Admin",
                isAdmin = true
            )
            showStatus("Admin Access Granted!")
        } else {
            showStatus("Invalid Admin Key! Try 'admin123'")
        }
    }

    fun logout() {
        _userState.value = UserProfileState()
        showStatus("Logged out successfully")
    }

    fun toggleDarkMode(enabled: Boolean) {
        _userState.value = _userState.value.copy(isDarkMode = enabled)
    }

    fun togglePushNotifications(enabled: Boolean) {
        _userState.value = _userState.value.copy(pushNotificationsEnabled = enabled)
        showStatus(if (enabled) "Push Notifications Enabled" else "Push Notifications Muted")
    }

    // Admin Publishing operations
    fun adminPublishNotice(title: String, category: String, description: String, pdfUrl: String, isImportant: Boolean) {
        viewModelScope.launch {
            val notice = NoticeEntity(
                id = "n_${System.currentTimeMillis()}",
                title = title,
                category = category,
                date = "Today",
                description = description,
                pdfUrl = if (pdfUrl.isBlank()) "https://ppup.ac.in/notices/Official_Notice.pdf" else pdfUrl,
                isImportant = isImportant
            )
            repository.insertNotice(notice)
            showStatus("Notice Published & Broadcasted!")
        }
    }

    fun adminPublishResult(title: String, course: String, session: String, pdfUrl: String) {
        viewModelScope.launch {
            val result = ResultEntity(
                id = "r_${System.currentTimeMillis()}",
                title = title,
                course = course,
                session = session,
                publishDate = "Today",
                pdfUrl = if (pdfUrl.isBlank()) "https://ppup.ac.in/results/Result_Official.pdf" else pdfUrl
            )
            repository.insertResult(result)
            showStatus("Result Published!")
        }
    }

    fun adminPublishPyq(title: String, course: String, department: String, year: String, semester: String, pdfUrl: String) {
        viewModelScope.launch {
            val pyq = PyqEntity(
                id = "p_${System.currentTimeMillis()}",
                title = title,
                course = course,
                department = department,
                year = year,
                semester = semester,
                pdfUrl = if (pdfUrl.isBlank()) "https://ppup.ac.in/pyq/PYQ_Paper.pdf" else pdfUrl
            )
            repository.insertPyq(pyq)
            showStatus("PYQ Paper Uploaded!")
        }
    }

    fun adminDeleteNotice(id: String) {
        viewModelScope.launch {
            repository.deleteNotice(id)
            showStatus("Notice deleted")
        }
    }

    fun adminDeleteResult(id: String) {
        viewModelScope.launch {
            repository.deleteResult(id)
            showStatus("Result deleted")
        }
    }

    fun adminDeletePyq(id: String) {
        viewModelScope.launch {
            repository.deletePyq(id)
            showStatus("PYQ deleted")
        }
    }

    fun adminSendBroadcast(title: String, body: String, type: String) {
        viewModelScope.launch {
            repository.sendBroadcastNotification(title, body, type)
            showStatus("Push Notification Sent to All Users!")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    // Student Suggestions
    private val _studentSuggestions = MutableStateFlow<List<StudentSuggestionEntity>>(
        listOf(
            StudentSuggestionEntity(
                id = "sug_1",
                studentName = "Rohan Kumar",
                studentEmail = "rohan.bca23@gmail.com",
                suggestionText = "Please add Semester 5 BCA Previous Year Question papers with solutions.",
                timestamp = System.currentTimeMillis() - 3600000,
                dateString = "Today, 11:30 AM"
            ),
            StudentSuggestionEntity(
                id = "sug_2",
                studentName = "Priya Kumari",
                studentEmail = "priya.bsc.patna@gmail.com",
                suggestionText = "Great app! Kindly update B.Sc Part 2 Physics Exam Schedule notice link.",
                timestamp = System.currentTimeMillis() - 86400000,
                dateString = "Yesterday, 3:45 PM"
            )
        )
    )
    val studentSuggestions: StateFlow<List<StudentSuggestionEntity>> = _studentSuggestions.asStateFlow()

    fun submitStudentSuggestion(text: String, studentName: String = "", studentEmail: String = "") {
        if (text.isBlank()) return
        val name = studentName.ifBlank { _userState.value.name }
        val email = studentEmail.ifBlank { _userState.value.email }
        val newSug = StudentSuggestionEntity(
            id = "sug_${System.currentTimeMillis()}",
            studentName = name,
            studentEmail = email,
            suggestionText = text,
            timestamp = System.currentTimeMillis(),
            dateString = "Just Now"
        )
        _studentSuggestions.value = listOf(newSug) + _studentSuggestions.value
        showStatus("धन्यवाद! आपका सुझाव एडमिन को प्राप्त हो गया है।")
    }

    fun deleteStudentSuggestion(id: String) {
        _studentSuggestions.value = _studentSuggestions.value.filter { it.id != id }
        showStatus("Suggestion deleted")
    }

    fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
