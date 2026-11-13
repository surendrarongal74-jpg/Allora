package com.example.data.ai

import android.util.Log
import com.example.data.model.ItinerarySlot
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

data class GeneratedTripItinerary(
    val title: String,
    val summary: String,
    val estimatedTotalUsd: Double,
    val slots: List<ItinerarySlot>
)

class AlloraAiService {

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            systemInstruction = content {
                text(
                    """
                    You are ALLORA, a refined futuristic personal travel & life assistant.
                    Always be transparent about live provider integrations vs estimates:
                    never claim a flight or hotel reservation is live-ticketed unless explicitly approved in ALLORA's Permission/Approval Engine.
                    Give crisp, practical, elevated travel guidance.
                    """.trimIndent()
                )
            }
        )
    }

    private val itinerarySchema = Schema.obj(
        properties = mapOf(
            "tripTitle" to Schema.string(description = "Concise title for the trip"),
            "summary" to Schema.string(description = "Brief 2-sentence overview of the tailored trip"),
            "estimatedTotalUsd" to Schema.double(description = "Estimated total budget in USD"),
            "days" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "timeRange" to Schema.string(description = "Time window e.g. Day 1 • 09:00 AM - 11:30 AM"),
                        "title" to Schema.string(description = "Activity or transit title"),
                        "location" to Schema.string(description = "Neighborhood or venue name"),
                        "category" to Schema.enumeration(
                            values = listOf("Flight", "CheckIn", "Dining", "Sights", "Transit"),
                            description = "Slot category"
                        ),
                        "durationMinutes" to Schema.integer(description = "Duration in minutes"),
                        "estimatedCostUsd" to Schema.double(description = "Estimated slot cost in USD")
                    )
                ),
                description = "Chronological itinerary stops (4 to 6 stops)"
            )
        )
    )

    private val itineraryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = itinerarySchema
            }
        )
    }

    fun streamAssistantResponse(
        prompt: String,
        historyPairs: List<Pair<Boolean, String>>
    ): Flow<String> = flow {
        val history: List<Content> = historyPairs.takeLast(8).map { (isUser, msg) ->
            content(if (isUser) "user" else "model") { text(msg) }
        }
        val chat = chatModel.startChat(history = history)
        val sb = StringBuilder()
        try {
            withTimeout(60_000L) {
                chat.sendMessageStream(prompt).collect { chunk ->
                    val delta = chunk.text.orEmpty()
                    if (delta.isNotEmpty()) {
                        sb.append(delta)
                        emit(sb.toString())
                    }
                }
            }
            if (sb.isBlank()) {
                throw IllegalStateException("ALLORA AI returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini streaming timed out", e)
            throw IllegalStateException("AI request timed out after 60 seconds. Please retry.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini streaming failed", e)
            throw e
        }
    }

    suspend fun generateStructuredItinerary(
        destination: String,
        startDate: String,
        endDate: String,
        travellers: Int,
        style: String,
        budgetUsd: Double,
        transport: String,
        lodgingType: String,
        activities: List<String>
    ): Result<GeneratedTripItinerary> = try {
        val prompt = """
            Design a refined $style itinerary for $travellers traveller(s) visiting $destination
            from $startDate to $endDate with a target budget of $${budgetUsd.toInt()} USD.
            Preferred transport: $transport. Preferred lodging: $lodgingType.
            Focus interests: ${activities.joinToString(", ").ifBlank { "Culture, Dining, Landmarks" }}.
            Return 5 crisp chronological itinerary blocks.
        """.trimIndent()

        val response = withTimeout(60_000L) {
            itineraryModel.generateContent(prompt)
        }
        val rawJson = response.text ?: throw IllegalStateException("Gemini returned empty JSON.")
        val json = JSONObject(rawJson)
        val title = json.optString("tripTitle", "$destination Curated Escape")
        val summary = json.optString("summary", "Tailored itinerary generated via ALLORA AI.")
        val total = json.optDouble("estimatedTotalUsd", budgetUsd)
        val daysArr = json.getJSONArray("days")
        val slots = (0 until daysArr.length()).map { idx ->
            val obj = daysArr.getJSONObject(idx)
            ItinerarySlot(
                id = "ai-slot-${UUID.randomUUID().toString().take(6)}",
                timeRange = obj.optString("timeRange", "Day ${idx + 1}"),
                title = obj.optString("title", "Curated Experience"),
                location = obj.optString("location", destination),
                type = obj.optString("category", "Sights"),
                durationMinutes = obj.optInt("durationMinutes", 90),
                routeSequence = idx + 1,
                calSyncStatus = "AI Generated • Ready for Calendar Sync"
            )
        }
        Result.success(GeneratedTripItinerary(title, summary, total, slots))
    } catch (e: TimeoutCancellationException) {
        Log.e(TAG, "Itinerary generation timed out", e)
        Result.failure(IllegalStateException("Itinerary generation timed out after 60 seconds.", e))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Itinerary generation failed", e)
        Result.failure(e)
    }

    private companion object {
        const val TAG = "AlloraAiService"
        const val DEFAULT_MODEL = "gemini-3.8-flash"
    }
}
