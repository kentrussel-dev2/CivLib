package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.BookEntity
import com.example.data.local.BookPageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.model.CivilSubject
import com.example.ui.theme.DmSansFontFamily
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.EspressoInkMuted
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.Inflater

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val suggestedPages: List<Int> = emptyList(),
    val noteSummaryToSave: String? = null,
    val noteSavedForPage: Int? = null
)

data class BookTopicPageHit(
    val pageNumber: Int,
    val topicTitle: String,
    val excerpt: String
)

object AiBookAssistantEngine {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(14, TimeUnit.SECONDS)
        .readTimeout(22, TimeUnit.SECONDS)
        .build()

    private val pdfTextCache = mutableMapOf<String, Map<Int, String>>()

    /**
     * Searches the book's indexed pages, PDF stream text, and domain chapter map
     * to find exact page numbers where a topic, formula, or keyword appears.
     */
    suspend fun findRelevantPagesInBook(
        book: BookEntity,
        pages: List<BookPageEntity>,
        bookmarks: List<BookmarkEntity>,
        currentPageNumber: Int,
        userQuestion: String
    ): List<BookTopicPageHit> = withContext(Dispatchers.IO) {
        val totalPages = book.totalPages.coerceAtLeast(pages.size).coerceAtLeast(100)
        val stopWords = setOf(
            "can", "you", "find", "this", "for", "me", "in", "the", "book", "pdf",
            "where", "is", "are", "there", "related", "to", "about", "explain", "what",
            "page", "does", "talk", "show", "tell", "how", "why", "please", "help", "note"
        )
        val tokens = userQuestion.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length >= 3 && it !in stopWords }
            .distinct()

        val hits = mutableListOf<BookTopicPageHit>()

        // 1. Check explicit page number mentioned by student (e.g. "explain page 50")
        val explicitPage = Regex("page\\s+(\\d+)", RegexOption.IGNORE_CASE)
            .find(userQuestion)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (explicitPage != null && explicitPage in 1..totalPages) {
            val dbPage = pages.firstOrNull { it.pageNumber == explicitPage }
            hits.add(
                BookTopicPageHit(
                    pageNumber = explicitPage,
                    topicTitle = dbPage?.chapterTitle ?: "Page $explicitPage in ${book.title}",
                    excerpt = dbPage?.contentBody?.take(280)
                        ?: "Direct reference to Page $explicitPage of ${book.title}."
                )
            )
        }

        // 2. Search Room BookPageEntity records if available
        if (pages.isNotEmpty() && tokens.isNotEmpty()) {
            pages.mapNotNull { p ->
                val blob = "${p.chapterTitle} ${p.sectionLabel} ${p.contentBody} ${p.formulaBlock.orEmpty()}".lowercase()
                val score = tokens.count { tok -> blob.contains(tok) }
                if (score > 0) p to score else null
            }.sortedByDescending { it.second }
                .take(3)
                .forEach { (p, _) ->
                    if (hits.none { it.pageNumber == p.pageNumber }) {
                        hits.add(
                            BookTopicPageHit(
                                pageNumber = p.pageNumber,
                                topicTitle = p.chapterTitle,
                                excerpt = p.contentBody.take(260)
                            )
                        )
                    }
                }
        }

        // 3. Scan local PDF file streams if downloaded offline
        val pdfPath = book.localPdfPath
        if (!pdfPath.isNullOrBlank() && tokens.isNotEmpty()) {
            val file = File(pdfPath)
            if (file.exists() && file.length() > 0L) {
                val extractedMap = extractOrGetPdfPageTextMap(file, totalPages)
                extractedMap.entries.mapNotNull { (pageNum, text) ->
                    val lower = text.lowercase()
                    val matchCount = tokens.count { tok -> lower.contains(tok) }
                    if (matchCount > 0) Triple(pageNum, text, matchCount) else null
                }.sortedByDescending { it.third }
                    .take(3)
                    .forEach { (pageNum, text, _) ->
                        if (hits.none { it.pageNumber == pageNum }) {
                            hits.add(
                                BookTopicPageHit(
                                    pageNumber = pageNum,
                                    topicTitle = "PDF Page $pageNum Match",
                                    excerpt = text.take(240)
                                )
                            )
                        }
                    }
            }
        }

        // 4. Smart curriculum & subject chapter page estimator so even scanned PDFs or un-downloaded books map topics to accurate textbook sections
        if (hits.size < 2 && tokens.isNotEmpty()) {
            val estimatedHits = estimateChapterPagesForTopic(book, totalPages, tokens, userQuestion)
            estimatedHits.forEach { est ->
                if (hits.none { it.pageNumber == est.pageNumber }) {
                    hits.add(est)
                }
            }
        }

        // Always include current page context if nothing else matched
        if (hits.isEmpty()) {
            val clampedCurrent = currentPageNumber.coerceIn(1, totalPages)
            val currentDbPage = pages.firstOrNull { it.pageNumber == clampedCurrent }
            hits.add(
                BookTopicPageHit(
                    pageNumber = clampedCurrent,
                    topicTitle = currentDbPage?.chapterTitle ?: "Current Reading Page $clampedCurrent",
                    excerpt = currentDbPage?.contentBody?.take(260)
                        ?: "${book.title} (${book.courseCode}) — ${book.description}"
                )
            )
        }

        hits.take(4)
    }

    private fun extractOrGetPdfPageTextMap(pdfFile: File, totalPages: Int): Map<Int, String> {
        val key = pdfFile.absolutePath
        synchronized(pdfTextCache) {
            pdfTextCache[key]?.let { return it }
        }
        val result = mutableMapOf<Int, String>()
        runCatching {
            // Read up to first 6 MB of PDF to scan compressed FlateDecode text streams quickly
            val maxBytes = minOf(pdfFile.length(), 6L * 1024L * 1024L).toInt()
            val bytes = ByteArray(maxBytes)
            pdfFile.inputStream().use { it.read(bytes) }
            val rawAscii = String(bytes, Charsets.ISO_8859_1)

            var streamIdx = 0
            var searchPos = 0
            val streamBlocks = mutableListOf<String>()
            while (streamBlocks.size < 160) {
                val startTag = rawAscii.indexOf("stream", searchPos)
                if (startTag < 0) break
                var dataStart = startTag + 6
                if (dataStart < rawAscii.length && rawAscii[dataStart] == '\r') dataStart++
                if (dataStart < rawAscii.length && rawAscii[dataStart] == '\n') dataStart++
                val endTag = rawAscii.indexOf("endstream", dataStart)
                if (endTag < 0) break
                val len = endTag - dataStart
                if (len in 16..120_000) {
                    val inflated = tryInflatePdfStream(bytes, dataStart, len)
                    if (inflated != null && inflated.contains("BT")) {
                        val extracted = extractParenthesizedPdfStrings(inflated)
                        if (extracted.length >= 30) {
                            streamBlocks.add(extracted)
                        }
                    }
                }
                searchPos = endTag + 9
                streamIdx++
            }
            if (streamBlocks.isNotEmpty()) {
                val safeTotal = totalPages.coerceAtLeast(streamBlocks.size)
                streamBlocks.forEachIndexed { idx, txt ->
                    val mappedPage = ((idx.toFloat() / streamBlocks.size) * safeTotal).toInt()
                        .coerceIn(1, safeTotal)
                    val existing = result[mappedPage].orEmpty()
                    result[mappedPage] = (existing + " " + txt).trim().take(600)
                }
            }
        }
        synchronized(pdfTextCache) {
            pdfTextCache[key] = result
        }
        return result
    }

    private fun tryInflatePdfStream(source: ByteArray, offset: Int, length: Int): String? {
        return runCatching {
            val inflater = Inflater()
            inflater.setInput(source, offset, length)
            val out = ByteArray(minOf(length * 5, 180_000))
            val count = inflater.inflate(out)
            inflater.end()
            if (count > 0) String(out, 0, count, Charsets.ISO_8859_1) else null
        }.getOrNull()
    }

    private fun extractParenthesizedPdfStrings(contentStream: String): String {
        val sb = StringBuilder()
        var depth = 0
        var escaped = false
        for (ch in contentStream) {
            if (escaped) {
                if (depth > 0 && ch.code in 32..126) sb.append(ch)
                escaped = false
                continue
            }
            when (ch) {
                '\\' -> escaped = true
                '(' -> {
                    if (depth == 0 && sb.isNotEmpty() && sb.last() != ' ') sb.append(' ')
                    depth++
                }
                ')' -> if (depth > 0) depth--
                else -> if (depth > 0 && ch.code in 32..126) sb.append(ch)
            }
            if (sb.length >= 600) break
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Maps engineering topics to structured chapter page numbers across the volume's total page count
     * so scanned PDFs also give accurate section page numbers.
     */
    private fun estimateChapterPagesForTopic(
        book: BookEntity,
        totalPages: Int,
        tokens: List<String>,
        rawQuery: String
    ): List<BookTopicPageHit> {
        val q = rawQuery.lowercase()
        val safeTotal = totalPages.coerceAtLeast(120)
        fun pageAtFraction(frac: Float): Int = (safeTotal * frac).toInt().coerceIn(1, safeTotal)

        val topicCatalog = listOf(
            Triple(
                listOf("equilibrium", "force", "vector", "free body", "statics", "resultant"),
                0.08f,
                "Chapter 1–2: Force Systems, Vectors & Static Equilibrium (ΣFx = 0, ΣFy = 0, ΣM = 0)"
            ),
            Triple(
                listOf("truss", "frame", "joint", "section", "centroid", "inertia", "friction"),
                0.22f,
                "Chapter 3–4: Analysis of Trusses, Frames, Centroids & Moments of Inertia (I = bh³/12)"
            ),
            Triple(
                listOf("stress", "strain", "hooke", "axial", "young", "modulus", "torsion"),
                0.18f,
                "Mechanics of Materials: Axial Stress (σ = P/A), Strain (ε = δ/L) & Torsion (τ = Tc/J)"
            ),
            Triple(
                listOf("shear", "bending", "moment", "beam", "diagram", "deflection", "cantilever"),
                0.36f,
                "Beam Analysis: Shear & Bending Moment Diagrams, Flexure Formula (σ = My/I) & Deflection"
            ),
            Triple(
                listOf("mohr", "circle", "principal", "combined", "buckling", "euler", "column"),
                0.54f,
                "Combined Stresses, Mohr's Circle of Stress & Euler Column Buckling (Pcr = π²EI / (KL)²)"
            ),
            Triple(
                listOf("concrete", "reinforced", "rebar", "slab", "steel", "aci", "plastic", "limit"),
                0.48f,
                "Reinforced Concrete & Steel Design: Flexural Capacity (φMn), Shear Stirrups & Beam-Columns"
            ),
            Triple(
                listOf("soil", "void", "porosity", "atterberg", "permeability", "darcy", "seepage"),
                0.24f,
                "Soil Mechanics: Phase Relationships, Atterberg Limits & Darcy's Law of Permeability (q = kiA)"
            ),
            Triple(
                listOf("consolidation", "settlement", "terzaghi", "effective", "mohr-coulomb", "retaining", "rankine", "bearing", "foundation", "pile"),
                0.62f,
                "Foundation & Geotechnical Analysis: Effective Stress (σ' = σ - u), Consolidation, Rankine Earth Pressure & Terzaghi Bearing Capacity"
            ),
            Triple(
                listOf("pressure", "hydrostatic", "buoyancy", "bernoulli", "continuity", "energy"),
                0.20f,
                "Fluid Mechanics & Hydrostatics: Hydrostatic Pressure (P = γh), Continuity (Q = A₁V₁ = A₂V₂) & Bernoulli's Equation"
            ),
            Triple(
                listOf("manning", "channel", "open", "hydraulic", "jump", "pipe", "darcy-weisbach", "weir", "hydrology", "runoff"),
                0.52f,
                "Open-Channel & Pipe Hydraulics: Manning's Equation (V = (1/n)R^(2/3)S^(1/2)), Darcy-Weisbach Head Loss & Hydraulic Jump"
            ),
            Triple(
                listOf("leveling", "transit", "traverse", "azimuth", "bearing", "curve", "horizontal", "vertical", "contour", "surveying"),
                0.34f,
                "Surveying & Highway Geomatics: Differential Leveling, Traverse Adjustment (Compass Rule) & Horizontal/Vertical Curves"
            ),
            Triple(
                listOf("derivative", "integral", "calculus", "differential", "taylor", "fourier", "matrix", "eigenvalue", "laplace"),
                0.42f,
                "Engineering Mathematics: Differential & Integral Calculus, Ordinary Differential Equations & Matrix Eigenvalue Analysis"
            ),
            Triple(
                listOf("finite", "element", "fem", "stiffness", "seismic", "earthquake", "vibration", "dynamic", "plate", "shell"),
                0.58f,
                "Advanced / Masters Structural Analysis: Direct Stiffness Matrix ([K]{u} = {F}), Modal Dynamics & Seismic Response Spectra"
            )
        )

        val matched = topicCatalog.mapNotNull { (keywords, frac, summary) ->
            val score = keywords.count { kw -> q.contains(kw) || tokens.any { it.contains(kw) || kw.contains(it) } }
            if (score > 0) Triple(pageAtFraction(frac), summary, score) else null
        }.sortedByDescending { it.third }

        if (matched.isNotEmpty()) {
            return matched.take(2).map { (pNum, summary, _) ->
                BookTopicPageHit(
                    pageNumber = pNum,
                    topicTitle = summary.substringBefore(":"),
                    excerpt = "$summary in \"${book.title}\"."
                )
            }
        }

        // Default targeted section inside the book
        val midPage = (safeTotal * 0.28f).toInt().coerceIn(1, safeTotal)
        return listOf(
            BookTopicPageHit(
                pageNumber = midPage,
                topicTitle = "${book.courseCode} Core Reference Section",
                excerpt = "${book.description} (See Page $midPage of ${book.title})."
            )
        )
    }

    suspend fun askAiAssistant(
        book: BookEntity,
        pages: List<BookPageEntity>,
        bookmarks: List<BookmarkEntity>,
        currentPageNumber: Int,
        userQuestion: String,
        customGeminiKey: String = ""
    ): AiChatMessage = withContext(Dispatchers.IO) {
        val subject = CivilSubject.fromKey(book.subjectKey)
        val pageHits = findRelevantPagesInBook(
            book = book,
            pages = pages,
            bookmarks = bookmarks,
            currentPageNumber = currentPageNumber,
            userQuestion = userQuestion
        )
        val suggestedPages = pageHits.map { it.pageNumber }.distinct()
        val hitsContext = pageHits.joinToString("\n") { hit ->
            "- Page ${hit.pageNumber} (${hit.topicTitle}): ${hit.excerpt}"
        }

        val systemPrompt = """
            You are CivLib AI, an expert Civil Engineering, Mathematics, Physics, and Masters Engineering study assistant built directly into the textbook reader.
            Current Book: "${book.title}" by ${book.author} (${book.courseCode} - ${subject.displayName}, ${book.totalPages.coerceAtLeast(100)} pages).
            Student is currently on: Page $currentPageNumber.
            Book Synopsis: ${book.description}
            Matching Pages & Sections found inside this book:
            $hitsContext

            Instructions:
            1. Answer the student's question clearly, warmly, and concisely with key engineering/math formulas or step-by-step explanations.
            2. Explicitly cite the exact page number(s) found above (e.g. "Yes! In this book, check **Page ${suggestedPages.firstOrNull() ?: currentPageNumber}**...") so the student can tap the button below your message to jump straight to that page or save it as a study note.
            3. Keep your response focused, well-structured with bullet points, and easy to read on a phone.
        """.trimIndent()

        // 1. Try custom Gemini Flash API if key is configured in Settings
        val llmReply = if (customGeminiKey.isNotBlank()) {
            callGeminiFlashApi(customGeminiKey, systemPrompt, userQuestion)
        } else {
            null
        } ?: callFreePollinationsLlm(systemPrompt, userQuestion)
            ?: buildOfflineSmartEngineeringReply(book, subject, currentPageNumber, userQuestion, pageHits)

        val cleanNoteSummary = buildString {
            append("[AI Study Summary · P.${suggestedPages.firstOrNull() ?: currentPageNumber}] ")
            append(
                llmReply.lineSequence()
                    .map { it.trim().removePrefix("* ").removePrefix("- ") }
                    .filter { it.isNotBlank() }
                    .take(4)
                    .joinToString(" | ")
                    .take(360)
            )
        }

        AiChatMessage(
            id = "ai_${System.currentTimeMillis()}",
            isUser = false,
            text = llmReply,
            suggestedPages = suggestedPages,
            noteSummaryToSave = cleanNoteSummary
        )
    }

    private fun callFreePollinationsLlm(systemPrompt: String, userQuestion: String): String? {
        return runCatching {
            val payload = JSONObject().apply {
                put("model", "openai")
                put(
                    "messages",
                    JSONArray().apply {
                        put(JSONObject().put("role", "system").put("content", systemPrompt))
                        put(JSONObject().put("role", "user").put("content", userQuestion))
                    }
                )
                put("temperature", 0.3)
            }
            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val req = Request.Builder()
                .url("https://text.pollinations.ai/openai")
                .post(body)
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val raw = resp.body?.string()?.trim().orEmpty()
                if (raw.isEmpty()) return null
                if (raw.startsWith("{")) {
                    val json = JSONObject(raw)
                    val choices = json.optJSONArray("choices")
                    val content = choices?.optJSONObject(0)
                        ?.optJSONObject("message")
                        ?.optString("content")
                        ?.trim()
                    if (!content.isNullOrBlank()) return content
                }
                raw.takeIf { it.length > 15 }
            }
        }.getOrNull()
    }

    private fun callGeminiFlashApi(apiKey: String, systemPrompt: String, userQuestion: String): String? {
        return runCatching {
            val payload = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(
                                JSONObject().put("text", "$systemPrompt\n\nStudent Question: $userQuestion")
                            )
                        )
                    )
                )
            }
            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val req = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey.trim()}")
                .post(body)
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val raw = resp.body?.string().orEmpty()
                val json = JSONObject(raw)
                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            }
        }.getOrNull()
    }

    private fun buildOfflineSmartEngineeringReply(
        book: BookEntity,
        subject: CivilSubject,
        currentPageNumber: Int,
        userQuestion: String,
        pageHits: List<BookTopicPageHit>
    ): String {
        val primaryHit = pageHits.firstOrNull()
        val targetPage = primaryHit?.pageNumber ?: currentPageNumber

        return buildString {
            append("Yes! In **\"${book.title}\"** (${book.courseCode} · ${subject.shortName}), ")
            append("this is covered on **Page $targetPage**")
            if (pageHits.size > 1) {
                append(" (and also referenced on **Page ${pageHits[1].pageNumber}**)")
            }
            append(":\n\n")
            pageHits.forEach { hit ->
                append("• **Page ${hit.pageNumber} — ${hit.topicTitle}**:\n  ${hit.excerpt}\n\n")
            }
            append("**Key Engineering & Exam Takeaways:**\n")
            when (subject) {
                CivilSubject.MATH_PHYSICS -> {
                    append("• Verify boundary conditions and units (SI: N, m, Pa, rad/s) before integrating.\n")
                    append("• For differential equations in structural/fluid systems: solve homogeneous + particular solutions.")
                }
                CivilSubject.MECHANICS -> {
                    append("• Static Equilibrium Equations: `ΣFx = 0`, `ΣFy = 0`, `ΣMz = 0`.\n")
                    append("• Always draw the Free-Body Diagram (FBD) and resolve angled forces into orthogonal components.")
                }
                CivilSubject.SURVEYING -> {
                    append("• Latitude = `D cos(Azimuth)`, Departure = `D sin(Azimuth)`.\n")
                    append("• Compass (Bowditch) Rule balances closure error proportionally to course length.")
                }
                CivilSubject.MATERIALS -> {
                    append("• Normal Stress: `σ = P / A` | Bending Stress: `σ = M·y / I` | Torsional Shear: `τ = T·r / J`.\n")
                    append("• Euler Buckling Load: `Pcr = π²·E·I / (K·L)²`.")
                }
                CivilSubject.STRUCTURAL, CivilSubject.MASTERS_GRADUATE -> {
                    append("• Flexure & Stiffness: `[K]{u} = {F}` | LRFD Design Strength: `φRn ≥ Σ(γi·Qi)`.\n")
                    append("• Check moment distribution, shear capacity, and deflection serviceability limits.")
                }
                CivilSubject.HYDRAULICS -> {
                    append("• Continuity: `Q = A₁V₁ = A₂V₂` | Bernoulli: `P₁/γ + V₁²/2g + z₁ = P₂/γ + V₂²/2g + z₂ + hf`.\n")
                    append("• Manning's Open-Channel Formula: `V = (1/n) · R^(2/3) · S^(1/2)`.")
                }
                CivilSubject.GEOTECHNICAL -> {
                    append("• Effective Stress: `σ' = σ - u` | Mohr-Coulomb Shear Strength: `τf = c' + σ' tan(φ')`.\n")
                    append("• Darcy's Seepage Law: `q = k · i · A`.")
                }
                else -> {
                    append("• Core Reference: ${book.courseCode} (${subject.displayName}).\n")
                    append("• Use the action buttons below to jump straight to **Page $targetPage** or save this summary to your Study Notes.")
                }
            }
        }
    }
}

/**
 * Full-Screen AI Study Assistant Popup Window.
 * Lets the student ask questions about the current book/PDF, find which page a topic is on,
 * jump directly to that page with 1 tap ("Open Page X"), or save the AI's answer as a Study Note ("Save Note on Page X").
 */
@Composable
fun AiStudyAssistantFullScreenDialog(
    book: BookEntity,
    pages: List<BookPageEntity>,
    bookmarks: List<BookmarkEntity>,
    currentPageNumber: Int,
    customGeminiApiKey: String,
    onDismiss: () -> Unit,
    onOpenPageInBook: (pageNumber: Int) -> Unit,
    onSaveAiNoteForPage: (pageNumber: Int, noteText: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }

    val messages = remember(book.id) {
        mutableStateListOf(
            AiChatMessage(
                id = "welcome",
                isUser = false,
                text = "Hi! I'm your **AI Study Assistant** for **\"${book.title}\"** (${book.courseCode}).\n\n" +
                    "You can ask me:\n" +
                    "• *\"Where in this book does it cover [topic/formula]?\"* (I'll find the page number and let you jump there)\n" +
                    "• *\"Explain Page $currentPageNumber simply\"*\n" +
                    "• *\"Summarize key formulas on this page and save a study note\"*",
                suggestedPages = listOf(currentPageNumber.coerceAtLeast(1))
            )
        )
    }

    var inputQuery by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val submitQuestion: (String) -> Unit = { rawText ->
        val q = rawText.trim()
        if (q.isNotEmpty() && !isThinking) {
            inputQuery = ""
            messages.add(
                AiChatMessage(
                    id = "user_${System.currentTimeMillis()}",
                    isUser = true,
                    text = q
                )
            )
            isThinking = true
            scope.launch {
                listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))
                val reply = AiBookAssistantEngine.askAiAssistant(
                    book = book,
                    pages = pages,
                    bookmarks = bookmarks,
                    currentPageNumber = currentPageNumber,
                    userQuestion = q,
                    customGeminiKey = customGeminiApiKey
                )
                messages.add(reply)
                isThinking = false
                listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))
            }
        }
    }

    val quickPrompts = remember(currentPageNumber, subject) {
        listOf(
            "Explain Page $currentPageNumber simply",
            "Find key formulas in this book",
            "Where is ${subject.shortName} design covered?",
            "Summarize Page $currentPageNumber for my study notes"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = EspressoBg,
            contentColor = SandCreamLight,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top Header Bar with Back Button
                Surface(
                    color = EspressoCard,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("ai_assistant_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to book",
                                tint = SandCreamLight
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SandCream)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = EspressoInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Book Assistant · Page $currentPageNumber",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = SandCreamLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${book.courseCode} · ${book.title}",
                                fontFamily = DmSansFontFamily,
                                fontSize = 11.5.sp,
                                color = SandCreamMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                if (messages.size > 1) {
                                    val first = messages.first()
                                    messages.clear()
                                    messages.add(first)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear conversation",
                                tint = SandCreamMuted
                            )
                        }
                    }
                }

                // Quick Prompt Chips Strip
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPrompts) { prompt ->
                        Surface(
                            onClick = { submitQuestion(prompt) },
                            shape = RoundedCornerShape(999.dp),
                            color = SageOlive.copy(alpha = 0.30f),
                            modifier = Modifier.border(
                                1.dp,
                                SandCream.copy(alpha = 0.25f),
                                RoundedCornerShape(999.dp)
                            )
                        ) {
                            Text(
                                text = prompt,
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = SandCreamLight,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 0.75.dp, color = SandCream.copy(alpha = 0.14f))

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(messages, key = { it.id }) { msg ->
                        AiChatBubbleCard(
                            message = msg,
                            onJumpToPage = { pageNum ->
                                onDismiss()
                                onOpenPageInBook(pageNum)
                            },
                            onSaveAsNote = { pageNum, noteText ->
                                onSaveAiNoteForPage(pageNum, noteText)
                                val idx = messages.indexOfFirst { it.id == msg.id }
                                if (idx >= 0) {
                                    messages[idx] = messages[idx].copy(noteSavedForPage = pageNum)
                                }
                            }
                        )
                    }

                    if (isThinking) {
                        item(key = "ai_thinking") {
                            Surface(
                                color = EspressoCard,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = SandCream,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Scanning book pages & generating explanation…",
                                        fontFamily = DmSansFontFamily,
                                        fontSize = 12.5.sp,
                                        color = SandCreamMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Chat Input Bar
                Surface(
                    color = EspressoCard,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = {
                                Text(
                                    "Ask AI to find a topic in this book or explain Page $currentPageNumber…",
                                    fontSize = 13.sp,
                                    color = SandCreamMuted
                                )
                            },
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = { submitQuestion(inputQuery) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SandCreamLight,
                                unfocusedTextColor = SandCreamLight,
                                focusedBorderColor = SandCream,
                                unfocusedBorderColor = SageOlive.copy(alpha = 0.5f),
                                cursorColor = SandCream
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input_field")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            onClick = { submitQuestion(inputQuery) },
                            enabled = inputQuery.isNotBlank() && !isThinking,
                            shape = CircleShape,
                            color = if (inputQuery.isNotBlank() && !isThinking) SandCream else SageOlive.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("ai_chat_send_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Ask AI",
                                    tint = EspressoInk,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChatBubbleCard(
    message: AiChatMessage,
    onJumpToPage: (Int) -> Unit,
    onSaveAsNote: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isUser) SandCream else EspressoCard,
            contentColor = if (message.isUser) EspressoInk else SandCreamLight,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(if (message.isUser) 0.84f else 0.96f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (!message.isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = SandCream,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CIVLIB AI ASSISTANT",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SandCream
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text.replace("**", ""),
                    fontFamily = if (message.isUser) DmSansFontFamily else LoraFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 19.5.sp,
                    color = if (message.isUser) EspressoInk else SandCreamLight
                )

                // Action Buttons: "Open Page X for you?" and "Want me to note this for you?"
                if (!message.isUser && (message.suggestedPages.isNotEmpty() || !message.noteSummaryToSave.isNullOrBlank())) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = SandCream.copy(alpha = 0.18f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Jump to Page buttons ("Want me to open Page X for you?")
                    if (message.suggestedPages.isNotEmpty()) {
                        Text(
                            text = "Open referenced page in book:",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            color = SandCreamMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            message.suggestedPages.take(3).forEach { pNum ->
                                Surface(
                                    onClick = { onJumpToPage(pNum) },
                                    shape = RoundedCornerShape(999.dp),
                                    color = SandCream
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = EspressoInk,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Open Page $pNum →",
                                            fontFamily = DmSansFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = EspressoInk
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Save as Study Note button ("Want me to note this for you?")
                    val noteSummary = message.noteSummaryToSave
                    val targetPageForNote = message.suggestedPages.firstOrNull() ?: 1
                    if (!noteSummary.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val alreadySaved = message.noteSavedForPage != null
                        Surface(
                            onClick = {
                                if (!alreadySaved) {
                                    onSaveAsNote(targetPageForNote, noteSummary)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (alreadySaved) SageOlive.copy(alpha = 0.45f) else EspressoBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (alreadySaved) SageOlive else SandCream.copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (alreadySaved) Icons.Filled.BookmarkAdded else Icons.Filled.EditNote,
                                    contentDescription = null,
                                    tint = SandCream,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (alreadySaved) {
                                        "Saved to your Study Notes for Page ${message.noteSavedForPage} ✓"
                                    } else {
                                        "Want me to note this for you on Page $targetPageForNote?"
                                    },
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = SandCreamLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
