package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoticeEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NoticeCard
import com.example.ui.components.ShimmerLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticesScreen(
    notices: List<NoticeEntity>,
    isRefreshing: Boolean = false,
    errorMessage: String? = null,
    selectedCategoryFilter: String,
    onCategoryFilterSelect: (String) -> Unit,
    onRefresh: () -> Unit,
    onNoticeClick: (NoticeEntity) -> Unit,
    onBookmarkToggle: (NoticeEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Exam", "Admission", "General", "Academic", "Sports")

    // Urgent/Important notices pinned at TOP (sorted by newest date), followed by remaining notices (sorted by newest date)
    val sortedNotices = remember(notices) {
        val (urgentOrImportant, normal) = notices.partition { notice ->
            notice.isImportant ||
            notice.title.contains("urgent", ignoreCase = true) ||
            notice.title.contains("important", ignoreCase = true) ||
            notice.title.contains("🔴") ||
            notice.title.contains("⚠️")
        }
        val sortedPinned = urgentOrImportant.sortedWith(
            compareByDescending<NoticeEntity> { notice ->
                val titleLower = notice.title.lowercase()
                if (titleLower.contains("urgent") || titleLower.contains("🔴")) 2 else 1
            }.thenByDescending { it.timestamp }
        )
        val sortedNormal = normal.sortedByDescending { it.timestamp }
        sortedPinned + sortedNormal
    }

    val filteredNotices = remember(sortedNotices, selectedCategoryFilter, searchQuery) {
        sortedNotices.filter { notice ->
            val matchesCategory = if (selectedCategoryFilter == "All") true else notice.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesQuery = if (searchQuery.isBlank()) true else {
                notice.title.contains(searchQuery, ignoreCase = true) ||
                notice.description.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .testTag("notices_screen_pull_refresh")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("notices_screen")
        ) {
            // Header Bar with Notice Board Title & Manual Refresh Button
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📢 PPU Notice Board",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Source: ppup.ac.in/notice-board",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("notices_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh Notices",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Search & Category Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notices and circulars...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notices_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryFilterSelect(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("notices_chip_$category")
                        )
                    }
                }
            }

            // Loading Indicator Bar
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Content Area
            when {
                // Case 1: Fetching initially and no notices cached yet
                isRefreshing && filteredNotices.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Patliputra University Notice Board से ताज़ा नोटिस लोड हो रहे हैं...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        ShimmerLoader()
                    }
                }

                // Case 2: Error state with no notices available
                errorMessage != null && filteredNotices.isEmpty() -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WifiOff,
                                    contentDescription = "No Connection",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("पुनः प्रयास करें (Retry)")
                                }
                            }
                        }
                    }
                }

                // Case 3: Empty notices list (No matching or no available)
                filteredNotices.isEmpty() -> {
                    EmptyStateView(
                        title = "कोई नया नोटिस उपलब्ध नहीं है।",
                        subtitle = if (searchQuery.isNotBlank()) "आपकी खोज '$searchQuery' से संबंधित कोई नोटिस नहीं मिला।" else "पाटलिपुत्र विश्वविद्यालय नोटिस बोर्ड पर वर्तमान में कोई नोटिस उपलब्ध नहीं है।",
                        icon = Icons.Filled.Campaign
                    )
                }

                // Case 4: Display Notices List in Material Design Cards
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "📌 Notices (Latest → Oldest)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${filteredNotices.size} Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(
                            items = filteredNotices,
                            key = { notice -> notice.id }
                        ) { notice ->
                            NoticeCard(
                                notice = notice,
                                onNoticeClick = onNoticeClick,
                                onBookmarkToggle = onBookmarkToggle
                            )
                        }
                    }
                }
            }
        }
    }
}
