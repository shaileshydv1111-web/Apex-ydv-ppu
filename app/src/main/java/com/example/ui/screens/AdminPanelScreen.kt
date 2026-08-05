package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    notices: List<NoticeEntity>,
    results: List<ResultEntity>,
    pyqs: List<PyqEntity>,
    studentSuggestions: List<StudentSuggestionEntity> = emptyList(),
    onClose: () -> Unit,
    onLogout: (() -> Unit)? = null,
    onPublishNotice: (title: String, category: String, description: String, pdfUrl: String, isImportant: Boolean) -> Unit,
    onPublishResult: (title: String, course: String, session: String, pdfUrl: String) -> Unit,
    onPublishPyq: (title: String, course: String, department: String, year: String, semester: String, pdfUrl: String) -> Unit,
    onSendBroadcastNotification: (title: String, body: String, type: String) -> Unit,
    onDeleteNotice: (String) -> Unit,
    onDeleteResult: (String) -> Unit,
    onDeletePyq: (String) -> Unit,
    onDeleteSuggestion: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Form States
    // Notice Form
    var noticeTitle by remember { mutableStateOf("") }
    var noticeCategory by remember { mutableStateOf("Exam") }
    var noticeDesc by remember { mutableStateOf("") }
    var noticePdfUrl by remember { mutableStateOf("") }
    var noticeIsImportant by remember { mutableStateOf(false) }

    // Result Form
    var resultTitle by remember { mutableStateOf("") }
    var resultCourse by remember { mutableStateOf("UG") }
    var resultSession by remember { mutableStateOf("2024-2027") }
    var resultPdfUrl by remember { mutableStateOf("") }

    // PYQ Form
    var pyqTitle by remember { mutableStateOf("") }
    var pyqCourse by remember { mutableStateOf("BCA") }
    var pyqDept by remember { mutableStateOf("Computer Applications") }
    var pyqYear by remember { mutableStateOf("2026") }
    var pyqSem by remember { mutableStateOf("Sem 1") }
    var pyqPdfUrl by remember { mutableStateOf("") }

    // Broadcast Notif Form
    var notifTitle by remember { mutableStateOf("") }
    var notifBody by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PPU Admin Control Panel", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("admin_close_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onLogout != null) {
                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("admin_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Logout",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("admin_panel_screen")
        ) {
            // Admin Action Tabs
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Publish Notice") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Publish Result") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Upload PYQ") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Push Broadcast") }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Manage Content") }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    text = { Text("Suggestions (${studentSuggestions.size})") }
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tab 0: Notice Publisher Form
                if (selectedTab == 0) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Publish New Official Notice", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = noticeTitle,
                                    onValueChange = { noticeTitle = it },
                                    label = { Text("Notice Title") },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_notice_title_input")
                                )

                                OutlinedTextField(
                                    value = noticeCategory,
                                    onValueChange = { noticeCategory = it },
                                    label = { Text("Category (Exam, Admission, General, Sports)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = noticeDesc,
                                    onValueChange = { noticeDesc = it },
                                    label = { Text("Notice Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = noticePdfUrl,
                                    onValueChange = { noticePdfUrl = it },
                                    label = { Text("PDF Document Link URL") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = noticeIsImportant,
                                        onCheckedChange = { noticeIsImportant = it }
                                    )
                                    Text("Mark as URGENT Priority Notice")
                                }

                                Button(
                                    onClick = {
                                        if (noticeTitle.isNotBlank()) {
                                            onPublishNotice(noticeTitle, noticeCategory, noticeDesc, noticePdfUrl, noticeIsImportant)
                                            noticeTitle = ""
                                            noticeDesc = ""
                                            noticePdfUrl = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_notice_publish_button")
                                ) {
                                    Icon(Icons.Filled.Publish, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Publish Notice Now")
                                }
                            }
                        }
                    }
                }

                // Tab 1: Result Publisher Form
                if (selectedTab == 1) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Publish New Examination Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = resultTitle,
                                    onValueChange = { resultTitle = it },
                                    label = { Text("Result Title (e.g. UG Part 3 Honours Result)") },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_result_title_input")
                                )

                                OutlinedTextField(
                                    value = resultCourse,
                                    onValueChange = { resultCourse = it },
                                    label = { Text("Course (UG, PG, Vocational, B.Tech, B.Ed)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = resultSession,
                                    onValueChange = { resultSession = it },
                                    label = { Text("Academic Session (e.g. 2024-2027)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = resultPdfUrl,
                                    onValueChange = { resultPdfUrl = it },
                                    label = { Text("Result PDF URL") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (resultTitle.isNotBlank()) {
                                            onPublishResult(resultTitle, resultCourse, resultSession, resultPdfUrl)
                                            resultTitle = ""
                                            resultPdfUrl = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_result_publish_button")
                                ) {
                                    Icon(Icons.Filled.FactCheck, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Publish Result Record")
                                }
                            }
                        }
                    }
                }

                // Tab 2: PYQ Upload Form
                if (selectedTab == 2) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Upload Question Paper (PYQ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = pyqTitle,
                                    onValueChange = { pyqTitle = it },
                                    label = { Text("Paper Title (e.g. Data Structures & C++)") },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_pyq_title_input")
                                )

                                OutlinedTextField(
                                    value = pyqCourse,
                                    onValueChange = { pyqCourse = it },
                                    label = { Text("Course (BCA, B.Sc, B.A, B.Com, M.Sc)") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = pyqDept,
                                    onValueChange = { pyqDept = it },
                                    label = { Text("Department") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = pyqYear,
                                        onValueChange = { pyqYear = it },
                                        label = { Text("Year") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pyqSem,
                                        onValueChange = { pyqSem = it },
                                        label = { Text("Semester") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                OutlinedTextField(
                                    value = pyqPdfUrl,
                                    onValueChange = { pyqPdfUrl = it },
                                    label = { Text("Question Paper PDF URL") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (pyqTitle.isNotBlank()) {
                                            onPublishPyq(pyqTitle, pyqCourse, pyqDept, pyqYear, pyqSem, pyqPdfUrl)
                                            pyqTitle = ""
                                            pyqPdfUrl = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_pyq_publish_button")
                                ) {
                                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Upload PYQ Paper")
                                }
                            }
                        }
                    }
                }

                // Tab 3: Broadcast Push Notification
                if (selectedTab == 3) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Send Push Notification Broadcast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)

                                OutlinedTextField(
                                    value = notifTitle,
                                    onValueChange = { notifTitle = it },
                                    label = { Text("Notification Title") },
                                    modifier = Modifier.fillMaxWidth().testTag("admin_notif_title_input")
                                )

                                OutlinedTextField(
                                    value = notifBody,
                                    onValueChange = { notifBody = it },
                                    label = { Text("Message Body") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (notifTitle.isNotBlank()) {
                                            onSendBroadcastNotification(notifTitle, notifBody, "System")
                                            notifTitle = ""
                                            notifBody = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_broadcast_button")
                                ) {
                                    Icon(Icons.Filled.Campaign, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Broadcast Push Notification")
                                }
                            }
                        }
                    }
                }

                // Tab 4: Manage & Delete items
                if (selectedTab == 4) {
                    item {
                        Text("Active Notices (${notices.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(notices) { notice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(notice.title, maxLines = 1, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteNotice(notice.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Active Results (${results.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(results) { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(result.title, maxLines = 1, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteResult(result.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Tab 5: Student Suggestions Received
                if (selectedTab == 5) {
                    item {
                        Text(
                            text = "📥 Received Student Suggestions & Feedback (${studentSuggestions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (studentSuggestions.isEmpty()) {
                        item {
                            Text(
                                text = "No student suggestions submitted yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(studentSuggestions) { suggestion ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "👤 ${suggestion.studentName}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = suggestion.studentEmail,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { onDeleteSuggestion(suggestion.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Text(
                                        text = "💬 \"${suggestion.suggestionText}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "🕒 ${suggestion.dateString}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
