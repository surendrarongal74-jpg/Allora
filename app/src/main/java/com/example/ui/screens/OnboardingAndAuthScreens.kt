package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.AlloraGoogleSignInButton
import com.example.ui.components.AlloraAiOrb
import com.example.ui.components.AlloraAtmosphericBackground
import com.example.ui.components.AtmosphereMood
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassCircleIconButton
import com.example.ui.components.GlassInput
import com.example.ui.components.GlassTabBar
import com.example.ui.components.OrbState
import com.example.ui.components.StatusGlassPill
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.Lavender
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SuccessMint
import com.example.ui.theme.WarningAmber

data class OnboardingPreferencesDraft(
    val travelStyle: String = "Luxury & Culture",
    val budgetTier: String = "Balanced Premium",
    val homeAirport: String = "SFO"
)

@Composable
fun UnauthenticatedFlowScreen(
    onAuthenticated: (OnboardingPreferencesDraft) -> Unit
) {
    var showOnboarding by remember { mutableStateOf(true) }
    var draft by remember { mutableStateOf(OnboardingPreferencesDraft()) }

    AlloraAtmosphericBackground(mood = AtmosphereMood.AUTH) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            if (showOnboarding) {
                OnboardingFlowContent(
                    draft = draft,
                    onUpdateDraft = { draft = it },
                    onFinish = { showOnboarding = false }
                )
            } else {
                AuthGlassScreen(
                    draft = draft,
                    onBackToOnboarding = { showOnboarding = true },
                    onAuthSuccess = { onAuthenticated(draft) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingFlowContent(
    draft: OnboardingPreferencesDraft,
    onUpdateDraft: (OnboardingPreferencesDraft) -> Unit,
    onFinish: () -> Unit
) {
    val glass = LocalAlloraGlassColors.current
    var step by remember { mutableIntStateOf(0) } // 0..4 (5 screens)
    val totalSteps = 5

    BackHandler(enabled = step > 0) {
        step -= 1
    }

    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top row: Clear Back + Clear Skip capsule
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                GlassCircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous step",
                    material = GlassMaterial.CLEAR,
                    onClick = { step -= 1 }
                )
            } else {
                StatusGlassPill(label = "ALLORA OS", color = SoftCyan)
            }

            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge.copy(color = PearlWhite)
                )
            }
        }

        // Main Animated Step Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(targetState = step, label = "onboardingStep") { currentStep ->
                when (currentStep) {
                    0 -> {
                        // Screen 1: Welcome Screen (Reference 1 / 4 aerial wing sky + luminous orb)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AlloraAiOrb(state = OrbState.IDLE, size = 164.dp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "ALLORA",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = PearlWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your life, effortlessly connected.",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = SoftCyan
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            GlassCard(material = GlassMaterial.CLEAR, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "An intelligent liquid-glass ecosystem unifying AI trip orchestration, weather-aware discovery, and zero-surprise booking approvals.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = PearlWhite.copy(alpha = 0.92f)),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    1 -> {
                        // Screen 2: Feature Introduction 1 (Travel & AI)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.25f))
                                    .border(1.dp, SoftCyan.copy(alpha = 0.65f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(34.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Contextual AI Planning",
                                style = MaterialTheme.typography.headlineLarge.copy(color = PearlWhite),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Converse naturally with Gemini 3.8 Flash to build live day-by-day itineraries, compare flights, and explore curated destinations.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = PearlWhite.copy(alpha = 0.88f)),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GlassCard(material = GlassMaterial.FROSTED, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = SoftCyan)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Multi-Provider Travel Search", style = MaterialTheme.typography.titleSmall.copy(color = glass.textPrimary))
                                        Text("Clear distinction between Sample Estimates and GDS Adapters", style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary))
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Screen 3: Feature Introduction 2 (Shield Approval)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(SoftMint.copy(alpha = 0.18f))
                                    .border(1.dp, SuccessMint.copy(alpha = 0.65f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessMint, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Glass Shield Approvals",
                                style = MaterialTheme.typography.headlineLarge.copy(color = PearlWhite),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "ALLORA stages temporary holds and verifies policies, taxes, and spending limits before anything is finalized.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = PearlWhite.copy(alpha = 0.88f)),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GlassCard(material = GlassMaterial.FROSTED, modifier = Modifier.fillMaxWidth(), accentBorder = SuccessMint.copy(alpha = 0.55f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuccessMint)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Two-Phase Cryptographic Ledger", style = MaterialTheme.typography.titleSmall.copy(color = glass.textPrimary))
                                        Text("Offline Room vault + Firebase Firestore cloud sync", style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary))
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Screen 4: Personalization Setup
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Tailor Your Companion",
                                style = MaterialTheme.typography.headlineLarge.copy(color = PearlWhite)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Preferences sync securely to your local Room vault and cloud profile.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = PearlWhite.copy(alpha = 0.88f))
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            GlassCard(material = GlassMaterial.FROSTED, modifier = Modifier.fillMaxWidth()) {
                                Text("Preferred Travel Style", style = MaterialTheme.typography.labelLarge.copy(color = SoftCyan))
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Luxury & Culture", "Calm Wellness", "Urban Explorer", "Adventure").forEach { style ->
                                        GlassChip(
                                            label = style,
                                            selected = draft.travelStyle == style,
                                            onClick = { onUpdateDraft(draft.copy(travelStyle = style)) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Budget Posture", style = MaterialTheme.typography.labelLarge.copy(color = Lavender))
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Smart Value", "Balanced Premium", "Ultra Luxury").forEach { tier ->
                                        GlassChip(
                                            label = tier,
                                            selected = draft.budgetTier == tier,
                                            onClick = { onUpdateDraft(draft.copy(budgetTier = tier)) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                GlassInput(
                                    value = draft.homeAirport,
                                    onValueChange = { onUpdateDraft(draft.copy(homeAirport = it.uppercase().take(4))) },
                                    label = "Home Airport (IATA)",
                                    placeholder = "e.g. SFO, JFK, LHR"
                                )
                            }
                        }
                    }

                    else -> {
                        // Screen 5: Permissions Rationale
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Transparent Permissions",
                                style = MaterialTheme.typography.headlineLarge.copy(color = PearlWhite)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ALLORA follows strict least-privilege standards—only requesting access when you invoke a feature.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = PearlWhite.copy(alpha = 0.88f))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            GlassCard(material = GlassMaterial.FROSTED, modifier = Modifier.fillMaxWidth()) {
                                PermissionRow(Icons.Default.LocationOn, "Nearby Map & Weather", "Coordinates are used strictly on-demand when filtering nearby attractions.")
                                Spacer(modifier = Modifier.height(12.dp))
                                PermissionRow(Icons.Default.CalendarMonth, "Itinerary & Calendar Sync", "Sync confirmed trip blocks only when you explicitly approve.")
                                Spacer(modifier = Modifier.height(12.dp))
                                PermissionRow(Icons.Default.Lock, "Google Account Vault", "Secures your trips, bookings, and saved items per user.")
                            }
                        }
                    }
                }
            }
        }

        // Bottom indicators + Pearl CTA
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSteps) { idx ->
                    val active = idx == step
                    Box(
                        modifier = Modifier
                            .width(if (active) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (active) PearlWhite else glass.glassBorder)
                            .clickable { step = idx }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            GlassButton(
                text = if (step < totalSteps - 1) "Continue" else "Enter ALLORA",
                onClick = {
                    if (step < totalSteps - 1) step += 1 else onFinish()
                },
                isPrimary = true,
                material = GlassMaterial.PEARL,
                testTag = "onboarding_next_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    val glass = LocalAlloraGlassColors.current
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(color = glass.textPrimary))
            Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary))
        }
    }
}

@Composable
fun AuthGlassScreen(
    draft: OnboardingPreferencesDraft,
    onBackToOnboarding: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val glass = LocalAlloraGlassColors.current
    var authTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Create Account, 2: Recovery
    var authError by remember { mutableStateOf<String?>(null) }
    var recoveryEmail by remember { mutableStateOf("") }
    var recoverySentMessage by remember { mutableStateOf<String?>(null) }

    BackHandler {
        onBackToOnboarding()
    }

    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AlloraAiOrb(state = if (authError != null) OrbState.ERROR else OrbState.IDLE, size = 104.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sign in to ALLORA",
            style = MaterialTheme.typography.headlineLarge.copy(color = PearlWhite, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Home Hub: ${draft.homeAirport} • ${draft.travelStyle}",
            style = MaterialTheme.typography.labelMedium.copy(color = SoftCyan)
        )
        Spacer(modifier = Modifier.height(18.dp))

        GlassTabBar(
            tabs = listOf("Sign In", "New Account", "Recovery"),
            selectedIndex = authTab,
            onSelectTab = {
                authTab = it
                authError = null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            material = GlassMaterial.FROSTED,
            contentPadding = PaddingValues(20.dp)
        ) {
            when (authTab) {
                0, 1 -> {
                    val isSignUp = authTab == 1
                    Text(
                        text = if (isSignUp) "Create Your Private Vault" else "Welcome Back",
                        style = MaterialTheme.typography.titleLarge.copy(color = glass.textPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSignUp) {
                            "ALLORA provisions an isolated Firebase user vault and syncs your travel preferences seamlessly."
                        } else {
                            "Authenticate with Jetpack Credential Manager and your verified Google identity."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = glass.textSecondary)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    AlloraGoogleSignInButton(
                        label = if (isSignUp) "Create Account with Google" else "Sign in with Google",
                        onAuthSuccess = {
                            authError = null
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            authError = msg
                        }
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(ErrorCoral.copy(alpha = 0.16f))
                                .border(1.dp, ErrorCoral.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = ErrorCoral, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = authError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(color = PearlWhite)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessMint, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Passkey & zero-password hardware-backed verification",
                            style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary)
                        )
                    }
                }

                else -> {
                    Text(
                        text = "Account Recovery & Email Verification",
                        style = MaterialTheme.typography.titleLarge.copy(color = glass.textPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ALLORA signs you in using your Google Account via Android Credential Manager. Password resets and 2-Step Verification are protected directly by Google Account Security.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = glass.textSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassInput(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it },
                        label = "Registered Google Email",
                        placeholder = "name@example.com",
                        leadingIcon = Icons.Default.MarkEmailRead
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = "Check Recovery Guidance",
                        onClick = {
                            val trimmed = recoveryEmail.trim()
                            recoverySentMessage = if (!trimmed.contains("@") || !trimmed.contains(".")) {
                                "Please enter a valid email address (e.g. alex@domain.com)."
                            } else {
                                "For $trimmed: visit accounts.google.com/signin/recovery to verify ownership, then tap Sign in with Google on the Sign In tab."
                            }
                        },
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (recoverySentMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = recoverySentMessage.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(color = SoftCyan)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackToOnboarding) {
            Text(
                text = "Review onboarding preferences",
                style = MaterialTheme.typography.labelMedium.copy(color = glass.textSecondary)
            )
        }
    }
}
