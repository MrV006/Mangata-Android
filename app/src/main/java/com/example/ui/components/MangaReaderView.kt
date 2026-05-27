package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.ChapterItem
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun MangaReaderView(
    chapter: ChapterItem,
    mangaTitle: String,
    chaptersList: List<ChapterItem> = emptyList(),
    onChapterChanged: (ChapterItem) -> Unit = {},
    onCloseReader: () -> Unit
) {
    // Parse the image urls list from gson string
    val imagesList: List<String> = try {
        if (!chapter.imagesJson.isNullOrEmpty()) {
            val parsed = Gson().fromJson(chapter.imagesJson, Array<String>::class.java).toList()
            // Natural numeric sort on file names (e.g. extracts 1, 2, 10 out of .../1.png, .../2.png)
            parsed.sortedWith { u1, u2 ->
                val f1 = u1.substringAfterLast("/").substringBeforeLast(".")
                val f2 = u2.substringAfterLast("/").substringBeforeLast(".")
                val n1 = f1.filter { it.isDigit() }.toIntOrNull()
                val n2 = f2.filter { it.isDigit() }.toIntOrNull()
                if (n1 != null && n2 != null) {
                    n1.compareTo(n2)
                } else {
                    f1.compareTo(f2, ignoreCase = true)
                }
            }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    // Toggle overlay controls
    var isOverlaysVisible by remember { mutableStateOf(true) }
    
    // UI Customize settings
    var selectedBgColor by remember { mutableStateOf(Color(0xFF000000)) }
    var selectedFitMode by remember { mutableStateOf(ContentScale.FillWidth) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Is the background in a light eye-comfort tone
    val isBgLight = selectedBgColor == Color(0xFFFAF0E6)
    val overlayBackground = if (isBgLight) Color(0xFFFAF0E6).copy(alpha = 0.95f) else Color.Black.copy(alpha = 0.9f)
    val readerTextColor = if (isBgLight) Color(0xFF1B1A1E) else Color.White
    val readerSubColor = if (isBgLight) Color(0xFFFF5722) else Color(0xFFFF7597)

    // Scroll state & reactive progress percentage calculation
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val scrollPercentage by remember {
        derivedStateOf {
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            if (totalItems <= 1) 0f
            else {
                val visibleIndex = lazyListState.firstVisibleItemIndex
                val offset = lazyListState.firstVisibleItemScrollOffset
                val totalOffset = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 1
                val fraction = offset.toFloat() / totalOffset
                val progress = (visibleIndex + fraction) / (totalItems - 1)
                (progress * 100).coerceIn(0f, 100f)
            }
        }
    }

    // Find next/prev chapters
    val currentChapterIndex = chaptersList.indexOfFirst { it.id == chapter.id }
    val prevChapter = if (currentChapterIndex > 0) chaptersList[currentChapterIndex - 1] else null
    val nextChapter = if (currentChapterIndex >= 0 && currentChapterIndex < chaptersList.size - 1) chaptersList[currentChapterIndex + 1] else null

    Scaffold(
        containerColor = selectedBgColor,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isOverlaysVisible = !isOverlaysVisible
                }
        ) {
            if (imagesList.isEmpty()) {
                // Highly polished Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141218)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF7597).copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Empty",
                                tint = Color(0xFFFF7597),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "چپتر فاقد تصویر است",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "این خطا ممکن است به علت ناقص بودن فرمت فایل زیپ آپلود شده در سرور مانهوایی رخ داده باشد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 1.6.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onCloseReader,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7597))
                            ) {
                                Text("بازگشت به مانهوا", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Interactive Chapter Pages Content
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(imagesList) { _, imageUrl ->
                        ZoomableMangaPage(
                            imageUrl = imageUrl,
                            contentScale = selectedFitMode,
                            modifier = Modifier.fillMaxWidth(),
                            onTap = {
                                isOverlaysVisible = !isOverlaysVisible
                            }
                        )
                    }

                    // Luxury Chapter Finished Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isBgLight) Color(0xFFEFE6DD) else Color(0xFF141218)),
                            border = BorderStroke(1.dp, readerSubColor.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Read Done",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "پایان عالی این فصل!",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = readerTextColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "مطالعه چپتر ${chapter.chapterNumber} مانهوا با موفقیت به اتمام رسید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isBgLight) Color.DarkGray else Color.Gray,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                if (nextChapter != null) {
                                    Button(
                                        onClick = { onChapterChanged(nextChapter) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7597)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "مطالعه چپتر بعدی (${nextChapter.chapterNumber}) 👁️",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "شما در حال حاضر آخرین چپتر منتشر شده مانگاتا را لود کرده‌اید ✨",
                                        color = readerSubColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top immersive bar (Smooth Motion Inset Overlay)
            AnimatedVisibility(
                visible = isOverlaysVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(overlayBackground)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mangaTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = readerTextColor
                        )
                        Text(
                            text = "چپتر ${chapter.chapterNumber} ${chapter.title ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = readerSubColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onCloseReader,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Reader",
                            tint = readerTextColor
                        )
                    }
                }
            }

            // Bottom immersive controllers (Slide in dynamically)
            AnimatedVisibility(
                visible = isOverlaysVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.0f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 1.0f)
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Progress Slider Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${scrollPercentage.toInt()}%",
                                color = Color(0xFFFF7597),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            LinearProgressIndicator(
                                progress = scrollPercentage / 100f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFFF7597),
                                trackColor = Color.White.copy(alpha = 0.2f),
                            )
                            
                            IconButton(
                                onClick = { isSettingsOpen = true },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Left & Right Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev Chapter
                            if (prevChapter != null) {
                                TextButton(
                                    onClick = { onChapterChanged(prevChapter) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Prev",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("چپتر ${prevChapter.chapterNumber}", fontSize = 12.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(40.dp))
                            }

                            // Center Jump Scroll to Top Action
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        lazyListState.animateScrollToItem(0)
                                    }
                                },
                                modifier = Modifier
                                    .background(Color(0xFF33303E), CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Top",
                                    tint = Color.White
                                )
                            }

                            // Next Chapter
                            if (nextChapter != null) {
                                TextButton(
                                    onClick = { onChapterChanged(nextChapter) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF7597)),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("چپتر ${nextChapter.chapterNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet System / Dialog config for appearance theme switcher
    if (isSettingsOpen) {
        AlertDialog(
            onDismissRequest = { isSettingsOpen = false },
            confirmButton = {
                TextButton(onClick = { isSettingsOpen = false }) {
                    Text("بستن تنظیمات", color = Color(0xFFFF7597))
                }
            },
            title = {
                Text(
                    text = "تنظیمات نمایشی صفحه خواننده",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = Color(0xFF16141D),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    // Accent Text
                    Text(
                        text = "تم اتمسفر پس‌زمینه ریدر:",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val colorThemes = listOf(
                            Triple(Color(0xFF000000), "تاریک مطلق", Color.White),
                            Triple(Color(0xFF121016), "Midnight", Color.White),
                            Triple(Color(0xFFFAF0E6), "سپیای ملایم", Color(0xFF1E1E1E)),
                            Triple(Color(0xFF252528), "Charcoal", Color.White)
                        )
                        colorThemes.forEach { (bgColor, name, strokeColor) ->
                            val isSelected = selectedBgColor == bgColor
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { selectedBgColor = bgColor }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFFF7597) else Color.DarkGray,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "نحوه هماهنگ‌سازی تصاویر مانهوا:",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectedFitMode = ContentScale.FillWidth },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFitMode == ContentScale.FillWidth) Color(0xFFFF7597) else Color(0xFF272433)
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("عریض وب‌تون (موبایل)", fontSize = 10.sp, color = Color.White)
                        }
                        Button(
                            onClick = { selectedFitMode = ContentScale.Fit },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFitMode == ContentScale.Fit) Color(0xFFFF7597) else Color(0xFF272433)
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("کامل فیت (تبلت)", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ZoomableMangaPage(
    imageUrl: String,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }
    var lastClickTime by remember { mutableStateOf(0L) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 300) {
                    if (scale > 1f) {
                        scale = 1f
                        offset = Offset.Zero
                    } else {
                        scale = 2.5f
                    }
                } else {
                    onTap()
                }
                lastClickTime = currentTime
            }
            .transformable(state = state, enabled = scale > 1f)
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Manga Page",
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF7597),
                        strokeWidth = 3.dp
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF1E1B24)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFFF7597)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "خطا در بارگذاری آنلاین صفحه مانهوا",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = contentScale
        )
    }
}
