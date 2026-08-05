package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Environment
import android.view.View
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

object PersistentWebViewHolder {
    private val webViewCache = mutableMapOf<String, WebView>()

    fun getOrCreate(context: Context, key: String): Pair<WebView, Boolean> {
        val existing = webViewCache[key]
        if (existing != null) {
            (existing.parent as? android.view.ViewGroup)?.removeView(existing)
            return Pair(existing, true)
        }
        val newWebView = WebView(context)
        webViewCache[key] = newWebView
        return Pair(newWebView, false)
    }

    fun clearAll() {
        webViewCache.values.forEach { it.destroy() }
        webViewCache.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ExamFormWebViewScreen(
    initialUrl: String = "https://ppuponline.in/exam_form_search_student_semester.php",
    title: String = "PPU Exam Form Portal",
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableFloatStateOf(0.1f) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // File picker launcher for HTML5 file uploads
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback != null) {
            val intentData = result.data
            var results: Array<Uri>? = null
            if (result.resultCode == Activity.RESULT_OK && intentData != null) {
                val dataString = intentData.dataString
                val clipData = intentData.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
                } else if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    // Handle system back button for WebView history navigation
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBackClick()
        }
    }

    val triggerDownload: () -> Unit = {
        try {
            var urlToDownload = currentUrl.ifBlank { initialUrl }
            // If viewing through Google Docs viewer, extract original raw PDF URL
            if (urlToDownload.contains("docs.google.com/gview") && urlToDownload.contains("url=")) {
                val extracted = Uri.parse(urlToDownload).getQueryParameter("url")
                if (!extracted.isNullOrBlank()) {
                    urlToDownload = extracted
                }
            }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(urlToDownload)
            val isPdf = urlToDownload.contains(".pdf", ignoreCase = true)
            val fileName = if (isPdf) {
                "PPU_Result_${System.currentTimeMillis()}.pdf"
            } else {
                URLUtil.guessFileName(urlToDownload, null, "application/pdf")
            }
            val request = DownloadManager.Request(uri).apply {
                setTitle(title.ifBlank { "PPU Result Document" })
                setDescription("Downloading PDF directly to Mobile File Manager (Downloads folder)...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/pdf")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            dm.enqueue(request)
            Toast.makeText(context, "📥 Saved directly to Mobile File Manager (Downloads folder)!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl.ifBlank { initialUrl }))
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = Uri.parse(currentUrl).host ?: "ppuponline.in",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (webViewInstance?.canGoBack() == true) {
                                webViewInstance?.goBack()
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier.testTag("webview_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Download Button (shown ONLY when PDF/page is successfully loaded and visible)
                    if (!isLoading && !hasError) {
                        IconButton(
                            onClick = triggerDownload,
                            modifier = Modifier.testTag("webview_download_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download File",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Zoom Out Button
                    IconButton(
                        onClick = {
                            webViewInstance?.zoomOut()
                            Toast.makeText(context, "Zoom Out (-)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("webview_zoom_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out"
                        )
                    }
                    // Zoom In Button
                    IconButton(
                        onClick = {
                            webViewInstance?.zoomIn()
                            Toast.makeText(context, "Zoom In (+)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("webview_zoom_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In"
                        )
                    }
                    // Refresh button
                    IconButton(
                        onClick = {
                            hasError = false
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.testTag("webview_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Page"
                        )
                    }
                    // Open in External Browser
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("webview_external_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in External Browser"
                        )
                    }
                    // Close WebView
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("webview_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Android WebView using Persistent Holder to prevent auto-refreshing
            AndroidView(
                factory = { ctx ->
                    val (webView, isReused) = PersistentWebViewHolder.getOrCreate(ctx, initialUrl)

                    // Configure client & chrome client callbacks
                    webView.apply {
                        isFocusable = true
                        isFocusableInTouchMode = true
                        requestFocus()
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = false
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.setSupportMultipleWindows(true)
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.textZoom = 120

                        if (!isReused) {
                            setInitialScale(125)
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        setDownloadListener { downloadUrl, userAgent, contentDisposition, mimeType, contentLength ->
                            try {
                                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                                    setMimeType(mimeType)
                                    addRequestHeader("User-Agent", userAgent)
                                    val cookie = cookieManager.getCookie(downloadUrl)
                                    addRequestHeader("Cookie", cookie)
                                    setDescription("Downloading file from PPU Portal...")
                                    val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType)
                                    setTitle(fileName)
                                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                }
                                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(request)
                                Toast.makeText(ctx, "📥 File Download Started...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    ctx.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(ctx, "Download failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadingProgress = (newProgress / 100f).coerceIn(0.1f, 1.0f)
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            }

                            override fun onJsAlert(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: JsResult?
                            ): Boolean {
                                try {
                                    AlertDialog.Builder(ctx)
                                        .setTitle("Notice")
                                        .setMessage(message ?: "")
                                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                                        .setOnCancelListener { result?.cancel() }
                                        .show()
                                } catch (_: Exception) {
                                    result?.confirm()
                                }
                                return true
                            }

                            override fun onJsConfirm(
                                view: WebView?,
                                url: String?,
                                message: String?,
                                result: JsResult?
                            ): Boolean {
                                try {
                                    AlertDialog.Builder(ctx)
                                        .setTitle("Confirm")
                                        .setMessage(message ?: "")
                                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                                        .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                                        .setOnCancelListener { result?.cancel() }
                                        .show()
                                } catch (_: Exception) {
                                    result?.cancel()
                                }
                                return true
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallbackParam: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                filePathCallback?.onReceiveValue(null)
                                filePathCallback = filePathCallbackParam
                                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                }
                                try {
                                    filePickerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    filePathCallback = null
                                    return false
                                }
                                return true
                            }

                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                val result = view?.hitTestResult
                                val data = result?.extra
                                val handleTargetUrl: (String) -> Unit = { targetUrl ->
                                    val lower = targetUrl.lowercase()
                                    if (lower.contains(".pdf") && !lower.contains("docs.google.com")) {
                                        val viewerUrl = "https://docs.google.com/gview?embedded=true&url=" + Uri.encode(targetUrl)
                                        view?.loadUrl(viewerUrl)
                                        try {
                                            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                            val req = DownloadManager.Request(Uri.parse(targetUrl)).apply {
                                                setTitle("PPU Result Marksheet")
                                                setDescription("Downloading result PDF...")
                                                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "PPU_Result_${System.currentTimeMillis()}.pdf")
                                            }
                                            dm.enqueue(req)
                                            Toast.makeText(ctx, "📄 Opening Result PDF & saving to Downloads...", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                    } else {
                                        view?.loadUrl(targetUrl)
                                    }
                                }

                                if (data != null) {
                                    handleTargetUrl(data)
                                } else {
                                    val newWebView = WebView(ctx)
                                    newWebView.settings.javaScriptEnabled = true
                                    newWebView.settings.domStorageEnabled = true
                                    newWebView.webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
                                            req?.url?.toString()?.let { target ->
                                                handleTargetUrl(target)
                                            }
                                            return true
                                        }
                                    }
                                    val transport = resultMsg?.obj as? WebView.WebViewTransport
                                    transport?.webView = newWebView
                                    resultMsg?.sendToTarget()
                                }
                                return true
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
                                url?.let { currentUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                url?.let { currentUrl = it }
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?
                            ) {
                                handler?.proceed()
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    hasError = true
                                    errorMessage = error?.description?.toString() ?: "Connection failed"
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val urlStr = request?.url?.toString() ?: return false
                                val scheme = request.url?.scheme ?: ""

                                if (scheme == "tel" || scheme == "mailto" || scheme == "whatsapp" || scheme == "intent") {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                                        ctx.startActivity(intent)
                                    } catch (_: Exception) {}
                                    return true
                                }

                                if (scheme == "http" || scheme == "https") {
                                    val lowerUrl = urlStr.lowercase()
                                    if (lowerUrl.contains(".pdf") && !lowerUrl.contains("docs.google.com")) {
                                        val viewerUrl = "https://docs.google.com/gview?embedded=true&url=" + Uri.encode(urlStr)
                                        view?.loadUrl(viewerUrl)
                                        try {
                                            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                            val req = DownloadManager.Request(Uri.parse(urlStr)).apply {
                                                setTitle("PPU Result Marksheet")
                                                setDescription("Downloading result PDF...")
                                                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "PPU_Result_${System.currentTimeMillis()}.pdf")
                                            }
                                            dm.enqueue(req)
                                            Toast.makeText(ctx, "📄 Opening Result PDF & saving to Downloads...", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                        return true
                                    }
                                    return false
                                }

                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                                    ctx.startActivity(intent)
                                    return true
                                } catch (e: Exception) {
                                    return false
                                }
                            }
                        }

                        if (!isReused || url.isNullOrBlank()) {
                            loadUrl(initialUrl)
                        }
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                    if (webView.url.isNullOrBlank()) {
                        webView.loadUrl(initialUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("exam_form_webview")
            )

            // Floating Quick Zoom & Download Controls at bottom right
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            webViewInstance?.zoomOut()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Zoom",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = {
                            webViewInstance?.zoomIn()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isLoading && !hasError) {
                        Spacer(modifier = Modifier.width(6.dp))

                        VerticalDivider(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(horizontal = 2.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = triggerDownload,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("floating_download_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download File",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Download PDF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Linear Progress Indicator when page is loading
            AnimatedVisibility(
                visible = isLoading && !hasError,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            }

            // Network Error Card overlay if loading fails
            if (hasError) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudOff,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Portal Connection Error",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (errorMessage.isNotBlank()) errorMessage else "Unable to connect to the PPU Exam Form Portal. Please check your internet connection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Browser")
                            }

                            Button(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    webViewInstance?.reload()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}
