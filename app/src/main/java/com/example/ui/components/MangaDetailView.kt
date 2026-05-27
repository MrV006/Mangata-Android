package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import coil.compose.AsyncImage
import com.example.data.ChapterItem
import com.example.data.MangaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailView(
    manga: MangaItem,
    chapters: List<ChapterItem>,
    userRole: String,
    onBack: () -> Unit,
    onReadChapter: (ChapterItem) -> Unit
) {
    var isDescExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = manga.title, 
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF141218).copy(alpha = 0.95f),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0C0A0F) // Obsidian dark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // COVER & METADATA SECTION
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Blurry back background
                    AsyncImage(
                        model = manga.coverImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.15f
                    )

                    // Bottom ambient glow gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF0C0A0F)),
                                    startY = 50f
                                )
                            )
                    )

                    // Content layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Floating Cover card
                        AsyncImage(
                            model = manga.coverImage,
                            contentDescription = manga.title,
                            modifier = Modifier
                                .width(120.dp)
                                .height(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFFF7597).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Meta details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Badge types
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFF7597).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("مانهوا کره‌ای", color = Color(0xFFFF7597), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF03DAC6).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("در حال پخش", color = Color(0xFF03DAC6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = manga.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = Color.White,
                                lineHeight = 28.sp
                            )

                            if (!manga.author.isNullOrBlank()) {
                                Text(
                                    text = "سازنده / نویسنده: ${manga.author}",
                                    color = Color(0xFFFF7597),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!manga.genres.isNullOrBlank()) {
                                Text(
                                    text = "ژانرها: ${manga.genres}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "امتیاز", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Text("امتیاز کاربری: 9.8 / 10", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            val yearLabel = if (!manga.releaseYear.isNullOrBlank()) "سال ${manga.releaseYear}" else manga.createdAt.take(10)
                            Text(
                                text = "انتشار: $yearLabel",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )

                            if (!manga.mainCharacters.isNullOrBlank()) {
                                Text(
                                    text = "شخصیت‌های اصلی: ${manga.mainCharacters}",
                                    color = Color.LightGray.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // EXPANSIBLE DESCRIPTION / SYNOPSIS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16141F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .animateContentSize()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF7597), modifier = Modifier.size(18.dp))
                            Text("خلاصه و داستان مانهوا", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = manga.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            maxLines = if (isDescExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )

                        Text(
                            text = if (isDescExpanded) "نمایش کمتر ▲" else "ادامه داستان مانهوا ▼",
                            color = Color(0xFFFF7597),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { isDescExpanded = !isDescExpanded }
                                .padding(top = 8.dp)
                        )
                    }
                }
            }

            // CHAPTERS SECTION HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                        Text("فصل‌های منتشر شده فعال", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF1E1B24))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("${chapters.size} چپتر در سیستم", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }

            // LIST OF CHAPTERS
            if (chapters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هنوز هیچ چپتری برای این اثر منتشر نشده است. ⌛", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(chapters) { chapter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16141F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "چپتر ${chapter.chapterNumber.toString().replace(".0", "")}",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = chapter.title.ifEmpty { "بدون نام برای چپتر" },
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { onReadChapter(chapter) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7597)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("خوانش", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
