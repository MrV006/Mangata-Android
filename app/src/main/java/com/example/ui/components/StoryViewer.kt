package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.StoryEntity
import com.example.ui.MovieViewModel
import kotlinx.coroutines.delay

@Composable
fun StoryViewerTray(
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier
) {
    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUserAccount.collectAsState()
    val context = LocalContext.current

    var selectedStory by remember { mutableStateOf<StoryEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Seed empty default stories if database remains empty
    LaunchedEffect(stories) {
        if (stories.isEmpty()) {
            viewModel.postStory(
                mediaUrl = "https://picsum.photos/id/1015/800/1200",
                caption = "پیش‌نمایش آرت فصل جدید سولو لولینگ لو رفت! 🔥"
            )
            viewModel.postStory(
                mediaUrl = "https://picsum.photos/id/1025/800/1200",
                caption = "تیزر رسمی چپتر این هفته برج خدا منتشر شد."
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "برخط مانهوا (داستان‌ها)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            textAlign = TextAlign.Right
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true // Fits Persian RTL
        ) {
            // "+" Add Story Button for Staff or Admin
            currentUser?.let { user ->
                if (user.role == "SUPER_ADMIN" || user.role == "STAFF" || user.role == "DEPT_ADMIN") {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showCreateDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16191E))
                                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF))), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "ارسال استوری",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "ارسال خبر",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Normal stories in database
            items(stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedStory = story }
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .border(
                                2.dp,
                                Brush.sweepGradient(listOf(Color(0xFFFF007A), Color(0xFF9E00FF), Color(0xFFFFCC00), Color(0xFFFF007A))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = story.mediaUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.staffName,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(64.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Full Screen story viewer dialog
        selectedStory?.let { story ->
            FullscreenStoryDialog(
                story = story,
                viewModel = viewModel,
                currentUser = currentUser,
                onDismiss = { selectedStory = null }
            )
        }

        // Publish story layout
        if (showCreateDialog) {
            StoryCreationDialog(
                viewModel = viewModel,
                onDismiss = { showCreateDialog = false }
            )
        }
    }
}

@Composable
fun FullscreenStoryDialog(
    story: StoryEntity,
    viewModel: MovieViewModel,
    currentUser: com.example.data.UserAccount?,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var paused by remember { mutableStateOf(false) }

    // Timer effect
    LaunchedEffect(paused) {
        if (!paused) {
            while (progress < 1f) {
                delay(100)
                progress += 0.01f
            }
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { paused = !paused }
        ) {
            // Full screen Image
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Top Status Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Stories horizontal progress status bar
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
                    }

                    // Sender profile info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(story.staffName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(story.staffRole, color = Color.LightGray, fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(story.staffName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom caption card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        story.caption,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Right,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Delete button for Super Admins
                        if (currentUser?.role == "SUPER_ADMIN" || currentUser?.id == story.staffId) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteStory(story.id)
                                    onDismiss()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف استوری", tint = Color(0xFFFF5252))
                            }
                        } else {
                            Box(modifier = Modifier.size(2.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("در حال پخش ویدیو (۳۰ ثانیه)", color = Color.Gray, fontSize = 11.sp)
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCreationDialog(
    viewModel: MovieViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUserAccount.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val context = LocalContext.current

    var captionInput by remember { mutableStateOf("") }
    var selectedMediaUrl by remember { mutableStateOf("https://picsum.photos/id/1029/900/1400") } // Default Mock Story image

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "ارسال داستان جدید (کادر همکاران)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                currentUser?.let { user ->
                    Text(
                        text = "توکن استوری باقی‌مانده شما: ${if (user.role == "SUPER_ADMIN") "نافض" else "${user.storyTokens} استوری"}",
                        color = Color(0xFF00C6FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = "فایلهای پیوست برای ارسال (عکس مانهوا):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Render mock attachment picker options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "https://picsum.photos/id/1029/800/1200" to "پوستر برج خدا",
                        "https://picsum.photos/id/1043/800/1200" to "پوستر خانه شیرین",
                        "https://picsum.photos/id/1051/800/1200" to "حرامزاده دارک"
                    ).forEach { (url, label) ->
                        val isSelected = selectedMediaUrl == url
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0055B3) else Color(0xFF16191E))
                                .border(1.dp, if (isSelected) Color(0xFF00C6FF) else Color(0xFF2D3139), RoundedCornerShape(8.dp))
                                .clickable { selectedMediaUrl = url }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = captionInput,
                    onValueChange = { captionInput = it },
                    label = { Text("کپشن استوری (معرفی اثر / خبر تیم)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF0055B3)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "فیلم و گیف‌ها بر اساس آیین‌نامه‌ها حداکثر تا ${settings.maxVideoStoryDurationSeconds} ثانیه محدود خواهند بود و کلیه فایل‌ها در شروع ماه جدید پاکسازی می‌شوند تا فضای کاربری تخلیه شود.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Right
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (captionInput.isNotEmpty()) {
                        viewModel.postStory(
                            mediaUrl = selectedMediaUrl,
                            caption = captionInput,
                            onSuccess = {
                                Toast.makeText(context, "استوری شما با موفقیت منتشر شد!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "کپشن استوری خالی است", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("منتشر کردن داستان", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لغو", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E2229),
        shape = RoundedCornerShape(16.dp)
    )
}
