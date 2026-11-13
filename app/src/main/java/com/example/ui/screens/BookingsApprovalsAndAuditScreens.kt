package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalStatus
import com.example.ui.components.GlassBookingCard
import com.example.ui.components.GlassBottomSheet
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.GlassStateView
import com.example.ui.components.GlassTabBar
import com.example.ui.components.StatusGlassPill
import com.example.ui.theme.Aqua
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
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.Violet
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AlloraViewModel

data class UnifiedBookingUi(
    val referenceCode: String,
    val title: String,
    val providerSource: String,
    val providerPnr: String?,
    val amountUsd: Double,
    val status: String,
    val timestamp: String,
    val sha256Hash: String,
    val remoteDocId: String? = null
)

@Composable
fun BookingsHubScreen(
    viewModel: AlloraViewModel,
    onNavigateSub: (SubDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val ledger by viewModel.ledgerTransactions.collectAsState()
    val remoteBookings by viewModel.remoteBookings.collectAsState()
    val cachedBookings by viewModel.cachedBookings.collectAsState()
    val approvals by viewModel.approvalRequests.collectAsState()
    val cancelledRefs by viewModel.cancelledRefs.collectAsState()

    var tabIndex by remember { mutableIntStateOf(0) } // All, Upcoming, Completed, Cancelled
    var selectedBooking by remember { mutableStateOf<UnifiedBookingUi?>(null) }

    val pendingApprovals = approvals.count { it.status == ApprovalStatus.PENDING }

    val combined = remember(ledger, remoteBookings, cachedBookings, cancelledRefs) {
        val map = LinkedHashMap<String, UnifiedBookingUi>()
        remoteBookings.forEach { doc ->
            val st = if (doc.referenceCode in cancelledRefs) "CANCELLED" else doc.status
            map[doc.referenceCode] = UnifiedBookingUi(
                referenceCode = doc.referenceCode,
                title = doc.title,
                providerSource = doc.providerSource,
                providerPnr = doc.providerPnr,
                amountUsd = doc.totalAmountUsd,
                status = st,
                timestamp = "Cloud Vault Synced",
                sha256Hash = doc.sha256AuditHash,
                remoteDocId = doc.id
            )
        }
        cachedBookings.forEach { rec ->
            if (!map.containsKey(rec.referenceCode)) {
                val st = if (rec.referenceCode in cancelledRefs) "CANCELLED" else rec.status
                map[rec.referenceCode] = UnifiedBookingUi(
                    referenceCode = rec.referenceCode,
                    title = rec.title,
                    providerSource = rec.providerSource,
                    providerPnr = rec.providerPnr,
                    amountUsd = rec.totalAmountUsd,
                    status = st,
                    timestamp = "Room Offline Cache",
                    sha256Hash = rec.sha256AuditHash
                )
            }
        }
        ledger.forEach { tx ->
            if (!map.containsKey(tx.referenceCode)) {
                val st = if (tx.referenceCode in cancelledRefs) "CANCELLED" else tx.status
                map[tx.referenceCode] = UnifiedBookingUi(
                    referenceCode = tx.referenceCode,
                    title = tx.title,
                    providerSource = "ALLORA Shield Verified",
                    providerPnr = tx.providerPnr,
                    amountUsd = tx.amountUsd,
                    status = st,
                    timestamp = tx.timestamp,
                    sha256Hash = tx.sha256Hash
                )
            }
        }
        map.values.toList()
    }

    val filtered = remember(combined, tabIndex) {
        when (tabIndex) {
            1 -> combined.filter { it.status.contains("CONFIRMED", true) || it.status.contains("HELD", true) }
            2 -> combined.filter { it.status.contains("COMPLETED", true) }
            3 -> combined.filter { it.status.contains("CANCELLED", true) }
            else -> combined
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bookings & Vault", style = MaterialTheme.typography.headlineLarge.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold))
                    Text("Boarding passes, Room cache & two-phase Shield", style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary))
                }
                GlassChip(
                    label = "Audit Hub",
                    selected = false,
                    leadingIcon = Icons.Default.VerifiedUser,
                    onClick = { onNavigateSub(SubDestination.AUDIT_HUB) }
                )
            }
        }

        // Approval Shield Entry Card (Luminous Glass)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                material = GlassMaterial.LUMINOUS,
                accentBorder = if (pendingApprovals > 0) WarningAmber.copy(alpha = 0.65f) else SuccessMint.copy(alpha = 0.5f),
                onClick = { onNavigateSub(SubDestination.APPROVALS) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SoftCyan.copy(alpha = 0.18f))
                                .border(1.dp, SoftCyan.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = "Approval Shield", tint = SoftCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Glass Shield Approvals", style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary))
                            Text(
                                text = if (pendingApprovals > 0) "$pendingApprovals hold(s) waiting for verification" else "All staged holds resolved",
                                style = MaterialTheme.typography.bodySmall.copy(color = if (pendingApprovals > 0) WarningAmber else SuccessMint)
                            )
                        }
                    }
                    StatusGlassPill(label = if (pendingApprovals > 0) "$pendingApprovals Open" else "Open", color = SoftCyan)
                }
            }
        }

        item {
            GlassTabBar(
                tabs = listOf("All", "Upcoming", "Completed", "Cancelled"),
                selectedIndex = tabIndex,
                onSelectTab = { tabIndex = it }
            )
        }

        if (filtered.isEmpty()) {
            item {
                GlassStateView(
                    title = "No reservations in this filter",
                    message = "Stage a flight, hotel, dining, or car hold and approve it inside Glass Shield to issue a voucher.",
                    actionLabel = "Search Flights",
                    onAction = { onNavigateSub(SubDestination.FLIGHTS) }
                )
            }
        } else {
            items(filtered, key = { it.referenceCode }) { booking ->
                GlassBookingCard(
                    referenceCode = booking.referenceCode,
                    title = booking.title,
                    providerSource = booking.providerSource,
                    providerPnr = booking.providerPnr,
                    amountUsd = booking.amountUsd,
                    status = booking.status,
                    timestamp = booking.timestamp,
                    onClick = { selectedBooking = booking }
                )
            }
        }
    }

    // Reference 2 Boarding Pass & Voucher QR Sheet
    selectedBooking?.let { booking ->
        GlassBottomSheet(onDismissRequest = { selectedBooking = null }) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatusGlassPill(
                    label = booking.status,
                    color = if (booking.status == "CANCELLED") ErrorCoral else SuccessMint
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(booking.title, style = MaterialTheme.typography.titleLarge.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold))
                Text("Ref: ${booking.referenceCode} • PNR: ${booking.providerPnr ?: "Pending"}", style = MaterialTheme.typography.labelMedium.copy(color = SoftCyan))
                Spacer(modifier = Modifier.height(14.dp))

                GlassCard(material = GlassMaterial.CLEAR, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Provider", style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
                            Text(booking.providerSource, style = MaterialTheme.typography.bodySmall.copy(color = glass.textPrimary))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Verified Amount", style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
                            Text("$${booking.amountUsd.toInt()}", style = TabularNumberStyle.copy(color = SuccessMint, fontSize = 16.sp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                Text("Scan the QR pass below", style = MaterialTheme.typography.labelMedium.copy(color = glass.textSecondary))
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(152.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PearlWhite)
                        .border(1.5.dp, SoftCyan.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    QrMatrixGraphic(seed = booking.referenceCode.hashCode())
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "SHA-256: ${booking.sha256Hash.take(28)}...",
                    style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted, fontFamily = FontFamily.Monospace)
                )
                Spacer(modifier = Modifier.height(14.dp))
                if (booking.status != "CANCELLED") {
                    GlassButton(
                        text = "Request Cancellation",
                        onClick = {
                            viewModel.cancelConfirmedBooking(booking.referenceCode, booking.remoteDocId)
                            selectedBooking = null
                        },
                        isPrimary = false,
                        material = GlassMaterial.CLEAR,
                        accentColor = ErrorCoral,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun QrMatrixGraphic(seed: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cells = 9
        val cellSize = size.minDimension / cells
        for (r in 0 until cells) {
            for (c in 0 until cells) {
                val isCorner = (r < 3 && c < 3) || (r < 3 && c >= cells - 3) || (r >= cells - 3 && c < 3)
                val bit = isCorner || (((r * 31 + c * 17 + seed) and 3) == 0)
                if (bit) {
                    drawRect(
                        color = Color(0xFF07132E),
                        topLeft = Offset(c * cellSize + 1f, r * cellSize + 1f),
                        size = Size(cellSize - 2f, cellSize - 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovalCenterScreen(
    viewModel: AlloraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val approvals by viewModel.approvalRequests.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    BackHandler(onBack = onBack)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeaderBar(
                title = "Approval Shield",
                subtitle = "Zero Autonomous Spend • Two-Phase Verification",
                onBack = onBack
            )
        }

        // Luminous Glass Shield Hero Banner
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                material = GlassMaterial.LUMINOUS,
                accentBorder = SoftCyan.copy(alpha = 0.65f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(listOf(SoftCyan.copy(alpha = 0.45f), Color.Transparent)),
                                radius = size.minDimension / 2f
                            )
                            drawCircle(
                                brush = Brush.sweepGradient(listOf(SoftCyan, ElectricBlue, IridescentPink, SoftCyan)),
                                radius = size.minDimension * 0.42f,
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                        Icon(Icons.Default.Shield, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Protected Authorization Gate", style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary))
                        Text(
                            text = "Biometric threshold: $${profile.biometricThresholdUsd.toInt()} USD. Amounts above require explicit review.",
                            style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
                        )
                    }
                }
            }
        }

        items(approvals, key = { it.id }) { req ->
            val exceedsLimit = req.amountUsd > profile.biometricThresholdUsd
            val statusColor = when (req.status) {
                ApprovalStatus.PENDING -> WarningAmber
                ApprovalStatus.APPROVED -> SuccessMint
                ApprovalStatus.REJECTED -> ErrorCoral
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                material = if (req.status == ApprovalStatus.PENDING) GlassMaterial.DEEP else GlassMaterial.FROSTED,
                accentBorder = statusColor.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusGlassPill(label = "${req.status.name} • ${req.riskLevel}", color = statusColor)
                    Text("$${req.amountUsd.toInt()}", style = TabularNumberStyle.copy(fontSize = 20.sp, color = glass.textPrimary))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(req.title, style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary))
                Text(req.merchantOrProvider, style = MaterialTheme.typography.labelMedium.copy(color = SoftCyan))
                Spacer(modifier = Modifier.height(6.dp))
                Text(req.details, style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Policy: ${req.providerPolicy}", style = MaterialTheme.typography.labelSmall.copy(color = SoftMint))
                if (exceedsLimit && req.status == ApprovalStatus.PENDING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exceeds $${profile.biometricThresholdUsd.toInt()} pre-auth limit • Expires in ${req.expiresInMinutes}m",
                            style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber)
                        )
                    }
                }
                if (req.status == ApprovalStatus.PENDING) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassButton(
                            text = "Decline",
                            onClick = { viewModel.rejectRequest(req.id) },
                            isPrimary = false,
                            material = GlassMaterial.CLEAR,
                            leadingIcon = Icons.Default.Close,
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = "Approve & Commit",
                            onClick = { viewModel.approveRequest(req.id) },
                            isPrimary = true,
                            material = GlassMaterial.PEARL,
                            leadingIcon = Icons.Default.Check,
                            testTag = "approve_button_${req.id}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Phase0AuditHubScreen(
    viewModel: AlloraViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val items by viewModel.auditItems.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }

    BackHandler(onBack = onBack)

    val filtered = remember(items, query, filter) {
        items.filter { repo ->
            val matchesCat = when (filter) {
                "Travel" -> repo.category == "Travel Priority"
                "Live API" -> repo.isRealApi
                "Mock/Demo" -> !repo.isRealApi
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                repo.name.contains(query, true) ||
                repo.primaryStack.contains(query, true) ||
                "#${repo.number}".contains(query, true)
            matchesCat && matchesQuery
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeaderBar(title = "Phase 0 Audit Hub", subtitle = "${items.size} Repositories Audited • Read-Only Report", onBack = onBack)
        }
        item {
            GlassSearchBar(
                query = query,
                onQueryChange = { query = it },
                material = GlassMaterial.CLEAR,
                placeholder = "Search repo #9, #80, React, Node.js..."
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Travel", "Live API", "Mock/Demo").forEach { tag ->
                    GlassChip(label = tag, selected = filter == tag, onClick = { filter = tag })
                }
            }
        }
        items(filtered, key = { it.number }) { repo ->
            val sevColor = when (repo.securitySeverity) {
                "CRITICAL" -> ErrorCoral
                "HIGH" -> WarningAmber
                else -> SoftCyan
            }
            GlassCard(modifier = Modifier.fillMaxWidth(), accentBorder = sevColor.copy(alpha = 0.45f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusGlassPill(label = "#${repo.number} • ${repo.category}", color = Lavender)
                    StatusGlassPill(label = repo.securitySeverity, color = sevColor)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(repo.name, style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary))
                Text(repo.primaryStack, style = MaterialTheme.typography.labelSmall.copy(color = SoftCyan))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Booking/API Realism: ${repo.bookingRealism}", style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Security: ${repo.securityFindings}", style = MaterialTheme.typography.bodySmall.copy(color = sevColor))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Recommendation: ${repo.recommendation}", style = MaterialTheme.typography.labelSmall.copy(color = SoftMint, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}
