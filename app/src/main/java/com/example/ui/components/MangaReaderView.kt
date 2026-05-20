package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import java.util.UUID
import com.example.data.MangaEntity
import org.json.JSONArray

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaReaderView(
    manga: MangaEntity,
    initialChapter: Int,
    isVerticalMode: Boolean,
    onClose: () -> Unit,
    onChapterChanged: (Int) -> Unit,
    onProgressUpdated: (Int, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeChapter by remember { mutableStateOf(initialChapter) }
    var scale by remember { mutableStateOf(1f) }
    var zoomLevelText by remember { mutableStateOf("100%") }
    var showControlUi by remember { mutableStateOf(true) }
    var brightnessSetting by remember { mutableStateOf(1.0f) }

    val context = LocalContext.current

    // Parse image page URLs from the JSON
    val pageImages = remember(manga, activeChapter) {
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(manga.pagesJson)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // Default fallbacks in case of paring failure
            list.add("https://picsum.photos/id/1015/800/1200")
            list.add("https://picsum.photos/id/1016/800/1200")
        }
        list
    }

    // Handle background tint according to brightness simulation
    val backgroundTint = Color.Black.copy(alpha = 1.0f - brightnessSetting * 0.4f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .testTag("manga_reader_parent")
    ) {
        // Main Comic Panels rendering
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showControlUi = !showControlUi } // Toggle bars on tap
        ) {
            if (isVerticalMode) {
                // WEBTOON STYLE: infinite vertical scrolling column
                val lazyListState = rememberLazyListState()

                // Save reading progress on scroll
                LaunchedEffect(lazyListState.firstVisibleItemIndex) {
                    if (pageImages.isNotEmpty()) {
                        val percentage = (lazyListState.firstVisibleItemIndex.toFloat() / pageImages.size) * 100f
                        onProgressUpdated(activeChapter, percentage)
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .testTag("vertical_webtoon_scroller"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(pageImages) { index, pageUrl ->
                        val secureUrl = remember(pageUrl) {
                            val uniqueId = UUID.randomUUID().toString().take(8)
                            if (pageUrl.contains("?")) "$pageUrl&sec_tok=tmp_$uniqueId" else "$pageUrl?sec_tok=tmp_$uniqueId"
                        }
                        val imageRequest = remember(secureUrl) {
                            ImageRequest.Builder(context)
                                .data(secureUrl)
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                .build()
                        }
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "صفحه ${index + 1} از مانهوا ${manga.titleFa}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            } else {
                // MANGA STYLE: Page-by-page horizontal swiper
                val pagerState = rememberPagerState(pageCount = { pageImages.size })

                LaunchedEffect(pagerState.currentPage) {
                    if (pageImages.isNotEmpty()) {
                        val percentage = (pagerState.currentPage.toFloat() / pageImages.size) * 100f
                        onProgressUpdated(activeChapter, percentage)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .testTag("horizontal_manga_pager"),
                    reverseLayout = true // Manga is read right-to-left
                ) { page ->
                    val pageUrl = pageImages[page]
                    val secureUrl = remember(pageUrl) {
                        val uniqueId = UUID.randomUUID().toString().take(8)
                        if (pageUrl.contains("?")) "$pageUrl&sec_tok=tmp_$uniqueId" else "$pageUrl?sec_tok=tmp_$uniqueId"
                    }
                    val imageRequest = remember(secureUrl) {
                        ImageRequest.Builder(context)
                            .data(secureUrl)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .build()
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "صفحه ${page + 1} از مانهوا ${manga.titleFa}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Page indicator overlays
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 90.dp, end = 20.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "صفحه ${pagerState.currentPage + 1} از ${pageImages.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlay brightness shader
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundTint)
                .clickable(enabled = false) {}
        )

        // Top Navigation and Title Bar
        AnimatedVisibility(
            visible = showControlUi,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.95f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                // Status area offset
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Action Control Panel Tools
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom in
                        IconButton(
                            onClick = {
                                if (scale < 1.8f) {
                                    scale += 0.2f
                                    zoomLevelText = "${(scale * 100).toInt()}%"
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF23262B))
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "بزرگنمایی", tint = Color.White)
                        }

                        // Zoom out
                        IconButton(
                            onClick = {
                                if (scale > 1.0f) {
                                    scale -= 0.2f
                                    zoomLevelText = "${(scale * 100).toInt()}%"
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF23262B))
                        ) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "کوچکنمایی", tint = Color.White)
                        }

                        // Zoom level indicator
                        Text(
                            text = zoomLevelText,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Title & Close Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = manga.titleFa,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "فصل $activeChapter - ${manga.translatorTeam}",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "خروج از ریدر",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Bottom Settings Dashboard Control
        AnimatedVisibility(
            visible = showControlUi,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.98f))
                        )
                    )
                    .padding(20.dp)
            ) {
                // Brightness adjustment bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.LightMode, contentDescription = null, tint = Color.Gray)
                    Slider(
                        value = brightnessSetting,
                        onValueChange = { brightnessSetting = it },
                        valueRange = 0.2f..1.0f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFA8C7FA),
                            activeTrackColor = Color(0xFFA8C7FA),
                            inactiveTrackColor = Color(0xFF2D3139)
                        )
                    )
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chapter navigation row + format conversion switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FORMAT SWITCH (Webtoon vertical scroll vs classic horizontal swipe)
                    Button(
                        onClick = { onChapterChanged(if (isVerticalMode) 1 else 0) }, // Use parent toggle handler safely
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23262B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (isVerticalMode) Icons.Default.SwapVert else Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Color(0xFFA8C7FA),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isVerticalMode) "نمایش ستونی (وبتون)" else "نمایش برگی (مانگا)",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Pre/Next Chapter action triggers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous chapter
                        Button(
                            onClick = {
                                if (activeChapter > 1) {
                                    activeChapter--
                                    onChapterChanged(activeChapter)
                                }
                            },
                            enabled = activeChapter > 1,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeChapter > 1) Color(0xFF1D2024) else Color(0xFF16191E)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("فصل قبل", fontSize = 11.sp, color = if (activeChapter > 1) Color.White else Color.Gray)
                        }

                        // Badge count displaying current state
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF003366), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "فصل $activeChapter",
                                color = Color(0xFFA8C7FA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Next chapter
                        Button(
                            onClick = {
                                if (activeChapter < manga.chaptersCount) {
                                    activeChapter++
                                    onChapterChanged(activeChapter)
                                }
                            },
                            enabled = activeChapter < manga.chaptersCount,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeChapter < manga.chaptersCount) Color(0xFF1D2024) else Color(0xFF16191E)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("فصل بعد", fontSize = 11.sp, color = if (activeChapter < manga.chaptersCount) Color.White else Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
