package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MangaEntity
import com.example.data.TeamMember
import com.example.ui.MovieViewModel
import com.example.ui.components.GeminiReviewSummary
import com.example.ui.components.MangaReaderView
import com.example.ui.components.MyketPurchaseDialog
import com.example.ui.components.StoryViewerTray
import com.example.ui.components.RecruitmentPortalCard
import com.example.ui.components.StaffDashboard
import com.example.ui.components.AdminPanel
import com.example.ui.components.ForcedUpdateScreen
import com.example.ui.components.ChapterUnlockDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MovieViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val isVipActive by viewModel.isVipActive.collectAsState()
    val readingManga by viewModel.readingManga.collectAsState()
    val activeChapter by viewModel.activeChapter.collectAsState()
    val isVerticalMode by viewModel.isVerticalWebtoonMode.collectAsState()
    val selectedManga by viewModel.selectedManga.collectAsState()
    val showMyketBilling by viewModel.showMyketBillingDialog.collectAsState()
    val selectedSkuToBuy by viewModel.selectedSkuToBuy.collectAsState()

    val serverVersionCode by viewModel.serverVersionCode.collectAsState()
    if (serverVersionCode > 2) {
        ForcedUpdateScreen(onDownloadUpdate = {
            viewModel.updateServerVersionCode(2)
        })
        return
    }

    val darkBackground = Color(0xFF0F1115)
    val accentGold = Color(0xFFFFD700)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = darkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF14171B), // Elegant Dark bottom nav bg
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("app_bottom_bar")
            ) {
                // Navigation items (Home, Bookmarks, Team Hub, Store, Settings)
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "تنظیمات") },
                    label = { Text("تنظیمات", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )

                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "خرید VIP") },
                    label = { Text("خرید VIP", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentGold,
                        selectedTextColor = accentGold,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_store_tab")
                )

                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "تیم همکاران") },
                    label = { Text("تیم مانگاتا", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_team_tab")
                )

                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "برگزیده‌ها") },
                    label = { Text("برگزیده‌ها", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF4081),
                        selectedTextColor = Color(0xFFFF4081),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_bookmarks_tab")
                )

                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "خوانش خانگی") },
                    label = { Text("کتابخانه", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(darkBackground)
        ) {
            // Main sub-page tabs
            when (activeTab) {
                0 -> LibraryDashboard(viewModel = viewModel)
                1 -> BookmarksDashboard(viewModel = viewModel)
                2 -> CollaboratorsDashboard(viewModel = viewModel)
                3 -> StoreDashboard(viewModel = viewModel)
                4 -> SettingsDashboard(viewModel = viewModel)
            }

            // Slide Up Details page overlay
            AnimatedVisibility(
                visible = selectedManga != null,
                enter = slideInVertically(initialOffsetY = { h -> h }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { h -> h }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedManga?.let { manga ->
                    MangaDetailOverlay(
                        manga = manga,
                        viewModel = viewModel,
                        isVipActive = isVipActive,
                        onClose = { viewModel.selectManga(null) }
                    )
                }
            }

            // Real Full Screen Manga Comic Reader View
            AnimatedVisibility(
                visible = readingManga != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                readingManga?.let { manga ->
                    MangaReaderView(
                        manga = manga,
                        initialChapter = activeChapter,
                        isVerticalMode = isVerticalMode,
                        onClose = { viewModel.closeReader() },
                        onChapterChanged = { chapterIndex ->
                            viewModel.setChapter(chapterIndex)
                        },
                        onProgressUpdated = { ch, progress ->
                            viewModel.saveReadingProgress(manga.id, ch, progress)
                        }
                    )
                }
            }

            // Myket purchase checkout dialogue
            if (showMyketBilling && selectedSkuToBuy != null) {
                MyketPurchaseDialog(
                    skuDetails = selectedSkuToBuy!!,
                    onDismiss = { viewModel.closeMyketCheckout() },
                    onConfirmPurchase = { sku ->
                        viewModel.completeSimulatedPurchase(sku)
                    }
                )
            }
        }
    }
}

@Composable
fun LibraryDashboard(viewModel: MovieViewModel) {
    val mangas by viewModel.mangas.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf("همه") } // "همه", "مانهوا", "مانگا", "مانها"

    val filteredList = remember(mangas, searchQuery, selectedCategoryFilter) {
        mangas.filter {
            val titleMatches = it.titleFa.contains(searchQuery, ignoreCase = true) ||
                    it.titleEn.contains(searchQuery, ignoreCase = true)
            val categoryMatches = selectedCategoryFilter == "همه" || it.type == selectedCategoryFilter
            titleMatches && categoryMatches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.End // Supports Persian RTL
    ) {
        // App Header Brand Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF2D3139), RoundedCornerShape(12.dp))
                    .clickable { viewModel.selectTab(4) }, // Open settings tab
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "حساب کاربری", tint = Color.LightGray)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "مانگاتا",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF))),
                            RoundedCornerShape(10.dp)
                        )
                        .size(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        // Circular Live Stories highlight tray curated by Translators / Editors
        StoryViewerTray(viewModel = viewModel)

        // Search text field
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = {
                Text("جستجو میان هزاران فصل مانهوا و مانگا...", color = Color.Gray, fontSize = 13.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "جستجو", tint = Color.Gray)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1D2024),
                unfocusedContainerColor = Color(0xFF1D2024).copy(alpha = 0.8f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .border(1.dp, Color(0xFF2D3139), RoundedCornerShape(14.dp))
                .testTag("manga_search_field")
        )

        // Type select chips (همه / مانهوا / مانگا / مانها)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            listOf("مانها", "مانگا", "مانهوا", "همه").forEach { category ->
                val isSelected = selectedCategoryFilter == category
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) Color(0xFF0055B3) else Color(0xFF1D2024),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00C6FF) else Color(0xFF2D3139),
                            RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedCategoryFilter = category }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Smart Recruitment Portal Entrance for New Staff Translators / Cleaners
        RecruitmentPortalCard(viewModel = viewModel)

        // Top Banner slider (Featured hero titles dynamic carousel selected by Super Admin)
        val featuredIds by viewModel.featuredMangaIds.collectAsState()
        val featuredMangas = mangas.filter { featuredIds.contains(it.id) }

        if (featuredMangas.isNotEmpty() && searchQuery.isEmpty()) {
            var activeSliderIndex by remember { mutableStateOf(0) }
            // Safe index check
            val indexToUse = if (activeSliderIndex >= featuredMangas.size) 0 else activeSliderIndex
            val featuredManga = featuredMangas[indexToUse]

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive dot indicators
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    featuredMangas.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (idx == indexToUse) Color(0xFFFFD700) else Color.Gray)
                                .clickable { activeSliderIndex = idx }
                        )
                    }
                }

                Text(
                    "داغ‌ترین آثار در اسلایدر ویژه",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF2D3139), RoundedCornerShape(16.dp))
                    .clickable { viewModel.selectManga(featuredManga) }
                    .testTag("hero_manga_slider")
            ) {
                AsyncImage(
                    model = featuredManga.bannerUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF0F1115).copy(alpha = 0.95f))
                            )
                        )
                )

                // Chevron Arrows to slide easily
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            activeSliderIndex = if (indexToUse == 0) featuredMangas.size - 1 else indexToUse - 1
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = {
                            activeSliderIndex = if (indexToUse == featuredMangas.size - 1) 0 else indexToUse + 1
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF8E2DE2), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("پیشنهاد تحریریه", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF59B259), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("امتیاز ${featuredManga.rating}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = featuredManga.titleFa,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${featuredManga.author} • ${featuredManga.genres}",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manga catalog grid section
        Text(
            "کاتالوگ آثار بروز شده",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "اثری یافت نشد! عنوان دیگری را امتحان کنید.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Smooth custom vertical rows
            filteredList.forEach { manga ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .background(Color(0xFF16191E), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF2D3139), RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.selectManga(manga) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Metadata details
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE53935), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(manga.type, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = manga.status,
                                    color = if (manga.status == "در حال انتشار") Color(0xFF59B259) else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = manga.titleFa,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "نویسنده: ${manga.author}",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "تیم ترجمه: ${manga.translatorTeam}",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Right
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Text(manga.rating.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF003366), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${manga.chaptersCount} فصل", color = Color(0xFFA8C7FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Poster image
                        AsyncImage(
                            model = manga.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 80.dp, height = 110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF2D3139), RoundedCornerShape(10.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksDashboard(viewModel: MovieViewModel) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val mangas by viewModel.mangas.collectAsState()

    val bookmarkedMangas = remember(bookmarks, mangas) {
        val favoritedIds = bookmarks.map { it.mangaId }
        mangas.filter { favoritedIds.contains(it.id) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            "لیست نشان‌شده‌های شما",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (bookmarkedMangas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BorderColor, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "هنوز هیچ مانهوایی را نشان نکرده‌اید!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    bookmarkedMangas.forEach { manga ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { viewModel.selectManga(manga) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
                            border = BorderStroke(1.dp, Color(0xFF2D3139)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleBookmark(manga.id, false) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF5252))
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(manga.titleFa, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("وضعیت: ${manga.status}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    AsyncImage(
                                        model = manga.coverUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollaboratorsDashboard(viewModel: MovieViewModel) {
    val teamMembers by viewModel.teamMembers.collectAsState()
    val currentUser by viewModel.currentUserAccount.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Team Directory, 1 = My Workspace

    var showAddForm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputRole by remember { mutableStateOf("مترجم") } // "مترجم", "تایپیست", "کلینر", "ادیتور"
    var inputWorks by remember { mutableStateOf("") }
    var inputLevel by remember { mutableStateOf(3) } // 1 = Super Admin, 2= Editor, 3= normal member

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Toggle tabs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("میز کار من", "لیست اعضا").forEachIndexed { index, label ->
                val realIndex = 1 - index // Persian RTL Mapping
                val isSelected = activeSubTab == realIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF0055B3) else Color(0xFF16191E))
                        .clickable { activeSubTab = realIndex }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeSubTab == 1) {
            // My Workspace for Staff Members
            currentUser?.let { user ->
                if (user.role == "SUPER_ADMIN" || user.role == "STAFF" || user.role == "DEPT_ADMIN") {
                    Box(modifier = Modifier.fillMaxSize()) {
                        StaffDashboard(viewModel = viewModel)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "قفل",
                            tint = Color.Gray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "بخش همکاران قفل است",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "شما با موفقیت به عنوان خواننده (مهمان یا عادی) احراز هویت شده‌اید. برای تست هوشمند پنل ثبت گزارش و واگذاری جوایز، لطفا یکی از کارهای زیر را انجام دهید:\n\n۱. رول فعال خود را از تنظیمات به کادر ترجمه تغییر دهید.\n۲. از پورتال استخدام ثبت‌نام فرستاده و منتظر قبولی مدیریت بمانید.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            // Team list directory (Original screen preserved but visualised elegantly)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add button only for managers/admins
                val userRole = currentUser?.role ?: ""
                if (userRole == "SUPER_ADMIN" || userRole == "DEPT_ADMIN") {
                    Button(
                        onClick = { showAddForm = !showAddForm },
                        colors = ButtonDefaults.buttonColors(containerColor = if (showAddForm) Color(0xFFFF5252) else Color(0xFF0055B3)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (showAddForm) "بستن فرم" else "+ همکار جدید", fontSize = 11.sp, color = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    "پنل هماهنگی همکاران تیم",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // New member addition form
            if (showAddForm) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
                    border = BorderStroke(1.dp, Color(0xFF2D3139)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("ثبت عضو جدید در کادر ترجمه", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("نام همکار") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputRole,
                            onValueChange = { inputRole = it },
                            label = { Text("سمت (مثال: مترجم، کلینر، تایپیست)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputWorks,
                            onValueChange = { inputWorks = it },
                            label = { Text("مانهواهای تحت تخصیص") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (inputName.isNotEmpty()) {
                                    viewModel.addNewTeamMember(inputName, inputRole, inputWorks, inputLevel)
                                    inputName = ""
                                    inputWorks = ""
                                    showAddForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("افزودن همکار به کادر تیم", color = Color.White)
                        }
                    }
                }
            }

            // Help text
            Text(
                "ادمین ها و کادر ترجمه مسئول کنترل کیفیت ادیت، ترجمه و پاکسازی (Cleaning) پنل‌های مانیتور هستند.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Collaborators list view
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    teamMembers.forEach { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
                            border = BorderStroke(1.dp, Color(0xFF2D3139)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left actions: Delete or permissions
                                val userRole = currentUser?.role ?: ""
                                if (member.levelCode > 1 && (userRole == "SUPER_ADMIN" || userRole == "DEPT_ADMIN")) {
                                    IconButton(onClick = { viewModel.deleteMember(member) }) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "حذف همکار", tint = Color(0xFFFF5252))
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF003366), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("دسترسی کل", color = Color(0xFFA8C7FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Right details RTL
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (member.roleFa.contains("مترجم")) Color(0xFF0072FF)
                                                        else if (member.roleFa.contains("کلینر")) Color(0xFFE53935)
                                                        else Color(0xFF59B259),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(member.roleFa, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Text(member.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "پروژه‌ها: ${member.assignedWorks}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Right
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(Color(0xFF23262B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreDashboard(viewModel: MovieViewModel) {
    val isVipActive by viewModel.isVipActive.collectAsState()
    val myketHelper = viewModel.myketHelper
    val accentGold = Color(0xFFFFD700)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            "اشتراک ویژه Vip مانگاتا",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            "دسترسی سریع به فصول پیش‌نویس همراه با بهترین ادیت و ترجمه‌ی مستقیم از سورس کره‌ای مایکت.",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Subscription Status banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isVipActive) Color(0xFF1E3A1E) else Color(0xFF23262B),
                    RoundedCornerShape(16.dp)
                )
                .border(2.dp, if (isVipActive) Color(0xFF59B259) else Color(0xFF2D3139), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isVipActive) {
                    Button(
                        onClick = { viewModel.cancelVipSubscription() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("لغو اشتراک VIP", fontSize = 10.sp, color = Color.White)
                    }
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = accentGold, modifier = Modifier.size(24.dp))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isVipActive) "اشتراک VIP شما فعال است" else "شما کاربر عادی هستید",
                        color = if (isVipActive) Color(0xFF81C784) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isVipActive) "دسترسی بلامانع به کلیه آرشیو آثار" else "ارتقا با کلیه درگاه‌های مایکت بازار",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "طرح‌های ارتقای عضویت مایکت",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Render Sku details
        listOf(myketHelper.SKU_VIP_1MONTH, myketHelper.SKU_VIP_3MONTH, myketHelper.SKU_VIP_LIFETIME).forEach { sku ->
            val details = myketHelper.skuDetailsMap[sku]
            if (details != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, Color(0xFF2D3139), RoundedCornerShape(14.dp))
                        .clickable { viewModel.triggerMyketPurchase(sku) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Price badge button
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF003366), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = details.priceFa,
                                color = Color(0xFFA8C7FA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Right Sku titles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = details.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = details.description,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = accentGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDashboard(viewModel: MovieViewModel) {
    val siteDomain by viewModel.siteDomain.collectAsState()
    val isDomainConnected by viewModel.isDomainConnected.collectAsState()

    var inputDomain by remember { mutableStateOf(siteDomain) }
    var showPingState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            "تنظیمات اتصال به دامنه سایت",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            "آدرس هاست یا دامنه سایت وردپرسی یا کاستوم خود را وارد نمایید تا اپلیکیشن مانهواها را به صورت پویا از سایت واقعی شما لود کند.",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
            border = BorderStroke(1.dp, Color(0xFF2D3139)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "تنظیم آدرس اتصال API",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputDomain,
                    onValueChange = { inputDomain = it },
                    placeholder = { Text("https://myketmanga.com/wp-json") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showPingState) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isDomainConnected) "متصل به دامنه" else "در حال راستی‌آزمایی...",
                                color = if (isDomainConnected) Color(0xFF59B259) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = if (isDomainConnected) Color(0xFF59B259) else Color.White
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.updateSiteDomain(inputDomain)
                            showPingState = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055B3)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("اعمال و بررسی پینگ", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Technical Guide for zero programmer setup
        Text(
            "راهنمای راه‌اندازی هاست برای صاحب امتیاز:",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "۱. یک سایت با وردپرس (WordPress) روی هاست خود بسازید.\n" +
                    "۲. پوسته رایگان Madara یا wp-manga را نصب کنید.\n" +
                    "۳. افزونه‌های REST API پیشفرض را فعال بگذارید.\n" +
                    "۴. دامنه خود را در کادر بالا وارد کنید تا مانهواهای شما مستقیما خوانده شوند!",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Right
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Simulated Account Switcher for Evaluators to verify scenarios
        Text(
            "شبیه‌ساز هویت و رول کاربر (محیط آزمون دمو)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            "بین رول‌های زیر جابجا شده و تغییر درهای بسته‌شده، پنل همکاران، تقسیم درآمدها و قفل‌های چپترها را بلادرنگ امتحان کنید:",
            color = Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val userAccounts by viewModel.userAccounts.collectAsState()
        val currentUserAccount by viewModel.currentUserAccount.collectAsState()

        userAccounts.forEach { account ->
            val isSelected = currentUserAccount?.id == account.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.switchUser(account.id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF132235) else Color(0xFF16191E)
                ),
                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00C6FF) else Color(0xFF2D3139)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF59B259), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کاربر فعال شما", color = Color(0xFF59B259), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("کلیک جهت فعال‌سازی رول", color = Color.Gray, fontSize = 9.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(account.displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("سمت: ${account.subRole}", color = Color.LightGray, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("اعتبار: ${account.walletRial} تومان", color = Color(0xFFFFD700), fontSize = 9.sp)
                            Text("کوپن: ${account.walletGiftChapters} عدد", color = Color(0xFF00C6FF), fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        currentUserAccount?.let { user ->
            if (user.role == "SUPER_ADMIN" || user.role == "DEPT_ADMIN") {
                Spacer(modifier = Modifier.height(24.dp))
                AdminPanel(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MangaDetailOverlay(
    manga: MangaEntity,
    viewModel: MovieViewModel,
    isVipActive: Boolean,
    onClose: () -> Unit
) {
    val isBookmarked by viewModel.isBookmarked(manga.id).collectAsState(false)
    val readHistory by viewModel.getHistoryForManga(manga.id).collectAsState(null)
    val aiSummary by viewModel.aiSummaryState.collectAsState()
    val startsFromZeroMap by viewModel.mangaStartsFromZero.collectAsState()
    val startsFromZero = startsFromZeroMap[manga.id] ?: false

    val currentUser by viewModel.currentUserAccount.collectAsState()
    val purchasedList by if (currentUser != null) {
        viewModel.getPurchasedChapters(currentUser!!.id).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    var unlockDialogChapterNumber by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .verticalScroll(rememberScrollState())
    ) {
        // Banner Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            AsyncImage(
                model = manga.bannerUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF0F1115))
                        )
                    )
            )

            // Close card button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
            }
        }

        // Details content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorites Bookmark Button
                Box(
                    modifier = Modifier
                        .background(
                            if (isBookmarked) Color(0xFFFF4081).copy(alpha = 0.15f) else Color(0xFF1D2024),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { viewModel.toggleBookmark(manga.id, !isBookmarked) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مارک شده",
                        tint = if (isBookmarked) Color(0xFFFF4081) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = manga.titleFa,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = manga.titleEn,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Genre Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
            ) {
                manga.genres.split(",").forEach { genre ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1D2024), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(genre.trim(), color = Color.LightGray, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Last Reading History position tracker
            if (readHistory != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF003366).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF003366), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.openReader(manga, readHistory!!.currentChapter) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ادامه خواندن", fontSize = 10.sp, color = Color.White)
                        }

                        Text(
                            text = "اخرین بار: فصل ${readHistory!!.currentChapter} را خوانده‌اید",
                            color = Color(0xFFA8C7FA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                "خلاصه داستان مانهوا",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = manga.descriptionFa,
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Summarize storyline using Gemini artificial intelligence
            GeminiReviewSummary(state = aiSummary, modifier = Modifier.padding(bottom = 20.dp))

            Text(
                "فهرست فصول آماده‌ی خوانده شدن (${manga.chaptersCount} فصل)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Chapter items rendering
            // Chapters 1 to 3 are FREE. Chapters > 3 are locked VIP early release unless purchased
            val startCh = if (startsFromZero) 0 else 1
            val endCh = if (startsFromZero) 7 else 8
            for (ch in startCh..endCh) {
                val isPremiumChapter = ch > 3
                val isPurchased = purchasedList.any { it.mangaId == manga.id && it.chapterNumber == ch }
                val isUnlocked = !isPremiumChapter || isVipActive || isPurchased

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (isUnlocked) {
                                viewModel.openReader(manga, ch)
                            } else {
                                unlockDialogChapterNumber = ch
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color(0xFF1D2024) else Color(0xFF16191E)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2D3139)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUnlocked) {
                            if (isPurchased) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("خریداری شده", color = Color(0xFF59B259), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.LockOpen, contentDescription = "آزاد شده", tint = Color(0xFF59B259), modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.LightGray)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("خرید تک چپتر / VIP مایکت", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Lock, contentDescription = "قفل", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            }
                        }

                        Text(
                            "فصل $ch: آرک رستاخیز سایه ها",
                            color = if (isUnlocked) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            unlockDialogChapterNumber?.let { chapNum ->
                ChapterUnlockDialog(
                    mangaId = manga.id,
                    chapterNumber = chapNum,
                    viewModel = viewModel,
                    onDismiss = { unlockDialogChapterNumber = null },
                    onUnlockSuccess = {
                        viewModel.openReader(manga, chapNum)
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
