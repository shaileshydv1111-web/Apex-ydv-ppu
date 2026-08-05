package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PyqEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PyqCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PyqScreen(
    pyqs: List<PyqEntity>,
    selectedCourseFilter: String,
    onCourseFilterSelect: (String) -> Unit,
    onPyqClick: (PyqEntity) -> Unit,
    onBookmarkToggle: (PyqEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val courseList = listOf("All", "BCA", "B.Sc", "B.A", "B.Com", "M.Sc", "M.A", "BBA", "B.Ed")

    val filteredPyqs = remember(pyqs, selectedCourseFilter, searchQuery) {
        pyqs.filter { pyq ->
            val matchesCourse = if (selectedCourseFilter == "All") true else pyq.course.equals(selectedCourseFilter, ignoreCase = true)
            val matchesQuery = if (searchQuery.isBlank()) true else {
                pyq.title.contains(searchQuery, ignoreCase = true) ||
                pyq.department.contains(searchQuery, ignoreCase = true) ||
                pyq.year.contains(searchQuery, ignoreCase = true)
            }
            matchesCourse && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pyq_screen")
    ) {
        // Search & Course Filter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Subject or Department (e.g. Java, Physics)...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pyq_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Course filter horizontal row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(courseList) { course ->
                    val isSelected = selectedCourseFilter == course
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCourseFilterSelect(course) },
                        label = { Text(course) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("pyq_chip_$course")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PYQ List
        if (filteredPyqs.isEmpty()) {
            EmptyStateView(
                title = "No Question Papers Found",
                subtitle = "Try selecting another course or searching by department.",
                icon = Icons.Filled.School
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPyqs) { pyq ->
                    PyqCard(
                        pyq = pyq,
                        onPyqClick = onPyqClick,
                        onBookmarkToggle = onBookmarkToggle
                    )
                }
            }
        }
    }
}
