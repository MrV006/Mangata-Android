package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChapterItem
import com.example.data.MangaItem
import com.example.ui.MangaViewModel
import com.example.ui.components.AdminPanel
import com.example.ui.components.MangaReaderView
import com.example.ui.components.RecruitmentPortal
import com.example.ui.components.StaffDashboard
import com.example.ui.theme.MangataTheme
import com.example.ui.theme.SlateDarkBackground

class MainActivity : ComponentActivity() {

    private val viewModel: MangaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangataTheme {
                val currentUser by viewModel.currentUser.collectAsState()
                val mangas by viewModel.mangas.collectAsState()
                val chapters by viewModel.chapters.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                
                val errorMessage by viewModel.errorMessage.collectAsState()
                val successMessage by viewModel.successMessage.collectAsState()

                // State Navigation
                var currentScreen by remember { mutableStateOf("home") } // "home", "recruitment", "staff", "admin"
                var selectedChapterForReader by remember { mutableStateOf<ChapterItem?>(null) }
                var selectedMangaForReader by remember { mutableStateOf<MangaItem?>(null) }

                // Display alerts
                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                        viewModel.clearMessages()
                    }
                }
                LaunchedEffect(successMessage) {
                    successMessage?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                        viewModel.clearMessages()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SlateDarkBackground
                ) {
                    if (currentUser == null) {
                        // Show real WordPress auth screen
                        AuthScreen(
                            isLoading = isLoading,
                            onLoginSubmit = { user, pass -> viewModel.login(user, pass) },
                            onRegisterSubmit = { user, email, pass, role -> viewModel.register(user, email, pass, role) }
                        )
                    } else {
                        val user = currentUser!!
                        
                        // Active e-Reader overlay
                        val readerCap = selectedChapterForReader
                        val readerManga = selectedMangaForReader
                        if (readerCap != null && readerManga != null) {
                            MangaReaderView(
                                chapter = readerCap,
                                mangaTitle = readerManga.title,
                                onCloseReader = {
                                    selectedChapterForReader = null
                                    selectedMangaForReader = null
                                }
                            )
                        } else {
                            // Sub-screens routing
                            when (currentScreen) {
                                "recruitment" -> {
                                    RecruitmentPortal(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = "home" }
                                    )
                                }
                                "staff" -> {
                                    StaffDashboard(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = "home" }
                                    )
                                }
                                "admin" -> {
                                    AdminPanel(
                                        viewModel = viewModel,
                                        onBack = { currentScreen = "home" }
                                    )
                                }
                                else -> {
                                    // Primary HOME dashboard
                                    HomeScreenContent(
                                        userRole = user.role,
                                        username = user.username,
                                        mangas = mangas,
                                        chapters = chapters,
                                        isLoading = isLoading,
                                        onNavigateToRecruitment = { currentScreen = "recruitment" },
                                        onNavigateToStaff = { currentScreen = "staff" },
                                        onNavigateToAdmin = { currentScreen = "admin" },
                                        onLoadChapters = { viewModel.fetchChapters(it) },
                                        onReadChapter = { m, c ->
                                            selectedMangaForReader = m
                                            selectedChapterForReader = c
                                        },
                                        onLogout = { viewModel.logout() }
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

// 1. WordPress Auth Composables
@Composable
fun AuthScreen(
    isLoading: Boolean,
    onLoginSubmit: (String, String) -> Unit,
    onRegisterSubmit: (String, String, String, String) -> Unit
) {
    var isSignUpTab by remember { mutableStateOf(false) }

    // Forms
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("subscriber") } // subscriber, author etc.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1F1A3A), Color(0xFF121214))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo / Branding
            Text(
                text = "MANGATA • مانگاتا",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "پایگاه مانهواهای اختصاصی و تیمی",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab toggler
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { isSignUpTab = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSignUpTab) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("ورود با وردپرس", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isSignUpTab = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUpTab) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("ثبت نام جدید", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fields
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("نام کاربری (حروف انگلیسی)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (isSignUpTab) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("آدرس ایمیل") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Role Option
                        Text("علاقمند به عضویت در کادر:", style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedRole == "subscriber", onClick = { selectedRole = "subscriber" })
                                Text("مخاطب عادی", fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedRole == "contributor", onClick = { selectedRole = "contributor" })
                                Text("مترجم/طراح", fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور دیتابیس سایت") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (isSignUpTab) {
                                onRegisterSubmit(username, email, password, selectedRole)
                            } else {
                                onLoginSubmit(username, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text(
                                text = if (isSignUpTab) "عضویت در پورتال مرکزی" else "ورود واقعی و همگام به سایت",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 2. Dashboards Composables
@Composable
fun HomeScreenContent(
    userRole: String,
    username: String,
    mangas: List<MangaItem>,
    chapters: Map<Int, List<ChapterItem>>,
    isLoading: Boolean,
    onNavigateToRecruitment: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLoadChapters: (Int) -> Unit,
    onReadChapter: (MangaItem, ChapterItem) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مانگاهلوگ مانگاتا",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "حساب کاربر: $username (${translateRole(userRole)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = onLogout) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick navigations based on Roles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Try recruitment button (open to everyone)
                    Button(
                        onClick = onNavigateToRecruitment,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Job", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text("آزمون استخدام", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    // ZIP upload button (for crew & admin)
                    val isCrewOrAdmin = userRole == "administrator" || userRole == "contributor" || userRole == "author" || userRole == "editor"
                    if (isCrewOrAdmin) {
                        Button(
                            onClick = onNavigateToStaff,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "ZIP", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text("آپلود ZIP چپتر", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Super Admin Control Panel
                    if (userRole == "administrator") {
                        Button(
                            onClick = onNavigateToAdmin,
                            modifier = Modifier.weight(1.2f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Admin", tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("تصحیح آزمون‌ها", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "لیست آثار مانهوا هوشمند",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (isLoading && mangas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (mangas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("هیچ به اثری در دیتابیس ثبت نشده است.", color = Color.Gray)
                    }
                }
            } else {
                items(mangas) { manga ->
                    MangaItemCard(
                        manga = manga,
                        chapters = chapters[manga.id] ?: emptyList(),
                        onExpandChapters = { onLoadChapters(manga.id) },
                        onReadChapter = { onReadChapter(manga, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MangaItemCard(
    manga: MangaItem,
    chapters: List<ChapterItem>,
    onExpandChapters: () -> Unit,
    onReadChapter: (ChapterItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cover
                val coverUrl = manga.coverImage
                if (!coverUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = manga.title,
                        modifier = Modifier
                            .size(width = 80.dp, height = 110.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = manga.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(text = manga.description, style = MaterialTheme.typography.labelMedium, color = Color.LightGray, maxLines = 3)
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = {
                            expanded = !expanded
                            if (expanded && chapters.isEmpty()) {
                                onExpandChapters()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = if (expanded) "پنهان کردن چپترها ▲" else "نمایش چپترهای ZIP آپلود شده ▼",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chapters.isEmpty()) {
                        Text("هیچ فصلی برای نمایش وجود تدارد.", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    } else {
                        chapters.forEach { chapter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "چپتر ${chapter.chapterNumber}", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = chapter.title, style = MaterialTheme.typography.labelMedium, color = Color.LightGray)
                                }

                                Button(
                                    onClick = { onReadChapter(chapter) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(30.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("شروع خواندن", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun translateRole(role: String): String {
    return when (role) {
        "administrator" -> "مدیریت کل"
        "contributor", "author", "editor" -> "دستاندرکار مجاز"
        else -> "خواننده مانهوا"
    }
}
