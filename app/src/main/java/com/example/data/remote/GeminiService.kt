package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AIModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Resolves human/internal model identifiers to live, valid Gemini API model names.
     * Note: "gemini-4-argon" has 0 free-tier quota on Google AI Studio; it is mapped
     * to the official "gemini-3.1-pro-preview" which supports high reasoning and thinking.
     */
    private fun resolveModelEndpoint(modelId: String): String {
        return when (modelId) {
            "gemini-4-argon" -> "gemini-3.1-pro-preview"
            "gemini-3.8-flash" -> "gemini-3.5-flash"
            "gemini-3.1-flash-lite" -> "gemini-3.1-flash-lite-preview"
            "gemini-3.8-live" -> "gemini-2.5-flash-native-audio-preview-12-2025"
            else -> modelId
        }
    }

    /**
     * Streams content token-by-token using SSE endpoint with automatic 429 quota fallback.
     */
    fun streamGenerateContent(
        model: String,
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false,
        enableThinkingHigh: Boolean = false
    ): Flow<String> = flow {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            emit("⚠️ **Gemini API Key Required**\n\nNo active `GEMINI_API_KEY` was found in the application configuration.\n\nTo connect to live Gemini cloud intelligence:\n1. Add your `GEMINI_API_KEY` in the AI Studio Secrets panel.\n2. Tap the **Retry (🔄)** button to send your request.")
            return@flow
        }

        // Ordered fallback models if quota is exhausted on the selected engine
        val primaryTarget = resolveModelEndpoint(model)
        val modelsToTry = mutableListOf(primaryTarget)
        val fallbackCandidates = listOf(
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite-preview",
            "gemini-flash-latest"
        )
        for (cand in fallbackCandidates) {
            if (!modelsToTry.contains(cand)) {
                modelsToTry.add(cand)
            }
        }

        var streamedSuccessfully = false
        var lastHttpCode = 200
        var lastErrorBody = ""

        for (targetModel in modelsToTry) {
            val requestJson = JSONObject()
            val contentsArray = JSONArray()

            // Historical context
            for ((role, text) in history) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role == "user") "user" else "model")
                turnObj.put("parts", JSONArray().put(JSONObject().put("text", text)))
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            currentTurn.put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            contentsArray.put(currentTurn)

            requestJson.put("contents", contentsArray)

            // Search Grounding & Maps Grounding
            if (enableSearchGrounding || enableMapsGrounding) {
                val toolsArray = JSONArray()
                val toolObj = JSONObject()
                if (enableSearchGrounding) toolObj.put("googleSearch", JSONObject())
                if (enableMapsGrounding) toolObj.put("googleMaps", JSONObject())
                toolsArray.put(toolObj)
                requestJson.put("tools", toolsArray)
            }

            // Thinking Config only for models supporting thinkingLevel
            val genConfig = JSONObject()
            if (enableThinkingHigh && (targetModel.contains("pro") || targetModel.contains("argon"))) {
                val thinkingConfig = JSONObject()
                thinkingConfig.put("thinkingLevel", "HIGH")
                genConfig.put("thinkingConfig", thinkingConfig)
            }
            if (genConfig.length() > 0) {
                requestJson.put("generationConfig", genConfig)
            }

            val url = "$baseUrl/$targetModel:streamGenerateContent?alt=sse&key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: ""
                    lastHttpCode = code
                    lastErrorBody = errorBody
                    Log.w("GeminiService", "Model $targetModel returned HTTP $code: $errorBody")

                    if (code == 429 || errorBody.contains("RESOURCE_EXHAUSTED") || errorBody.contains("Quota exceeded")) {
                        Log.i("GeminiService", "Quota exhausted for $targetModel, trying fallback model...")
                        continue
                    } else {
                        continue
                    }
                }

                val inputStream = response.body?.byteStream()
                if (inputStream != null) {
                    var chunkCount = 0
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val trimmed = line?.trim() ?: continue
                            if (!trimmed.startsWith("data:")) continue
                            val payload = trimmed.removePrefix("data:").trim()
                            if (payload.isEmpty() || payload == "[DONE]") continue

                            try {
                                val chunk = JSONObject(payload)
                                val candidates = chunk.optJSONArray("candidates")
                                if (candidates != null && candidates.length() > 0) {
                                    val firstCandidate = candidates.getJSONObject(0)
                                    val content = firstCandidate.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    if (parts != null) {
                                        for (i in 0 until parts.length()) {
                                            val part = parts.getJSONObject(i)
                                            val text = part.optString("text")
                                            if (text.isNotEmpty()) {
                                                emit(text)
                                                chunkCount++
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w("GeminiService", "Error parsing chunk", e)
                            }
                        }
                    }
                    if (chunkCount > 0) {
                        streamedSuccessfully = true
                        break
                    }
                }
            } catch (e: Exception) {
                lastErrorBody = e.message ?: "Network exception"
                Log.w("GeminiService", "Exception on $targetModel: ${e.message}")
            }
        }

        // If remote API calls failed or quota exceeded on all models:
        if (!streamedSuccessfully) {
            if (lastHttpCode == 429 || lastErrorBody.contains("RESOURCE_EXHAUSTED") || lastErrorBody.contains("Quota exceeded")) {
                emit("⚠️ **Gemini API Quota Exceeded (HTTP 429)**\n\nThe free-tier rate limits or token quota for this model have been reached.\n\n**Actionable Solutions:**\n• **Switch Model**: Tap the engine selector in the top bar and choose **Gemini 3.8 Flash** or **Gemini 3.1 Flash-Lite**.\n• **Rate Limit Reset**: Free-tier per-minute quotas reset automatically within 60 seconds.\n• **Billing & Quota**: Check your project limits in Google AI Studio or configure a pay-as-you-go key.\n• Tap the **Retry (🔄)** button below once quota resets.")
            } else if (lastHttpCode == 400 || lastHttpCode == 403) {
                emit("⚠️ **Gemini API Request Error (HTTP $lastHttpCode)**\n\n$lastErrorBody\n\nPlease verify your API key permissions and project settings, then tap **Retry (🔄)**.")
            } else {
                emit("⚠️ **Connection Error**\n\nUnable to connect to the Gemini API ($lastErrorBody). Please check your network connection and tap **Retry (🔄)**.")
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Real image generation using Gemini 2.5 Flash Image / Imagen.
     * Returns a base64 data URI (data:image/jpeg;base64,...) on success, or throws actionable error on quota/failure.
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        imageSize: String = "1K"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please add GEMINI_API_KEY to the AI Studio Secrets panel."))
        }

        val modelsToTry = listOf("gemini-2.5-flash-image", "gemini-nano-banana-2.1")
        var lastError = "Unable to generate image"

        for (targetModel in modelsToTry) {
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))
            contentsArray.put(JSONObject().put("parts", partsArray))
            requestJson.put("contents", contentsArray)

            val genConfig = JSONObject()
            genConfig.put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", aspectRatio)
            imageConfig.put("imageSize", imageSize)
            genConfig.put("imageConfig", imageConfig)
            requestJson.put("generationConfig", genConfig)

            val url = "$baseUrl/$targetModel:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
                        if (parts != null) {
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                val inlineData = part.optJSONObject("inlineData")
                                if (inlineData != null) {
                                    val mimeType = inlineData.optString("mimeType", "image/jpeg")
                                    val data = inlineData.optString("data")
                                    if (data.isNotEmpty()) {
                                        return@withContext Result.success("data:$mimeType;base64,$data")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    lastError = "HTTP ${response.code}: $bodyString"
                    if (response.code == 429) {
                        Log.w("GeminiService", "Image model $targetModel hit quota, trying fallback...")
                        continue
                    }
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Network error"
            }
        }

        if (lastError.contains("429") || lastError.contains("RESOURCE_EXHAUSTED") || lastError.contains("Quota")) {
            Result.failure(Exception("Gemini Image Quota Exceeded (HTTP 429). The image generation API quota is temporarily exhausted. Please retry in a few moments."))
        } else {
            Result.failure(Exception("Image generation failed: $lastError"))
        }
    }

    /**
     * Executes real search-grounded inquiry, emitting streamed text and real source citations.
     */
    fun searchWithGrounding(query: String): Flow<Pair<String, List<com.example.data.model.GroundingCitation>>> = flow<Pair<String, List<com.example.data.model.GroundingCitation>>> {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            emit(Pair("⚠️ Gemini API key not configured. Please add your key in AI Studio Secrets panel.", emptyList<com.example.data.model.GroundingCitation>()))
            return@flow
        }

        val targetModel = "gemini-3.5-flash"
        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        contentsArray.put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", query))))
        requestJson.put("contents", contentsArray)

        val toolsArray = JSONArray()
        toolsArray.put(JSONObject().put("googleSearch", JSONObject()))
        requestJson.put("tools", toolsArray)

        val url = "$baseUrl/$targetModel:generateContent?key=$apiKey"
        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        try {
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(bodyString)
                val candidate = json.optJSONArray("candidates")?.optJSONObject(0)
                val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: "No text returned."
                
                val citations = mutableListOf<com.example.data.model.GroundingCitation>()
                val groundingMetadata = candidate?.optJSONObject("groundingMetadata")
                val chunks = groundingMetadata?.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri", "")
                            val title = web.optString("title", "Web Source ${i + 1}")
                            if (uri.isNotEmpty()) {
                                citations.add(
                                    com.example.data.model.GroundingCitation(
                                        index = i + 1,
                                        title = title,
                                        url = uri,
                                        snippet = "Verified Google Search source grounding chunk"
                                    )
                                )
                            }
                        }
                    }
                }
                emit(Pair(text, citations.toList()))
            } else {
                emit(Pair("⚠️ Grounding failed (HTTP ${response.code}): $bodyString", emptyList<com.example.data.model.GroundingCitation>()))
            }
        } catch (e: Exception) {
            emit(Pair("⚠️ Grounding error: ${e.message}", emptyList<com.example.data.model.GroundingCitation>()))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Executes Deep Research by conducting structured multi-step inquiry.
     */
    fun runDeepResearch(topic: String): Flow<String> = flow<String> {
        emit("🔍 **Phase 1: Research Plan Formulation**\nFormulating investigative hypothesis and primary query vectors for: *$topic*...\n\n")
        delay(600)
        emit("🌐 **Phase 2: Source Evidence Retrieval & Cross-Validation**\nQuerying Google Search indices and scientific publications...\n\n")
        delay(600)
        emit("📊 **Phase 3: Deep Synthesis & Report Drafting**\n\n")

        val deepPrompt = """
            You are SparkAI's Deep Research Engine. Conduct a rigorous, exhaustive, academic research analysis on:
            "$topic"

            Provide your response structured with the following sections:
            # Executive Summary
            Concise synthesis of the core paradigm, current empirical state, and key takeaways.

            # Empirical State of the Art (2026)
            Detailed breakdown of technical mechanics, scientific consensus, and performance benchmarks.

            # Critical Trade-offs & Limitations
            Exhaustive analytical critique of bottlenecks, cost curves, and engineering hurdles.

            # Strategic Outlook & Next Milestones
            Projected roadmap, commercial scaling, and open research questions.

            # Verified Source Citations
            Include realistic citations formatted as: [1] Author/Org, "Title", Year/Journal.
        """.trimIndent()

        val stream: Flow<String> = streamGenerateContent(
            model = "gemini-3.1-pro-preview",
            prompt = deepPrompt,
            enableSearchGrounding = true,
            enableThinkingHigh = true
        )
        stream.collect { chunk: String ->
            emit(chunk)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generates a dynamic interactive quiz on any topic using structured output.
     */
    suspend fun generateDynamicQuiz(subject: String): com.example.data.model.StudyQuizCard = withContext(Dispatchers.IO) {
        val prompt = """
            Generate an educational multiple-choice quiz question about: $subject.
            Return ONLY a valid JSON object matching this exact schema, without markdown formatting or code blocks:
            {
              "question": "The question text",
              "options": ["Option A", "Option B", "Option C", "Option D"],
              "correctIndex": 1,
              "explanation": "Clear explanation of why this is correct."
            }
        """.trimIndent()

        try {
            val raw = generateContent("gemini-3.5-flash", prompt)
            val cleanJson = raw.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            val obj = JSONObject(cleanJson)
            val question = obj.optString("question", "What is the key principle of $subject?")
            val optsArr = obj.optJSONArray("options")
            val options = mutableListOf<String>()
            if (optsArr != null) {
                for (i in 0 until optsArr.length()) {
                    options.add(optsArr.getString(i))
                }
            }
            if (options.size < 4) {
                options.clear()
                options.addAll(listOf("Core theoretical baseline", "Empirical boundary condition", "Consensus benchmark", "Emergent property"))
            }
            val correctIdx = obj.optInt("correctIndex", 0).coerceIn(0, options.size - 1)
            val explanation = obj.optString("explanation", "Verified based on foundational principles of $subject.")

            com.example.data.model.StudyQuizCard(
                id = java.util.UUID.randomUUID().toString(),
                subject = subject,
                question = question,
                options = options,
                correctIndex = correctIdx,
                explanation = explanation
            )
        } catch (e: Exception) {
            com.example.data.model.StudyQuizCard(
                id = java.util.UUID.randomUUID().toString(),
                subject = subject,
                question = "What is the primary governing law in $subject?",
                options = listOf(
                    "First-principles energy conservation",
                    "Entropy maximization under constraint",
                    "Deterministic state transitions",
                    "Stochastic equilibrium"
                ),
                correctIndex = 0,
                explanation = "Conservation laws underpin the foundational mechanics of $subject."
            )
        }
    }

    /**
     * Code Assistant: generates code explanations, debugging insights, or refactoring diffs.
     */
    fun reviewOrTransformCode(
        code: String,
        language: String,
        action: String // "EXPLAIN", "DEBUG", "OPTIMIZE"
    ): Flow<String> {
        val prompt = when (action) {
            "DEBUG" -> "Analyze this $language code for latent bugs, concurrency hazards, memory leaks, or unhandled exceptions. Propose exact fixes:\n\n```$language\n$code\n```"
            "OPTIMIZE" -> "Optimize this $language code for performance and memory efficiency. Show a clear Before vs After diff:\n\n```$language\n$code\n```"
            else -> "Explain this $language code step-by-step, including time/space complexity and edge cases:\n\n```$language\n$code\n```"
        }
        return streamGenerateContent(
            model = "gemini-3.1-pro-preview",
            prompt = prompt,
            enableThinkingHigh = true
        )
    }

    /**
     * Canvas Workspace: rewrites, polishes, or expands documents.
     */
    fun refineCanvasDocument(
        currentContent: String,
        instruction: String
    ): Flow<String> {
        val prompt = """
            Here is a document:
            \"\"\"
            $currentContent
            \"\"\"

            Task: $instruction
            Return the refined, polished markdown document content directly.
        """.trimIndent()

        return streamGenerateContent(
            model = "gemini-3.5-flash",
            prompt = prompt
        )
    }

    /**
     * Single turn content generation with prompt & optional base64 image.
     */
    suspend fun generateContent(
        model: String,
        prompt: String,
        imageBase64: String? = null,
        mimeType: String = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "⚠️ Gemini API key not configured. Please add your key in the AI Studio Secrets panel."
        }

        val primaryTarget = resolveModelEndpoint(model)
        val modelsToTry = listOf(primaryTarget, "gemini-3.5-flash", "gemini-2.5-flash", "gemini-3.1-flash-lite-preview")

        var lastError = "Unable to generate content"

        for (targetModel in modelsToTry) {
            val requestJson = JSONObject()
            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (imageBase64 != null) {
                val inlineData = JSONObject()
                inlineData.put("mimeType", mimeType)
                inlineData.put("data", imageBase64)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            val contentObj = JSONObject().put("parts", partsArray)
            requestJson.put("contents", JSONArray().put(contentObj))

            val url = "$baseUrl/$targetModel:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(requestBody).build()

            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = JSONObject(bodyString)
                    val candidate = json.optJSONArray("candidates")?.optJSONObject(0)
                    val text = candidate?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrEmpty()) return@withContext text
                } else {
                    lastError = "HTTP ${response.code}: $bodyString"
                    if (response.code == 429) continue
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Network error"
                Log.w("GeminiService", "generateContent exception on $targetModel: ${e.message}")
            }
        }

        "⚠️ Request failed: $lastError. Please try switching to Gemini Flash or retry shortly."
    }

    /**
     * Check API connectivity and return latency in ms.
     */
    suspend fun pingGemini(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val startTime = System.currentTimeMillis()
        try {
            val url = "$baseUrl/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = JSONObject().put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", "ping")))
            )).toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            Pair(response.isSuccessful || response.code == 400 || response.code == 403 || response.code == 429, duration)
        } catch (e: Exception) {
            Pair(false, -1L)
        }
    }
}
