package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AiSummaryState

@Composable
fun GeminiReviewSummary(
    state: AiSummaryState,
    modifier: Modifier = Modifier
) {
    // Beautiful AI-themed dual gradient (purple representing wisdom/Gemini)
    val aiGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF8E2DE2),
            Color(0xFF4A00E0)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("gemini_ai_summary_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End // RTL Farsi
        ) {
            // Header showing Gemini auto awesome icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gradient spark badge
                Box(
                    modifier = Modifier
                        .background(aiGradient, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "بروزرسانی نسخه آزمایشی",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "تحلیل هوشمند نظرات با Gemini AI",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Right
                    )
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "تحلیل هوش مصنوعی",
                        tint = Color(0xFFC490FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body rendering based on states
            Crossfade(targetState = state, label = "GeminiStateReveal") { summaryState ->
                when (summaryState) {
                    is AiSummaryState.Idle -> {
                        Text(
                            "در حال راه‌اندازی برای تحلیل...",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right
                        )
                    }
                    is AiSummaryState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "هوش مصنوعی در حال تحلیل داستان و نظرات خوانندگان مانهوا...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF8E2DE2)
                            )
                        }
                    }
                    is AiSummaryState.Success -> {
                        Text(
                            text = summaryState.summary,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Right,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is AiSummaryState.Error -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = summaryState.message,
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "خطا",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Inline helper for loading indicator size compatibility
private fun buttonDefaultsCircularProgressIndicatorSize() = 18.dp
