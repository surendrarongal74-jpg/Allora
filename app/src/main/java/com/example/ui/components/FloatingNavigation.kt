package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite

enum class AlloraTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val orbPrompt: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "Ask ALLORA"),
    DISCOVER("Discover", Icons.Filled.Explore, Icons.Outlined.Explore, "Explore"),
    TRIPS("Trips", Icons.Filled.FlightTakeoff, Icons.Outlined.FlightTakeoff, "Plan trip"),
    BOOKINGS("Bookings", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber, "Approvals"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "Settings")
}

/**
 * 5. FloatingNavigation — Transparent liquid-glass capsule navigation with Pearl Glass active pill
 * paired with the integrated FloatingAssistantOrb, adaptively proportioned from 320dp narrow phones up.
 */
@Composable
fun FloatingNavigation(
    selectedTab: AlloraTab,
    pendingApprovalsCount: Int,
    orbState: OrbState,
    reducedMotion: Boolean,
    onSelectTab: (AlloraTab) -> Unit,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val capsuleShape = remember { RoundedCornerShape(42.dp) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("floating_navigation_container")
    ) {
        val isUltraCompact = maxWidth < 350.dp
        val showActiveTabLabel = maxWidth >= 344.dp
        val showOrbLabel = maxWidth >= 400.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isUltraCompact) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Primary Floating Transparent Capsule
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp)
                    .clip(capsuleShape)
                    .background(
                        Brush.horizontalGradient(
                            if (glass.isDark) {
                                listOf(Color(0xA807122A), Color(0x980D2047))
                            } else {
                                listOf(Color(0xE8FFFFFF), Color(0xDCEAF2FF))
                            }
                        ),
                        capsuleShape
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(glass.innerHighlight, glass.glassBorder)),
                        capsuleShape
                    )
                    .padding(horizontal = 5.dp, vertical = 5.dp)
                    .testTag("allora_floating_navigation"),
                horizontalArrangement = Arrangement.spacedBy(if (isUltraCompact) 2.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlloraTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val pillColor by animateColorAsState(
                        targetValue = if (isSelected) glass.activeNavPill else Color(0x16FFFFFF),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "navPillColor"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) glass.activeNavContent else glass.inactiveNavContent,
                        label = "navContentColor"
                    )
                    val slotWeight = if (isSelected && showActiveTabLabel) 2.15f else 1f

                    Row(
                        modifier = Modifier
                            .weight(slotWeight)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                if (isSelected) {
                                    resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass)
                                } else {
                                    Brush.horizontalGradient(listOf(pillColor, pillColor))
                                },
                                RoundedCornerShape(32.dp)
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.6.dp,
                                color = if (isSelected) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .clickable { onSelectTab(tab) }
                            .testTag("nav_tab_${tab.name.lowercase()}")
                            .padding(horizontal = if (isSelected && showActiveTabLabel) 8.dp else 4.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val iconContent: @Composable () -> Unit = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = contentColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        if (tab == AlloraTab.BOOKINGS && pendingApprovalsCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = ErrorCoral,
                                        contentColor = PearlWhite
                                    ) {
                                        Text("$pendingApprovalsCount", fontSize = 9.sp)
                                    }
                                }
                            ) {
                                iconContent()
                            }
                        } else {
                            iconContent()
                        }

                        if (isSelected && showActiveTabLabel) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = contentColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Integrated Floating AI Assistant Orb beside the navigation pill
            FloatingAssistantOrb(
                promptLabel = selectedTab.orbPrompt,
                orbState = orbState,
                reducedMotion = reducedMotion,
                showLabel = showOrbLabel,
                onClick = onOrbClick
            )
        }
    }
}

/**
 * Adaptive Glass Sidebar / Navigation Rail for Tablet, Foldable & Wide displays.
 */
@Composable
fun AlloraGlassNavigationRail(
    selectedTab: AlloraTab,
    pendingApprovalsCount: Int,
    orbState: OrbState,
    onSelectTab: (AlloraTab) -> Unit,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(28.dp) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(104.dp)
            .padding(vertical = 16.dp, horizontal = 10.dp)
            .clip(shape)
            .background(resolveGlassSurfaceBrush(GlassMaterial.FROSTED, glass), shape)
            .border(1.dp, resolveGlassBorderBrush(GlassMaterial.FROSTED, glass), shape)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AlloraAiOrb(
                state = orbState,
                size = 56.dp,
                onClick = onOrbClick
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ALLORA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = glass.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AlloraTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSelected) resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass)
                            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val tint = if (isSelected) glass.activeNavContent else glass.inactiveNavContent
                    if (tab == AlloraTab.BOOKINGS && pendingApprovalsCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = ErrorCoral, contentColor = PearlWhite) {
                                    Text("$pendingApprovalsCount")
                                }
                            }
                        ) {
                            Icon(if (isSelected) tab.selectedIcon else tab.unselectedIcon, contentDescription = tab.title, tint = tint)
                        }
                    } else {
                        Icon(if (isSelected) tab.selectedIcon else tab.unselectedIcon, contentDescription = tab.title, tint = tint)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = tint,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Transparent))
    }
}
