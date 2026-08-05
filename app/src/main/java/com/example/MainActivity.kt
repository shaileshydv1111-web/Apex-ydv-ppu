package com.example

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.PpuPatnaTheme
import com.example.viewmodel.PpuViewModel

enum class AppDestination {
    Splash,
    UserSelection,
    Onboarding,
    MainPortal,
    Results,
    AdminPanel,
    PdfViewer,
    GlobalSearch,
    PortalInfo,
    PpuUpdates,
    ExamFormWebView
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PpuViewModel = viewModel()
            val userState by viewModel.userState.collectAsStateWithLifecycle()

            PpuPatnaTheme(darkTheme = userState.isDarkMode) {
                PpuPatnaInfoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PpuPatnaInfoApp(viewModel: PpuViewModel) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(AppDestination.Splash) }
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var activePortalType by remember { mutableStateOf(PortalType.ADMISSION) }
    var activeWebViewUrl by remember { mutableStateOf("https://ppuponline.in/exam_form_search_student_semester.php") }
    var activeWebViewTitle by remember { mutableStateOf("PPU Exam Form Portal") }

    // Collect ViewModel states
    val notices by viewModel.notices.collectAsStateWithLifecycle()
    val isNoticesRefreshing by viewModel.isNoticesRefreshing.collectAsStateWithLifecycle()
    val noticeErrorMessage by viewModel.noticeErrorMessage.collectAsStateWithLifecycle()
    val ppuUpdates by viewModel.ppuUpdates.collectAsStateWithLifecycle()
    val isUpdatesRefreshing by viewModel.isUpdatesRefreshing.collectAsStateWithLifecycle()
    val updatesErrorMessage by viewModel.updatesErrorMessage.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val pyqs by viewModel.pyqs.collectAsStateWithLifecycle()
    val admissions by viewModel.admissions.collectAsStateWithLifecycle()
    val scholarships by viewModel.scholarships.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val bookmarkedNotices by viewModel.bookmarkedNotices.collectAsStateWithLifecycle()
    val bookmarkedResults by viewModel.bookmarkedResults.collectAsStateWithLifecycle()
    val bookmarkedPyqs by viewModel.bookmarkedPyqs.collectAsStateWithLifecycle()

    val noticeCategoryFilter by viewModel.noticeCategoryFilter.collectAsStateWithLifecycle()
    val resultCourseFilter by viewModel.resultCourseFilter.collectAsStateWithLifecycle()
    val pyqCourseFilter by viewModel.pyqCourseFilter.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val activePdfState by viewModel.activePdfState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val studentSuggestions by viewModel.studentSuggestions.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Back Handler for system back button navigation
    when (currentDestination) {
        AppDestination.MainPortal -> {
            if (selectedBottomTab != 0) {
                BackHandler {
                    selectedBottomTab = 0
                }
            }
        }
        AppDestination.Results -> {
            BackHandler {
                currentDestination = AppDestination.MainPortal
            }
        }
        AppDestination.PdfViewer -> {
            BackHandler {
                viewModel.closePdfViewer()
                currentDestination = AppDestination.MainPortal
            }
        }
        AppDestination.GlobalSearch -> {
            BackHandler {
                currentDestination = AppDestination.MainPortal
            }
        }
        AppDestination.PortalInfo -> {
            BackHandler {
                currentDestination = AppDestination.MainPortal
            }
        }
        AppDestination.PpuUpdates -> {
            BackHandler {
                currentDestination = AppDestination.MainPortal
            }
        }
        AppDestination.AdminPanel -> {
            BackHandler {
                currentDestination = AppDestination.MainPortal
            }
        }
        AppDestination.ExamFormWebView -> {
            BackHandler {
                currentDestination = AppDestination.PortalInfo
            }
        }
        else -> {
            // Splash, UserSelection, Onboarding allow system back button to exit naturally
        }
    }

    // Status snackbar handler
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentDestination) {
                AppDestination.Splash -> {
                    SplashScreen(
                        onSplashFinished = {
                            val savedRole = UserSessionManager.getUserRole(context)
                            when (savedRole) {
                                UserRole.STUDENT -> {
                                    viewModel.updateUserRole(isAdmin = false)
                                    currentDestination = AppDestination.MainPortal
                                }
                                UserRole.ADMIN -> {
                                    viewModel.updateUserRole(isAdmin = true)
                                    currentDestination = AppDestination.MainPortal
                                }
                                null -> currentDestination = AppDestination.UserSelection
                            }
                        }
                    )
                }

                AppDestination.UserSelection -> {
                    UserSelectionScreen(
                        onStudentSelect = {
                            UserSessionManager.setUserRole(context, UserRole.STUDENT)
                            viewModel.updateUserRole(isAdmin = false)
                            currentDestination = AppDestination.MainPortal
                        },
                        onAdminLoginSuccess = {
                            UserSessionManager.setUserRole(context, UserRole.ADMIN)
                            viewModel.updateUserRole(isAdmin = true)
                            currentDestination = AppDestination.MainPortal
                        }
                    )
                }

                AppDestination.Onboarding -> {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            currentDestination = AppDestination.MainPortal
                        }
                    )
                }

                AppDestination.MainPortal -> {
                    val unreadNotifs = notifications.count { !it.isRead }

                    Scaffold(
                        topBar = {
                            PpuTopBar(
                                title = when (selectedBottomTab) {
                                    0 -> "PPU Patna Info"
                                    1 -> "Notices & Circulars"
                                    2 -> "Updates"
                                    else -> "Student Profile"
                                },
                                unreadNotificationCount = unreadNotifs,
                                onSearchClick = {
                                    currentDestination = AppDestination.GlobalSearch
                                },
                                onNotificationsClick = {
                                    viewModel.showStatus("Showing ${notifications.size} total notifications")
                                },
                                onAdminClick = {
                                    val role = UserSessionManager.getUserRole(context)
                                    if (role == UserRole.ADMIN) {
                                        currentDestination = AppDestination.AdminPanel
                                    } else {
                                        currentDestination = AppDestination.UserSelection
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            PpuBottomNav(
                                selectedTab = selectedBottomTab,
                                onTabSelected = { tabIndex -> selectedBottomTab = tabIndex }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (selectedBottomTab) {
                                0 -> HomeScreen(
                                    notices = notices,
                                    results = results,
                                    banners = banners,
                                    isNoticesRefreshing = isNoticesRefreshing,
                                    noticeErrorMessage = noticeErrorMessage,
                                    onRefreshNotices = { viewModel.refreshNotices() },
                                    onNoticeClick = { notice ->
                                        viewModel.openPdfViewer(
                                            title = notice.title,
                                            subtitle = "Notice • ${notice.category} • ${notice.date}",
                                            pdfUrl = notice.pdfUrl,
                                            category = notice.category,
                                            date = notice.date
                                        )
                                        currentDestination = AppDestination.PdfViewer
                                    },
                                    onResultClick = { result ->
                                        viewModel.openPdfViewer(
                                            title = result.title,
                                            subtitle = "Result • ${result.course} (${result.session})",
                                            pdfUrl = result.pdfUrl,
                                            category = result.course,
                                            date = result.publishDate
                                        )
                                        currentDestination = AppDestination.PdfViewer
                                    },
                                    onBookmarkToggleNotice = { notice ->
                                        viewModel.toggleNoticeBookmark(notice.id, notice.isBookmarked)
                                    },
                                    onBookmarkToggleResult = { result ->
                                        viewModel.toggleResultBookmark(result.id, result.isBookmarked)
                                    },
                                    onQuickAccessClick = { serviceName ->
                                        when (serviceName) {
                                            "Result", "Results", "Result Check" -> {
                                                currentDestination = AppDestination.Results
                                            }
                                            "All Admission" -> {
                                                activePortalType = PortalType.ADMISSION
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            "Exam Form" -> {
                                                activePortalType = PortalType.EXAM_FORM
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            "Admit Card" -> {
                                                activePortalType = PortalType.ADMIT_CARD
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            "PPU Updates", "Updates", "Update" -> {
                                                selectedBottomTab = 2
                                            }
                                            "Scholarships" -> {
                                                activePortalType = PortalType.SCHOLARSHIPS
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            "PYQ", "PYQs Papers" -> {
                                                activePortalType = PortalType.PYQ_PAPERS
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            "Syllabus" -> {
                                                activePortalType = PortalType.SYLLABUS
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            "Important Links" -> {
                                                activePortalType = PortalType.IMPORTANT_LINKS
                                                currentDestination = AppDestination.PortalInfo
                                            }
                                            else -> {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ppup.ac.in"))
                                                try { context.startActivity(intent) } catch (e: Exception) {
                                                    viewModel.showStatus("Opening official portal: https://ppup.ac.in")
                                                }
                                            }
                                        }
                                    },
                                    onViewAllNoticesClick = { selectedBottomTab = 1 },
                                    onViewAllResultsClick = { currentDestination = AppDestination.Results },
                                    onGlobalSearchClick = { currentDestination = AppDestination.GlobalSearch }
                                )

                                1 -> NoticesScreen(
                                    notices = notices,
                                    isRefreshing = isNoticesRefreshing,
                                    errorMessage = noticeErrorMessage,
                                    selectedCategoryFilter = noticeCategoryFilter,
                                    onCategoryFilterSelect = { filter -> viewModel.setNoticeCategoryFilter(filter) },
                                    onRefresh = { viewModel.refreshNotices() },
                                    onNoticeClick = { notice ->
                                        viewModel.openPdfViewer(
                                            title = notice.title,
                                            subtitle = "Notice • ${notice.category} • ${notice.date}",
                                            pdfUrl = notice.pdfUrl,
                                            category = notice.category,
                                            date = notice.date
                                        )
                                        currentDestination = AppDestination.PdfViewer
                                    },
                                    onBookmarkToggle = { notice ->
                                        viewModel.toggleNoticeBookmark(notice.id, notice.isBookmarked)
                                    }
                                )

                                2 -> PpuUpdatesScreen(
                                    updates = ppuUpdates,
                                    isRefreshing = isUpdatesRefreshing,
                                    errorMessage = updatesErrorMessage,
                                    onRefresh = { viewModel.refreshPpuUpdates() },
                                    onBackClick = { selectedBottomTab = 0 }
                                )

                                3 -> ProfileScreen(
                                    userState = userState,
                                    bookmarkedNotices = bookmarkedNotices,
                                    bookmarkedResults = bookmarkedResults,
                                    bookmarkedPyqs = bookmarkedPyqs,
                                    studentSuggestions = studentSuggestions,
                                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                    onTogglePushNotifications = { viewModel.togglePushNotifications(it) },
                                    onNoticeClick = { notice ->
                                        viewModel.openPdfViewer(
                                            title = notice.title,
                                            subtitle = "Notice • ${notice.category}",
                                            pdfUrl = notice.pdfUrl,
                                            category = notice.category,
                                            date = notice.date
                                        )
                                        currentDestination = AppDestination.PdfViewer
                                    },
                                    onResultClick = { result ->
                                        viewModel.openPdfViewer(
                                            title = result.title,
                                            subtitle = "Result • ${result.course}",
                                            pdfUrl = result.pdfUrl
                                        )
                                        currentDestination = AppDestination.PdfViewer
                                    },
                                    onPyqClick = { pyq ->
                                        viewModel.openPdfViewer(
                                            title = pyq.title,
                                            subtitle = "PYQ • ${pyq.course}",
                                            pdfUrl = pyq.pdfUrl
                                        )
                                        currentDestination = AppDestination.PdfViewer
                                    },
                                    onOpenAdminPanel = {
                                        currentDestination = AppDestination.AdminPanel
                                    },
                                    onSubmitSuggestion = { text -> viewModel.submitStudentSuggestion(text) },
                                    onDeleteSuggestion = { id -> viewModel.deleteStudentSuggestion(id) },
                                    onLogout = {
                                        UserSessionManager.clearSession(context)
                                        viewModel.updateUserRole(isAdmin = false)
                                        currentDestination = AppDestination.UserSelection
                                    }
                                )
                            }
                        }
                    }
                }

                AppDestination.Results -> {
                    ResultsScreen(
                        results = results,
                        selectedCourseFilter = resultCourseFilter,
                        onCourseFilterSelect = { filter -> viewModel.setResultCourseFilter(filter) },
                        onResultClick = { result ->
                            viewModel.openPdfViewer(
                                title = result.title,
                                subtitle = "Result • ${result.course} (${result.session})",
                                pdfUrl = result.pdfUrl,
                                category = result.course,
                                date = result.publishDate
                            )
                            currentDestination = AppDestination.PdfViewer
                        },
                        onBookmarkToggle = { result ->
                            viewModel.toggleResultBookmark(result.id, result.isBookmarked)
                        },
                        onBackClick = { currentDestination = AppDestination.MainPortal },
                        onOpenWebView = { url, title ->
                            activeWebViewUrl = url
                            activeWebViewTitle = title
                            currentDestination = AppDestination.ExamFormWebView
                        }
                    )
                }

                AppDestination.PdfViewer -> {
                    activePdfState?.let { pdfState ->
                        var showDuplicateDialog by remember { mutableStateOf(false) }
                        val sanitizedTitle = pdfState.title.take(30).replace("[^a-zA-Z0-9]".toRegex(), "_")
                        val fileName = if (sanitizedTitle.endsWith(".pdf", ignoreCase = true)) sanitizedTitle else "$sanitizedTitle.pdf"
                        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val existingFile = java.io.File(downloadsFolder, fileName)

                        fun performPdfDownload() {
                            try {
                                if (existingFile.exists()) {
                                    existingFile.delete()
                                }
                                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                val targetUrl = pdfState.pdfUrl.ifBlank { "https://ppup.ac.in/upload/notices/sample_notice.pdf" }
                                val request = DownloadManager.Request(Uri.parse(targetUrl)).apply {
                                    setTitle(pdfState.title)
                                    setDescription("Downloading PDF to Downloads folder...")
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                    setMimeType("application/pdf")
                                    setAllowedOverMetered(true)
                                    setAllowedOverRoaming(true)
                                }
                                downloadManager.enqueue(request)
                                Toast.makeText(
                                    context,
                                    "📥 PDF download started! Saved in Downloads folder & Files app.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        fun openDownloadedPdfFile() {
                            try {
                                if (existingFile.exists()) {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        existingFile
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open ${pdfState.title} with..."))
                                } else {
                                    val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open file: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        if (showDuplicateDialog) {
                            AlertDialog(
                                onDismissRequest = { showDuplicateDialog = false },
                                title = { Text("PDF Already Downloaded", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                                text = {
                                    Text(
                                        "The document \"$fileName\" is already saved in your phone's Downloads folder.\n\n" +
                                        "Would you like to open the PDF file or download it again?",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        showDuplicateDialog = false
                                        openDownloadedPdfFile()
                                    }) {
                                        Text("Open Downloaded PDF")
                                    }
                                },
                                dismissButton = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(onClick = {
                                            showDuplicateDialog = false
                                            performPdfDownload()
                                        }) {
                                            Text("Replace & Download")
                                        }
                                        TextButton(onClick = { showDuplicateDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                }
                            )
                        }

                        PdfViewerScreen(
                            pdfState = pdfState,
                            onClose = {
                                viewModel.closePdfViewer()
                                currentDestination = AppDestination.MainPortal
                            },
                            onPageChange = { delta -> viewModel.updatePdfPage(delta) },
                            onZoomChange = { delta -> viewModel.updatePdfZoom(delta) },
                            onSearchChange = { query -> viewModel.updatePdfSearchQuery(query) },
                            onDownloadPdf = {
                                if (existingFile.exists()) {
                                    showDuplicateDialog = true
                                } else {
                                    performPdfDownload()
                                }
                            },
                            onOpenDownloadedPdf = {
                                openDownloadedPdfFile()
                            },
                            onSharePdf = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, pdfState.title)
                                        putExtra(Intent.EXTRA_TEXT, "${pdfState.title}\n\nPPU Notice Link: ${pdfState.pdfUrl}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Notice PDF"))
                                } catch (_: Exception) {}
                            }
                        )
                    } ?: run {
                        currentDestination = AppDestination.MainPortal
                    }
                }

                AppDestination.GlobalSearch -> {
                    GlobalSearchScreen(
                        initialQuery = viewModel.globalSearchQuery.value,
                        notices = notices,
                        results = results,
                        pyqs = pyqs,
                        admissions = admissions,
                        scholarships = scholarships,
                        onClose = { currentDestination = AppDestination.MainPortal },
                        onNoticeClick = { notice ->
                            viewModel.openPdfViewer(
                                title = notice.title,
                                subtitle = "Notice • ${notice.category}",
                                pdfUrl = notice.pdfUrl
                            )
                            currentDestination = AppDestination.PdfViewer
                        },
                        onResultClick = { result ->
                            viewModel.openPdfViewer(
                                title = result.title,
                                subtitle = "Result • ${result.course}",
                                pdfUrl = result.pdfUrl
                            )
                            currentDestination = AppDestination.PdfViewer
                        },
                        onPyqClick = { pyq ->
                            viewModel.openPdfViewer(
                                title = pyq.title,
                                subtitle = "PYQ • ${pyq.course}",
                                pdfUrl = pyq.pdfUrl
                            )
                            currentDestination = AppDestination.PdfViewer
                        },
                        onBookmarkToggleNotice = { notice -> viewModel.toggleNoticeBookmark(notice.id, notice.isBookmarked) },
                        onBookmarkToggleResult = { result -> viewModel.toggleResultBookmark(result.id, result.isBookmarked) },
                        onBookmarkTogglePyq = { pyq -> viewModel.togglePyqBookmark(pyq.id, pyq.isBookmarked) }
                    )
                }

                AppDestination.AdminPanel -> {
                    AdminPanelScreen(
                        notices = notices,
                        results = results,
                        pyqs = pyqs,
                        studentSuggestions = studentSuggestions,
                        onClose = { currentDestination = AppDestination.MainPortal },
                        onLogout = {
                            UserSessionManager.clearSession(context)
                            currentDestination = AppDestination.UserSelection
                        },
                        onPublishNotice = { title, category, description, pdfUrl, isImportant ->
                            viewModel.adminPublishNotice(title, category, description, pdfUrl, isImportant)
                        },
                        onPublishResult = { title, course, session, pdfUrl ->
                            viewModel.adminPublishResult(title, course, session, pdfUrl)
                        },
                        onPublishPyq = { title, course, dept, year, sem, pdfUrl ->
                            viewModel.adminPublishPyq(title, course, dept, year, sem, pdfUrl)
                        },
                        onSendBroadcastNotification = { title, body, type ->
                            viewModel.adminSendBroadcast(title, body, type)
                        },
                        onDeleteNotice = { id -> viewModel.adminDeleteNotice(id) },
                        onDeleteResult = { id -> viewModel.adminDeleteResult(id) },
                        onDeletePyq = { id -> viewModel.adminDeletePyq(id) },
                        onDeleteSuggestion = { id -> viewModel.deleteStudentSuggestion(id) }
                    )
                }

                AppDestination.PortalInfo -> {
                    PortalInfoScreen(
                        portalType = activePortalType,
                        onBackClick = { currentDestination = AppDestination.MainPortal },
                        onOpenWebView = { url, title ->
                            activeWebViewUrl = url
                            activeWebViewTitle = title
                            currentDestination = AppDestination.ExamFormWebView
                        }
                    )
                }

                AppDestination.ExamFormWebView -> {
                    ExamFormWebViewScreen(
                        initialUrl = activeWebViewUrl,
                        title = activeWebViewTitle,
                        onBackClick = { currentDestination = AppDestination.PortalInfo }
                    )
                }

                AppDestination.PpuUpdates -> {
                    PpuUpdatesScreen(
                        updates = ppuUpdates,
                        isRefreshing = isUpdatesRefreshing,
                        errorMessage = updatesErrorMessage,
                        onRefresh = { viewModel.refreshPpuUpdates() },
                        onBackClick = { currentDestination = AppDestination.MainPortal }
                    )
                }
            }
        }
    }
}
