package com.vitanova.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: androidx.compose.ui.graphics.Color
)

private val pages = listOf(
    OnboardingPage(Icons.Filled.Nightlight, "Track Your Sleep", "Automatic sleep detection with smart alarm and hypnogram analysis.", SleepAccent),
    OnboardingPage(Icons.Filled.FavoriteBorder, "Measure Energy", "Camera-based HRV measurement with clinical-grade algorithms.", EnergyAccent),
    OnboardingPage(Icons.Filled.FitnessCenter, "Train Smart", "GPS tracking, workout programs, and ACWR monitoring.", FitnessAccent),
    OnboardingPage(Icons.Filled.Psychology, "Sharpen Your Mind", "Cognitive tests, focus tracking, and brain training.", BrainAccent),
)

@Composable
fun WelcomeScreen(
    onNavigateToProfileSetup: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VitaSurface, VitaBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(VitaGreen, VitaCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("V", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VitaBackground)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "VitaNova",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary
            )
            Text(
                "Your Complete Health Ecosystem",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val item = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(item.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = item.accentColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = VitaTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == i) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (pagerState.currentPage == i) VitaGreen
                                else VitaTextTertiary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onNavigateToProfileSetup()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VitaGreen)
            ) {
                Text(
                    if (pagerState.currentPage < pages.size - 1) "Next" else "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = VitaBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
