package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object NoticeFetcher {

    private const val CACHE_FILE_NAME = "ppu_notices_cache.json"
    private const val NOTICE_BOARD_URL = "https://ppup.ac.in/notice-board"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val liRegex = Regex("""<li>(.*?)</li>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val hrefRegex = Regex("""href=["']?([^"'\s>]+)""", RegexOption.IGNORE_CASE)
    private val dateRegex = Regex("""Updated On\s*:\s*([^<]+)""", RegexOption.IGNORE_CASE)
    private val spanRegex = Regex("""<span>.*?</span>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val htmlTagRegex = Regex("""<[^>]+>""")

    suspend fun fetchNoticesFromWeb(): List<NoticeEntity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(NOTICE_BOARD_URL)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw java.io.IOException("HTTP error code ${response.code}")
        }

        val html = response.body?.string() ?: throw java.io.IOException("Empty response body")
        parseNoticesFromHtml(html)
    }

    private val dateFormats = listOf(
        SimpleDateFormat("dd-MM-yyyy", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("dd.MM.yyyy", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd-MMM-yyyy", Locale.US),
        SimpleDateFormat("dd MMM yyyy", Locale.US),
        SimpleDateFormat("dd MMMM yyyy", Locale.US)
    )

    private val regexDate = Regex("""\b(\d{1,2})[-/.]([0-9]{1,2}|[A-Za-z]{3,9})[-/.](\d{2,4})\b""")

    private fun parseDateToMillis(dateStr: String): Long? {
        val trimmed = dateStr.trim()
        for (fmt in dateFormats) {
            try {
                val date = fmt.parse(trimmed)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        val match = regexDate.find(trimmed)
        if (match != null) {
            val extracted = match.value
            for (fmt in dateFormats) {
                try {
                    val date = fmt.parse(extracted)
                    if (date != null) return date.time
                } catch (_: Exception) {}
            }
        }
        return null
    }

    fun parseNoticesFromHtml(html: String): List<NoticeEntity> {
        val notices = mutableListOf<NoticeEntity>()
        val seenUrls = mutableSetOf<String>()

        val liBlocks = liRegex.findAll(html).toList()
        var validIndex = 0

        for (match in liBlocks) {
            val liContent = match.groupValues[1]
            val hrefMatch = hrefRegex.find(liContent)
            val dateMatch = dateRegex.find(liContent)

            if (hrefMatch == null || dateMatch == null) continue

            val rawHref = hrefMatch.groupValues[1]
            val cleanDateStr = dateMatch.groupValues[1].trim().ifBlank { "Latest" }

            val formattedLink = formatNoticeUrl(rawHref)
            if (seenUrls.contains(formattedLink)) continue
            seenUrls.add(formattedLink)

            // Clean Title
            val liNoSpan = spanRegex.replace(liContent, "")
            val cleanTitle = cleanHtmlText(liNoSpan)

            if (cleanTitle.isBlank() || cleanTitle.length < 3) continue

            // Parse timestamp so newest stays top
            val parsedTime = parseDateToMillis(cleanDateStr)
            val timestamp: Long = if (parsedTime != null) {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = parsedTime
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                cal.timeInMillis - (validIndex * 1000L)
            } else {
                System.currentTimeMillis() - (validIndex * 1000L)
            }

            val category = inferCategory(cleanTitle)

            val lowerTitle = cleanTitle.lowercase()
            val lowerLi = liContent.lowercase()

            val isUrgentOrImportant = lowerTitle.contains("urgent") ||
                    lowerTitle.contains("important") ||
                    lowerTitle.contains("🔴") ||
                    lowerTitle.contains("⚠️") ||
                    lowerLi.contains("color=\"red\"") ||
                    lowerLi.contains("color:red") ||
                    lowerLi.contains("color: red") ||
                    lowerLi.contains("new.gif") ||
                    lowerLi.contains("blink") ||
                    lowerLi.contains("class=\"urgent\"") ||
                    lowerLi.contains("class=\"important\"")

            notices.add(
                NoticeEntity(
                    id = "ppu_live_${formattedLink.hashCode()}",
                    title = cleanTitle,
                    category = category,
                    date = cleanDateStr,
                    description = "Official Notice published on Patliputra University Notice Board.",
                    pdfUrl = formattedLink,
                    isImportant = isUrgentOrImportant,
                    isBookmarked = false,
                    timestamp = timestamp
                )
            )
            validIndex++
        }

        // Sort: Urgent/Important notices pinned at top (sorted by latest date DESC), followed by normal notices (sorted by latest date DESC)
        val (importantList, normalList) = notices.partition { it.isImportant }
        return importantList.sortedByDescending { it.timestamp } + normalList.sortedByDescending { it.timestamp }
    }

    private fun cleanHtmlText(rawHtml: String): String {
        return rawHtml
            .replace(Regex("<[^>]+>"), " ") // Remove HTML tags
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("""\s+"""), " ") // Normalize multiple spaces
            .trim()
    }

    private fun formatNoticeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return when {
            trimmed.startsWith("//") -> "https:$trimmed"
            trimmed.startsWith("/") -> "https://ppup.ac.in$trimmed"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "https://ppup.ac.in/$trimmed"
        }
    }

    private fun inferCategory(title: String): String {
        val lower = title.lowercase()
        return when {
            lower.contains("exam") || lower.contains("schedule") || lower.contains("program") || lower.contains("admit") || lower.contains("scrutiny") -> "Exam"
            lower.contains("admission") || lower.contains("merit") || lower.contains("cutoff") || lower.contains("reg.") || lower.contains("llb") -> "Admission"
            lower.contains("sports") -> "Sports"
            lower.contains("ph.d") || lower.contains("viva") || lower.contains("syllabus") -> "Academic"
            else -> "General"
        }
    }

    // --- Cache Management ---
    fun saveToCache(context: Context, notices: List<NoticeEntity>) {
        try {
            val jsonArray = JSONArray()
            for (notice in notices) {
                val obj = JSONObject().apply {
                    put("id", notice.id)
                    put("title", notice.title)
                    put("category", notice.category)
                    put("date", notice.date)
                    put("description", notice.description)
                    put("pdfUrl", notice.pdfUrl)
                    put("isImportant", notice.isImportant)
                    put("isBookmarked", notice.isBookmarked)
                    put("timestamp", notice.timestamp)
                }
                jsonArray.put(obj)
            }
            val file = File(context.cacheDir, CACHE_FILE_NAME)
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFromCache(context: Context): List<NoticeEntity> {
        val notices = mutableListOf<NoticeEntity>()
        try {
            val file = File(context.cacheDir, CACHE_FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readText()
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    notices.add(
                        NoticeEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            category = obj.optString("category", "General"),
                            date = obj.getString("date"),
                            description = obj.optString("description", ""),
                            pdfUrl = obj.getString("pdfUrl"),
                            isImportant = obj.optBoolean("isImportant", false),
                            isBookmarked = obj.optBoolean("isBookmarked", false),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val (importantList, normalList) = notices.partition { it.isImportant }
        return importantList.sortedByDescending { it.timestamp } + normalList.sortedByDescending { it.timestamp }
    }
}
