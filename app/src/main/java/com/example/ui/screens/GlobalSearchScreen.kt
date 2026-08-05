package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoticeCard
import com.example.ui.components.PyqCard
import com.example.ui.components.ResultCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    initialQuery: String,
    notices: List<NoticeEntity>,
    results: List<ResultEntity>,
    pyqs: List<PyqEntity>,
    admissions: List<AdmissionEntity>,
    scholarships: List<ScholarshipEntity>,
    onClose: () -> Unit,
    onNoticeClick: (NoticeEntity) -> Unit,
    onResultClick: (ResultEntity) -> Unit,
    onPyqClick: (PyqEntity) -> Unit,
    onBookmarkToggleNotice: (NoticeEntity) -> Unit,
    onBookmarkToggleResult: (ResultEntity) -> Unit,
    onBookmarkTogglePyq: (PyqEntity) -> Unit
) {
    var query by remember { mutableStateOf(initialQuery) }

    val matchedNotices = remember(query, notices) {
        if (query.isBlank()) emptyList()
        else notices.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    val matchedResults = remember(query, results) {
        if (query.isBlank()) emptyList()
        else results.filter { it.title.contains(query, ignoreCase = true) || it.session.contains(query, ignoreCase = true) }
    }

    val matchedPyqs = remember(query, pyqs) {
        if (query.isBlank()) emptyList()
        else pyqs.filter { it.title.contains(query, ignoreCase = true) || it.department.contains(query, ignoreCase = true) }
    }

    val totalMatches = matchedNotices.size + matchedResults.size + matchedPyqs.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search everything in PPU Portal...") },
                        singleLine = true,
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("global_search_input")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (query.isBlank()) {
                EmptyStateView(
                    title = "Search Portal",
                    subtitle = "Type keywords like 'UG Result', 'Admit Card', 'Physics', 'BCA', 'Notice' to find official records.",
                    icon = Icons.Outlined.Search
                )
            } else if (totalMatches == 0) {
                EmptyStateView(
                    title = "No Matches Found",
                    subtitle = "No notices, results, or PYQs matched '$query'.",
                    icon = Icons.Filled.Search
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (matchedNotices.isNotEmpty()) {
                        item {
                            Text(
                                text = "Matching Notices (${matchedNotices.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(matchedNotices) { notice ->
                            NoticeCard(
                                notice = notice,
                                onNoticeClick = onNoticeClick,
                                onBookmarkToggle = onBookmarkToggleNotice
                            )
                        }
                    }

                    if (matchedResults.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Matching Results (${matchedResults.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(matchedResults) { result ->
                            ResultCard(
                                result = result,
                                onResultClick = onResultClick,
                                onBookmarkToggle = onBookmarkToggleResult
                            )
                        }
                    }

                    if (matchedPyqs.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Matching Question Papers (${matchedPyqs.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(matchedPyqs) { pyq ->
                            PyqCard(
                                pyq = pyq,
                                onPyqClick = onPyqClick,
                                onBookmarkToggle = onBookmarkTogglePyq
                            )
                        }
                    }
                }
            }
        }
    }
}
