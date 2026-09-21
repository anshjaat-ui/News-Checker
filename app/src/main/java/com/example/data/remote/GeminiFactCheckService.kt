package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.FactCheckResult
import com.example.data.model.FactCheckVerdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiFactCheckService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "SachCheckGemini"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val PRIMARY_MODEL = "gemini-2.5-flash"
        private const val FALLBACK_MODEL = "gemini-3.5-flash"

        val SYSTEM_PROMPT = """
Tum ek fact-checking assistant ho jiska naam hai "Sach Check". Tumhara eklauta kaam hai: user ke diye gaye WhatsApp forward/news claim/image ko verified, credible sources ke against check karna aur HONEST verdict dena — kabhi bhi apni taraf se guess ya assumption se verdict mat do.

## CRITICAL RULE — HALLUCINATION PREVENTION (SABSE IMPORTANT)
- Tum KABHI BHI apne training knowledge se "ye fake hai" ya "ye real hai" nahi bologe bina kisi verified external source ke
- Har verdict SIRF tab do jab tumhare paas actual search result / fact-check database match ho
- Agar koi verified source nahi milta, to verdict "UNVERIFIED" dena hai — kabhi bhi confidence fake mat karo
- Agar tumhe khud yakeen nahi hai, to seedha bol do "Mujhe iska pakka source nahi mila, isliye main confirm nahi kar sakta"
- Kisi bhi cheez ko invent mat karo — na fake source, na fake fact-checker ka naam, na fake statistic

## PROCESS (STEP BY STEP)
1. Claim Extraction: User ke forward/image/text se exact factual claim nikaalo. Agar claim vague hai, to sabse specific verifiable part identify karo.
2. Search/Match: Dekho ki:
   - Kya ye claim pehle se kisi verified fact-checker (Alt News, BOOM, PIB Fact Check, Vishvas News, Reuters Fact Check, AFP Fact Check, The Quint, India Today Fact Check) ne cover kiya hai
   - Kya koi official/primary source (government site, official statement, credible news) is claim ko confirm/deny karta hai
3. Verdict Assignment: Neeche diye gaye 4 categories mein se hi ek do, koi aur category invent mat karo:
   - "FAKE" — kisi verified fact-checker/source ne explicitly is claim ko galat saabit kiya hai
   - "MISLEADING" — claim mein kuch sach hai lekin context todha-mroda gaya hai, ya purani/unrelated cheez naye context mein daali gayi hai
   - "TRUE" — verified credible sources is claim ko confirm karte hain
   - "UNVERIFIED" — koi reliable source nahi mila is claim ke baare mein, is case mein user ko explicitly caution do

## TONE
- Simple, calm, non-judgmental Hinglish — user ko dara mat, samjhao
- Kabhi bhi user ko bewakoof mat bolo ki usne forward kyun kiya — helpful cousin/dost jaisa tone rakho jo bina judge kiye sach bata raha hai
- Agar verdict FAKE/MISLEADING hai, to ek chhoti si line explain karo ki asli sach kya hai (agar pata hai to)

## SOURCE CITATION (MANDATORY)
- Har verdict ke saath kam se kam ek real, actual source/link dena zaroori hai (fact-checker ka naam + agar possible ho to link)
- Agar UNVERIFIED verdict hai, to source ki jagah bolo "Koi verified fact-check nahi mila — savdhani se aage forward na karein"
- Kabhi bhi fake ya placeholder source name (jaise "TrustedSource.com") mat do — sirf real, existing fact-checking organizations ka naam lo

## OUTPUT FORMAT (STRICT JSON)
You must return ONLY a valid JSON object without markdown formatting:
{
  "extracted_claim": "user ke forward se nikala gaya exact claim",
  "verdict": "FAKE" | "MISLEADING" | "TRUE" | "UNVERIFIED",
  "confidence": "High" | "Medium" | "Low",
  "explanation": "2-3 lines mein Hinglish explanation ki verdict kyun diya",
  "sources": ["source 1 naam + link agar available", "source 2 agar hai"],
  "user_advice": "ek chhoti advice line — jaise 'aage forward karne se pehle source check kar lein' ya 'ye verified hai, bina fikar share kar sakte hain'"
}
""".trimIndent()
    }

    suspend fun verifyClaim(
        query: String,
        bitmap: Bitmap? = null
    ): FactCheckResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // 1. Check if we have an exact pre-verified match in our known fact-checks database
        val knownMatch = KnownFactChecks.findMatch(query)
        if (knownMatch != null && bitmap == null) {
            return@withContext knownMatch.copy(
                originalInput = query,
                timestamp = System.currentTimeMillis()
            )
        }

        // 2. If API key is missing or blank, use fallback mechanism
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            if (knownMatch != null) return@withContext knownMatch
            return@withContext FactCheckResult(
                extractedClaim = if (query.isNotBlank()) query.take(120) else "Claim photo / screenshot",
                verdict = FactCheckVerdict.UNVERIFIED,
                confidence = "Low",
                explanation = "Abhi live internet fact-check verification key configure nahi hui hai. Kripya AI Studio ke Secrets panel mein GEMINI_API_KEY add karein.",
                sources = listOf("Koi verified fact-check nahi mila — savdhani se aage forward na karein"),
                userAdvice = "Bina verified source ke is message ko kisi bhi WhatsApp group mein forward na karein.",
                originalInput = query
            )
        }

        // 3. Perform Live Gemini API Call with Google Search Grounding
        try {
            return@withContext executeGeminiCall(apiKey, PRIMARY_MODEL, query, bitmap, withSearch = true)
        } catch (e1: Exception) {
            Log.w(TAG, "Search tool call failed: ${e1.message}, trying without search tool...")
            try {
                return@withContext executeGeminiCall(apiKey, PRIMARY_MODEL, query, bitmap, withSearch = false)
            } catch (e2: Exception) {
                Log.w(TAG, "Primary model failed: ${e2.message}, trying fallback model $FALLBACK_MODEL")
                try {
                    return@withContext executeGeminiCall(apiKey, FALLBACK_MODEL, query, bitmap, withSearch = false)
                } catch (e3: Exception) {
                    Log.e(TAG, "All Gemini attempts failed: ${e3.message}", e3)
                    // Fallback to known check if available, otherwise return honest UNVERIFIED
                    knownMatch ?: FactCheckResult(
                        extractedClaim = if (query.isNotBlank()) query.take(120) else "WhatsApp Claim",
                        verdict = FactCheckVerdict.UNVERIFIED,
                        confidence = "Low",
                        explanation = "Internet connect nahi ho saka ya fact-check source verify karne mein dikkat aayi: ${e3.localizedMessage ?: "Unknown error"}. Isliye hum bina pakka source ke verdict nahi de sakte.",
                        sources = listOf("Koi verified fact-check nahi mila — savdhani se aage forward na karein"),
                        userAdvice = "Savdhani bartein aur aage kisi bhi social media ya WhatsApp par share na karein.",
                        originalInput = query
                    )
                }
            }
        }
    }

    private fun executeGeminiCall(
        apiKey: String,
        model: String,
        query: String,
        bitmap: Bitmap?,
        withSearch: Boolean
    ): FactCheckResult {
        val endpoint = "$BASE_URL/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject()

        // System Instruction
        val sysInstructionObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", SYSTEM_PROMPT))
        sysInstructionObj.put("parts", sysParts)
        requestJson.put("systemInstruction", sysInstructionObj)

        // Contents
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        val promptText = if (query.isNotBlank()) {
            "User WhatsApp forward / news claim:\n\"$query\"\n\nIs claim ko verified fact-checking databases (Alt News, BOOM, PIB Fact Check, Vishvas News, Reuters, etc.) ke against verify karo aur strict JSON format mein verdict do."
        } else {
            "User ne yeh image WhatsApp forward / screenshot bheja hai. Is image mein jo claim hai usko extract karke verified sources ke against check karo aur strict JSON output do."
        }
        partsArray.put(JSONObject().put("text", promptText))

        // Image if attached
        if (bitmap != null) {
            val base64Image = bitmapToBase64(bitmap)
            val inlineData = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", base64Image)
            }
            partsArray.put(JSONObject().put("inlineData", inlineData))
        }

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        requestJson.put("contents", contentsArray)

        // Search grounding tool
        if (withSearch) {
            val toolsArray = JSONArray()
            val googleSearchTool = JSONObject().apply {
                put("googleSearch", JSONObject())
            }
            toolsArray.put(googleSearchTool)
            requestJson.put("tools", toolsArray)
        }

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", 0.1)
        }
        requestJson.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toString().toRequestBody(mediaType)

        val httpRequest = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        val response = client.newCall(httpRequest).execute()
        val responseBody = response.body?.string() ?: throw IllegalStateException("Empty response body from Gemini")

        if (!response.isSuccessful) {
            throw IllegalStateException("API error HTTP ${response.code}: $responseBody")
        }

        val parsedText = extractTextFromGeminiResponse(responseBody)
        return parseResultJson(parsedText, query)
    }

    private fun extractTextFromGeminiResponse(responseBody: String): String {
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates")
            ?: throw IllegalStateException("No candidates in response")
        if (candidates.length() == 0) throw IllegalStateException("Empty candidates list")

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
            ?: throw IllegalStateException("No content in candidate")
        val parts = content.optJSONArray("parts")
            ?: throw IllegalStateException("No parts in candidate content")

        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            if (part.has("text")) {
                sb.append(part.getString("text"))
            }
        }
        return sb.toString()
    }

    private fun parseResultJson(rawText: String, originalInput: String): FactCheckResult {
        // Strip markdown code fences if present (e.g. ```json ... ```)
        var cleaned = rawText.trim()
        if (cleaned.startsWith("```")) {
            val lines = cleaned.lines()
            val filtered = lines.filterNot { it.trim().startsWith("```") }
            cleaned = filtered.joinToString("\n").trim()
        }

        // Find outer JSON boundaries
        val firstBrace = cleaned.indexOf('{')
        val lastBrace = cleaned.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1)
        }

        val json = JSONObject(cleaned)
        val extractedClaim = json.optString("extracted_claim", originalInput.take(120))
        val verdictStr = json.optString("verdict", "UNVERIFIED")
        val confidence = json.optString("confidence", "Medium")
        val explanation = json.optString("explanation", "Verified source check complete.")
        val userAdvice = json.optString("user_advice", "Aage forward karne se pehle source check kar lein.")

        val sourcesList = mutableListOf<String>()
        val sourcesArray = json.optJSONArray("sources")
        if (sourcesArray != null) {
            for (i in 0 until sourcesArray.length()) {
                sourcesList.add(sourcesArray.getString(i))
            }
        }
        if (sourcesList.isEmpty()) {
            sourcesList.add("Koi verified fact-check nahi mila — savdhani se aage forward na karein")
        }

        return FactCheckResult(
            extractedClaim = extractedClaim,
            verdict = FactCheckVerdict.fromString(verdictStr),
            confidence = confidence,
            explanation = explanation,
            sources = sourcesList,
            userAdvice = userAdvice,
            originalInput = originalInput
        )
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if very large to prevent memory overhead
        val scaled = if (bitmap.width > 1200 || bitmap.height > 1200) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth = if (ratio > 1f) 1200 else (1200 * ratio).toInt()
            val newHeight = if (ratio > 1f) (1200 / ratio).toInt() else 1200
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
