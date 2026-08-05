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

data class PpuUpdateEntity(
    val id: String,
    val title: String,
    val fullText: String,
    val date: String,
    val postUrl: String,
    val timestamp: Long
)

object PpuUpdateFetcher {

    private const val CACHE_FILE_NAME = "ppu_telegram_updates_cache.json"
    private const val TELEGRAM_CHANNEL_URL = "https://t.me/s/PPUPatnaInfo"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val widgetMessageRegex = Regex(
        """(<div class="tgme_widget_message\s.*?(?=<div class="tgme_widget_message\s|</body>))""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    private val dataPostRegex = Regex("""data-post="([^"]+)"""", RegexOption.IGNORE_CASE)
    private val textDivRegex = Regex("""<div class="tgme_widget_message_text[^"]*"[^>]*>(.*?)</div>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val timeTagRegex = Regex("""<time[^>]*datetime="([^"]+)"[^>]*>([^<]+)</time>""", RegexOption.IGNORE_CASE)

    suspend fun fetchUpdatesFromTelegram(): List<PpuUpdateEntity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(TELEGRAM_CHANNEL_URL)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw java.io.IOException("HTTP error ${response.code}")
        }

        val html = response.body?.string() ?: throw java.io.IOException("Empty response body")
        parseUpdatesFromHtml(html)
    }

    fun parseUpdatesFromHtml(html: String): List<PpuUpdateEntity> {
        val blocks = widgetMessageRegex.findAll(html).map { it.value }.toList()
        val updates = mutableListOf<PpuUpdateEntity>()

        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val displayFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)

        for (block in blocks) {
            val postMatch = dataPostRegex.find(block) ?: continue
            val postIdFull = postMatch.groupValues[1]
            val postNumber = postIdFull.substringAfterLast("/")
            val postShareUrl = "https://t.me/PPUPatnaInfo/$postNumber"

            val textMatch = textDivRegex.find(block)
            val textRaw = textMatch?.groupValues?.get(1) ?: ""

            val cleanText = textRaw
                .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<[^>]+>"), "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&#33;", "!")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace(Regex("""\n\s*\n"""), "\n")
                .trim()

            if (cleanText.isBlank()) continue

            val lines = cleanText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
            val title = if (lines.isNotEmpty()) lines.first() else "PPU Update"

            val timeMatch = timeTagRegex.find(block)
            val isoString = timeMatch?.groupValues?.get(1) ?: ""
            val rawTimeText = timeMatch?.groupValues?.get(2) ?: ""

            var timestamp = System.currentTimeMillis()
            var formattedDate = rawTimeText

            if (isoString.isNotBlank()) {
                try {
                    val cleanIso = isoString.take(19) // yyyy-MM-ddTHH:mm:ss
                    val parsedDate = isoFormat.parse(cleanIso)
                    if (parsedDate != null) {
                        timestamp = parsedDate.time
                        formattedDate = displayFormat.format(parsedDate)
                    }
                } catch (_: Exception) {
                }
            }

            updates.add(
                PpuUpdateEntity(
                    id = "tg_$postNumber",
                    title = title,
                    fullText = cleanText,
                    date = formattedDate,
                    postUrl = postShareUrl,
                    timestamp = timestamp
                )
            )
        }

        // Show newest posts first
        return updates.sortedByDescending { it.timestamp }
    }

    // Cache management
    fun saveToCache(context: Context, updates: List<PpuUpdateEntity>) {
        try {
            val jsonArray = JSONArray()
            for (update in updates) {
                val obj = JSONObject().apply {
                    put("id", update.id)
                    put("title", update.title)
                    put("fullText", update.fullText)
                    put("date", update.date)
                    put("postUrl", update.postUrl)
                    put("timestamp", update.timestamp)
                }
                jsonArray.put(obj)
            }
            val file = File(context.cacheDir, CACHE_FILE_NAME)
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFromCache(context: Context): List<PpuUpdateEntity> {
        val updates = mutableListOf<PpuUpdateEntity>()
        try {
            val file = File(context.cacheDir, CACHE_FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readText()
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    updates.add(
                        PpuUpdateEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            fullText = obj.optString("fullText", ""),
                            date = obj.getString("date"),
                            postUrl = obj.getString("postUrl"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return updates.sortedByDescending { it.timestamp }
    }
}
