package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.AlloraAiOrb
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassCircleIconButton
import com.example.ui.components.GlassLoader
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.OrbState
import com.example.ui.components.StatusGlassPill
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.Violet
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.AlloraViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAssistantScreen(
    viewModel: AlloraViewModel,
    onBack: () -> Unit,
    onNavigateSub: (SubDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val messages by viewModel.chatMessages.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val statusLabel by viewModel.aiStatusLabel.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    BackHandler(onBack = onBack)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Floating Clear-Glass Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassCircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                    modifier = Modifier.testTag("ai_back_button")
                )
                Spacer(modifier = Modifier.width(10.dp))
                AlloraAiOrb(state = orbState, size = 42.dp, reducedMotion = reducedMotion)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ALLORA AI",
                        style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (orbState == OrbState.ERROR) ErrorCoral else SoftCyan
                        )
                    )
                }
            }

            GlassCircleIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "New conversation",
                onClick = { viewModel.clearAiConversation() }
            )
        }

        // Chat Stream (with holographic orb hero when starting)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (messages.size <= 1) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AlloraAiOrb(state = orbState, size = 148.dp, reducedMotion = reducedMotion)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "How can I help you today?",
                            style = MaterialTheme.typography.headlineLarge.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Live streaming synthesis • Real adapter & booking shield context",
                            style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GlassChip(
                                label = "Plan 5 days in Tokyo",
                                selected = false,
                                leadingIcon = Icons.Default.AutoAwesome,
                                onClick = { viewModel.sendAiPrompt("Plan a balanced 5-day luxury & calm itinerary for Tokyo.") }
                            )
                            GlassChip(
                                label = "Search Flights",
                                selected = false,
                                leadingIcon = Icons.Default.FlightTakeoff,
                                onClick = { onNavigateSub(SubDestination.FLIGHTS) }
                            )
                            GlassChip(
                                label = "Explore Hotels",
                                selected = false,
                                leadingIcon = Icons.Default.Hotel,
                                onClick = { onNavigateSub(SubDestination.HOTELS) }
                            )
                            GlassChip(
                                label = "Review Shield Holds",
                                selected = false,
                                leadingIcon = Icons.Default.Shield,
                                onClick = { onNavigateSub(SubDestination.APPROVALS) }
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    onChipClick = { chip ->
                        when {
                            chip.contains("trip wizard", ignoreCase = true) -> onNavigateSub(SubDestination.TRIP_WIZARD)
                            chip.contains("flights", ignoreCase = true) -> onNavigateSub(SubDestination.FLIGHTS)
                            chip.contains("hotels", ignoreCase = true) -> onNavigateSub(SubDestination.HOTELS)
                            chip.contains("budget", ignoreCase = true) -> onNavigateSub(SubDestination.BUDGET)
                            else -> viewModel.sendAiPrompt(chip)
                        }
                    },
                    onRetry = {
                        val lastUser = messages.lastOrNull { it.isUser }?.text
                        if (!lastUser.isNullOrBlank()) viewModel.sendAiPrompt(lastUser)
                    }
                )
            }
            if (orbState == OrbState.PROCESSING) {
                item {
                    GlassLoader(statusMessage = "Contacting Gemini 3.8 Flash...")
                }
            }
        }

        // Translucent Bottom Prompt Composer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            GlassSearchBar(
                query = input,
                onQueryChange = { input = it },
                placeholder = "Ask anything...",
                material = GlassMaterial.DEEP,
                onVoiceClick = {
                    viewModel.sendAiPrompt("Voice summary: What is awaiting my approval and what is my upcoming Tokyo weather?")
                },
                onSubmit = {
                    if (input.isNotBlank()) {
                        val text = input
                        input = ""
                        viewModel.sendAiPrompt(text)
                    }
                },
                trailingAction = {
                    IconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                val text = input
                                input = ""
                                viewModel.sendAiPrompt(text)
                            }
                        },
                        modifier = Modifier.testTag("ai_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = SoftCyan)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatBubbleItem(
    message: AiChatMessage,
    onChipClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    val glass = LocalAlloraGlassColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (message.isUser) {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(RoundedCornerShape(22.dp, 22.dp, 6.dp, 22.dp))
                    .background(Brush.horizontalGradient(listOf(ElectricBlue.copy(alpha = 0.72f), Violet.copy(alpha = 0.72f))))
                    .border(1.dp, PearlWhite.copy(alpha = 0.42f), RoundedCornerShape(22.dp, 22.dp, 6.dp, 22.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = PearlWhite)
                )
            }
        } else {
            GlassCard(
                modifier = Modifier.widthIn(max = 340.dp),
                cornerRadius = 24.dp,
                material = GlassMaterial.DEEP,
                accentBorder = if (message.isError) ErrorCoral.copy(alpha = 0.7f) else SoftCyan.copy(alpha = 0.38f),
                contentPadding = PaddingValues(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (message.isError) ErrorCoral else SoftCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (message.isStreaming) "ALLORA • Streaming" else "ALLORA • Gemini 3.8 Flash",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (message.isError) ErrorCoral else SoftCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message.text.ifBlank { "Generating response..." },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (message.isError) ErrorCoral else glass.textPrimary
                    )
                )

                if (message.isError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusGlassPill(
                        label = "Retry Gemini Request",
                        color = SoftCyan,
                        modifier = Modifier.clickable(onClick = onRetry)
                    )
                } else if (message.followUpChips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        message.followUpChips.forEach { chip ->
                            GlassChip(
                                label = chip,
                                selected = false,
                                onClick = { onChipClick(chip) }
                            )
                        }
                    }
                }
            }
        }
    }
}
