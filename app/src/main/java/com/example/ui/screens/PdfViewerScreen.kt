package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.PdfDocumentState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PdfViewerScreen(
    pdfState: PdfDocumentState,
    onClose: () -> Unit,
    onPageChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSearchChange: (String) -> Unit,
    onDownloadPdf: () -> Unit,
    onOpenDownloadedPdf: () -> Unit = {},
    onSharePdf: () -> Unit
) {
    var showSearchField by remember { mutableStateOf(false) }
    var isWebViewMode by remember { mutableStateOf(pdfState.pdfUrl.startsWith("http")) }
    var isPdfLoaded by remember { mutableStateOf(!pdfState.pdfUrl.startsWith("http")) }
    var isLoading by remember { mutableStateOf(pdfState.pdfUrl.startsWith("http")) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sanitizedTitle = remember(pdfState.title) {
        pdfState.title.take(30).replace("[^a-zA-Z0-9]".toRegex(), "_")
    }
    val fileName = remember(sanitizedTitle) {
        if (sanitizedTitle.endsWith(".pdf", ignoreCase = true)) sanitizedTitle else "$sanitizedTitle.pdf"
    }
    val downloadsFolder = remember { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) }
    val downloadedFile = remember(fileName) { java.io.File(downloadsFolder, fileName) }
    var isFileDownloaded by remember { mutableStateOf(downloadedFile.exists()) }

    DisposableEffect(context, fileName) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    isFileDownloaded = downloadedFile.exists()
                }
            }
        }
        val filter = android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Text(
                            text = pdfState.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (pdfState.subtitle.isNotBlank()) {
                            Text(
                                text = pdfState.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("pdf_close_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchField = !showSearchField }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search Document")
                    }
                    IconButton(onClick = onSharePdf, modifier = Modifier.testTag("pdf_share_button")) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share PDF")
                    }
                    if (isPdfLoaded && isFileDownloaded) {
                        IconButton(onClick = onOpenDownloadedPdf, modifier = Modifier.testTag("pdf_open_downloaded_button")) {
                            Icon(Icons.Outlined.FolderOpen, contentDescription = "Open Downloaded PDF")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onZoomChange(-0.25f) }) {
                            Icon(Icons.Outlined.ZoomOut, contentDescription = "Zoom Out")
                        }
                        Text(
                            text = "${(pdfState.zoomLevel * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onZoomChange(0.25f) }) {
                            Icon(Icons.Outlined.ZoomIn, contentDescription = "Zoom In")
                        }
                    }

                    // Page Navigation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onPageChange(-1) },
                            enabled = pdfState.currentPage > 1
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Page")
                        }
                        Text(
                            text = "Page ${pdfState.currentPage} / ${pdfState.totalPages}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = { onPageChange(1) },
                            enabled = pdfState.currentPage < pdfState.totalPages
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next Page")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {

            AnimatedVisibility(visible = showSearchField) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    OutlinedTextField(
                        value = pdfState.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Find text in document...") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (pdfState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            // Web or Document Render Area
            if (isWebViewMode && pdfState.pdfUrl.startsWith("http")) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                        hasError = false
                                        isPdfLoaded = false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                        if (!hasError) {
                                            isPdfLoaded = true
                                        }
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        errorCode: Int,
                                        description: String?,
                                        failingUrl: String?
                                    ) {
                                        isLoading = false
                                        hasError = true
                                        isPdfLoaded = false
                                        errorMessage = description ?: "Unable to load PDF document."
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: android.webkit.WebResourceRequest?,
                                        error: android.webkit.WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            isLoading = false
                                            hasError = true
                                            isPdfLoaded = false
                                            errorMessage = error?.description?.toString() ?: "Network connection error."
                                        }
                                    }
                                }

                                val docUrl = if (pdfState.pdfUrl.contains(".pdf", ignoreCase = true)) {
                                    "https://docs.google.com/gview?embedded=true&url=${pdfState.pdfUrl}"
                                } else {
                                    pdfState.pdfUrl
                                }
                                loadUrl(docUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading && !isPdfLoaded && !hasError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.95f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading PDF Document...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (hasError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Failed to load PDF",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (errorMessage.isNotBlank()) errorMessage else "Please check your internet connection and try again.",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                hasError = false
                                                isLoading = true
                                                isPdfLoaded = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Retry Loading", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                hasError = false
                                                isLoading = false
                                                isWebViewMode = false
                                                isPdfLoaded = true
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("View Offline Preview", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Document Canvas Render
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.98f)
                            .graphicsLayer(
                                scaleX = pdfState.zoomLevel,
                                scaleY = pdfState.zoomLevel
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // University Document Header
                            Text(
                                text = "PATLIPUTRA UNIVERSITY, PATNA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0B3C5D),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Kankarbagh, Patna, Bihar - 800020",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "OFFICIAL EXAMINATION & ACADEMIC PORTAL DOCUMENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC0392B),
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(thickness = 2.dp, color = Color(0xFF0B3C5D))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Document Title
                            Text(
                                text = pdfState.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Body text preview representing the PDF
                            Text(
                                text = "REF NO: PPU/EXAM/2026/${100 + pdfState.currentPage}\t\tDATE: ${pdfState.date.ifBlank { "01 AUG 2026" }}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "This is to notify all concerned students, faculties, and constituent colleges under Patliputra University Patna that the official schedules, guidelines, and directives outlined below have been approved by the Competent Authority.\n\n" +
                                       "1. Students must verify their Roll Number and Registration Number before appearing for the scheduled examinations.\n" +
                                       "2. All practical and theory examination admit cards must be produced along with valid university identity cards.\n" +
                                       "3. Any grievance regarding result tabulations or answer key re-evaluation should be submitted through the student portal within 15 days of declaration.\n\n" +
                                       "By Order of the Vice-Chancellor\n" +
                                       "Controller of Examinations\n" +
                                       "Patliputra University, Patna",
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF1E293B)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            if (isPdfLoaded && isFileDownloaded) {
                                Button(
                                    onClick = onOpenDownloadedPdf,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF27AE60)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("pdf_canvas_open_button")
                                ) {
                                    Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Stamp / Seal graphic
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0B3C5D).copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0B3C5D)),
                                modifier = Modifier.size(90.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "OFFICIAL SEAL\nPPU PATNA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFF0B3C5D)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "--- Page ${pdfState.currentPage} of ${pdfState.totalPages} ---",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

