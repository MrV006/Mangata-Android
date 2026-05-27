package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import com.example.ui.components.MangaDetailView
import com.example.ui.components.RecruitmentPortal
import com.example.ui.components.StaffDashboard
import com.example.ui.theme.MangataTheme
import com.example.ui.theme.SlateDarkBackground
import androidx.compose.ui.text.style.TextOverflow

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

                val isForceUpdateRequired by viewModel.isForceUpdateRequired.collectAsState()
                val appSettings by viewModel.appSettings.collectAsState()

                // Enforced Update Blocking Modal
                if (isForceUpdateRequired && currentUser?.role != "administrator") {
                    AlertDialog(
                        onDismissRequest = { /* Force update cannot be dismissed */ },
                        title = {
                            Text(
                                text = "🔄 بروزرسانی اجباری برنامه",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                text = appSettings?.forceUpdateAppMsg ?: "نسخه جدید و حیاتی اپلیکیشن آماده دریافت است. لطفا جهت استفاده مجدد از امکانات برنامه آن را بروزرسانی کنید.",
                                fontSize = 14.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        },
                        confirmButton = {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Button(
                                onClick = {
                                    val url = appSettings?.forceUpdateAppUrl ?: "https://mr-v.ir/"
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "خطا در باز کردن مرورگر جهت دانلود.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("دانلود و نصب نسخه جدید 📥", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = null,
                        containerColor = Color(0xFF1E1B24),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // State Navigation
                var currentScreen by remember { mutableStateOf("home") } // "home", "recruitment", "staff", "admin"
                var selectedChapterForReader by remember { mutableStateOf<ChapterItem?>(null) }
                var selectedMangaForReader by remember { mutableStateOf<MangaItem?>(null) }
                var selectedMangaForDetails by remember { mutableStateOf<MangaItem?>(null) }

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
                                chaptersList = chapters[readerManga.id] ?: emptyList(),
                                onChapterChanged = { newChapter ->
                                    selectedChapterForReader = newChapter
                                },
                                onCloseReader = {
                                    selectedChapterForReader = null
                                    selectedMangaForReader = null
                                }
                            )
                        } else if (selectedMangaForDetails != null) {
                            MangaDetailView(
                                manga = selectedMangaForDetails!!,
                                chapters = chapters[selectedMangaForDetails!!.id] ?: emptyList(),
                                userRole = user.role,
                                onBack = { selectedMangaForDetails = null },
                                onReadChapter = { chapter ->
                                    selectedMangaForReader = selectedMangaForDetails
                                    selectedChapterForReader = chapter
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
                                        chaptersMap = chapters,
                                        isLoading = isLoading,
                                        onNavigateToRecruitment = { currentScreen = "recruitment" },
                                        onNavigateToStaff = { currentScreen = "staff" },
                                        onNavigateToAdmin = { currentScreen = "admin" },
                                        onSelectManga = { manga ->
                                            selectedMangaForDetails = manga
                                            viewModel.fetchChapters(manga.id)
                                        },
                                        onQuickReadChapter = { manga, chapter ->
                                            selectedMangaForReader = manga
                                            selectedChapterForReader = chapter
                                        },
                                        onLogout = { viewModel.logout() },
                                        onTriggerCacheClear = { viewModel.clearAppCache() },
                                        onSearch = { search, genre, year, character ->
                                            viewModel.fetchManhwas(
                                                search = search.takeIf { !it.isNullOrBlank() },
                                                genre = genre.takeIf { !it.isNullOrBlank() },
                                                year = year.takeIf { !it.isNullOrBlank() },
                                                character = character.takeIf { !it.isNullOrBlank() }
                                            )
                                        }
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

// 1. Standalone Centralized Auth Screen
@Composable
fun AuthScreen(
    isLoading: Boolean,
    onLoginSubmit: (String, String) -> Unit,
    onRegisterSubmit: (String, String, String, String) -> Unit
) {
    var isSignUpTab by remember { mutableStateOf(false) }

    // Forms state
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("subscriber") } // subscriber, contributor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C1B), // Midnight Velvet
                        Color(0xFF1E1435), // Royal Purple Dark
                        Color(0xFF08070F)  // Absolute Black
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header / App Logo Vibe
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Mangata Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MANGATA • مانگاتا",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )

                Text(
                    text = "پایگاه جامع، زنده و تیمی مانهواخوان",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Tab Switcher with Sleek Pill Animation style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isSignUpTab) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isSignUpTab = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ورود واقعی",
                        color = if (!isSignUpTab) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSignUpTab) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isSignUpTab = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ثبت‌نام جدید",
                        color = if (isSignUpTab) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Form container glassmorphic card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isSignUpTab) "کارت عضویت مانهواخواهان" else "احراز هویت دیتابیس هوشمند",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // 1. Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("نام کاربری (انگلیسی)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "user icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. Email (only on SignUp)
                    AnimatedVisibility(visible = isSignUpTab) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("آدرس ایمیل") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "email icon",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "علاقمند به فعالیت در کادر ترجمه؟",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (selectedRole == "subscriber") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color.White.copy(alpha = 0.04f)
                                        )
                                        .clickable { selectedRole = "subscriber" }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (selectedRole == "subscriber") Icons.Default.CheckCircle else Icons.Default.FavoriteBorder,
                                            contentDescription = "subscriber choice",
                                            tint = if (selectedRole == "subscriber") MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "مخاطب عادی",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedRole == "subscriber") Color.White else Color.Gray
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (selectedRole == "contributor") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else Color.White.copy(alpha = 0.04f)
                                        )
                                        .clickable { selectedRole = "contributor" }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (selectedRole == "contributor") Icons.Default.CheckCircle else Icons.Default.Star,
                                            contentDescription = "contributor choice",
                                            tint = if (selectedRole == "contributor") MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "مترجم و طراح",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedRole == "contributor") Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور دیتابیس") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "lock icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                    contentDescription = "Peek password",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (isSignUpTab) {
                                onRegisterSubmit(username, email, password, selectedRole)
                            } else {
                                onLoginSubmit(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.Black
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUpTab) "عضویت در پورتال مرکزی" else "ورود یکپارچه به دنیای مانهوا",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodyMedium
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
    chaptersMap: Map<Int, List<ChapterItem>>,
    isLoading: Boolean,
    onNavigateToRecruitment: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSelectManga: (MangaItem) -> Unit,
    onQuickReadChapter: (MangaItem, ChapterItem) -> Unit,
    onLogout: () -> Unit,
    onTriggerCacheClear: () -> Unit,
    onSearch: (String?, String?, String?, String?) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var selectedCharacter by remember { mutableStateOf("") }
    var isFilterExpanded by remember { mutableStateOf(false) }

    val genres = listOf("اکشن", "کمدی", "درام", "فانتزی", "ماجراجویی", "عاشقانه")
    var isGenreDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0A0F))
                    .border(BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.15f)))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "MANGATA | مانگاتا 🎨",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color(0xFFBB86FC)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFF03DAC6).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF03DAC6).copy(alpha = 0.3f), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Premium 👑", color = Color(0xFF03DAC6), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "کاربر فعال مانهواخوان: $username (${translateRole(userRole)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.Red.copy(alpha = 0.1f))
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red, modifier = Modifier.size(20.dp))
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14101A)),
                    border = BorderStroke(1.dp, Color(0xFFFF7597).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Cmd", tint = Color(0xFFFF7597), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "🎮 مرکز تسک‌ها و مأموریت‌های مانگاتا",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF7597)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFFFF7597).copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("پورتال هماهنگ ⚡", color = Color(0xFFFF7597), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Try recruitment button (open to everyone)
                            Button(
                                onClick = onNavigateToRecruitment,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7597).copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, Color(0xFFFF7597).copy(alpha = 0.4f))
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Job", tint = Color(0xFFFF7597), modifier = Modifier.size(14.dp))
                                    Text("آزمون استخدام 📝", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // ZIP upload button (for crew & admin)
                            val isCrewOrAdmin = userRole == "administrator" || userRole == "contributor" || userRole == "author" || userRole == "editor"
                            if (isCrewOrAdmin) {
                                Button(
                                    onClick = onNavigateToStaff,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6).copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, Color(0xFF03DAC6).copy(alpha = 0.4f))
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "ZIP", tint = Color(0xFF03DAC6), modifier = Modifier.size(14.dp))
                                        Text("آپلود ZIP چپتر 📤", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Super Admin Control Panel
                            if (userRole == "administrator") {
                                Button(
                                    onClick = onNavigateToAdmin,
                                    modifier = Modifier.weight(1.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.8f))
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Admin", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("مدیریت کل ⚙️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 👤 USER PROFILE & CACHE SYNC CARD
            item {
                var isProfileExpanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
                    border = BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isProfileExpanded = !isProfileExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = "Profile",
                                    tint = Color(0xFFBB86FC),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "👤 پروفایل کاربری من و بهینه سازی کش",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = if (isProfileExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = Color.Gray
                            )
                        }

                        if (isProfileExpanded) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "نام کاربری شما: $username",
                                fontSize = 13.sp,
                                color = Color.LightGray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "سطح دسترسی شما در پلتفرم: ${translateRole(userRole)}",
                                fontSize = 13.sp,
                                color = Color.LightGray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = onTriggerCacheClear,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("button_update_app_cache"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6))
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "بروزرسانی برنامه و بازنشانی کامل حافظه کش 🔄",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Advanced Search & Filter Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🔍 جستجوی پیشرفته مانهوا",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Search Text Field
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("جستجو کنید (عنوان، خلاصه اثر، نویسنده...)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Filters expand toggle button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { isFilterExpanded = !isFilterExpanded }) {
                                Text(
                                    text = if (isFilterExpanded) "◀ بستن ابزارهای فیلتر" else "▼ نمایش ابزارهای فیلتر تخصصی",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Collapsible Filter Panel
                        AnimatedVisibility(visible = isFilterExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Genre Dropdown
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { isGenreDropdownExpanded = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (selectedGenre.isEmpty()) "📂 انتخاب ژانر" else "ژانر: $selectedGenre",
                                            fontSize = 12.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = isGenreDropdownExpanded,
                                        onDismissRequest = { isGenreDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("همه ژانرها") },
                                            onClick = {
                                                selectedGenre = ""
                                                isGenreDropdownExpanded = false
                                            }
                                        )
                                        genres.forEach { g ->
                                            DropdownMenuItem(
                                                text = { Text(g) },
                                                onClick = {
                                                    selectedGenre = g
                                                    isGenreDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Year input
                                    OutlinedTextField(
                                        value = selectedYear,
                                        onValueChange = { selectedYear = it },
                                        placeholder = { Text("سال انتشار (مثال: 2024)", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Author/Character input
                                    OutlinedTextField(
                                        value = selectedCharacter,
                                        onValueChange = { selectedCharacter = it },
                                        placeholder = { Text("شخصیت اصلی", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1.1f)
                                    )
                                }
                            }
                        }

                        // Search and Clear buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSearch(
                                        searchText.takeIf { it.isNotEmpty() },
                                        selectedGenre.takeIf { it.isNotEmpty() },
                                        selectedYear.takeIf { it.isNotEmpty() },
                                        selectedCharacter.takeIf { it.isNotEmpty() }
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("اعمال جستجو 🚀", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (searchText.isNotEmpty() || selectedGenre.isNotEmpty() || selectedYear.isNotEmpty() || selectedCharacter.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        searchText = ""
                                        selectedGenre = ""
                                        selectedYear = ""
                                        selectedCharacter = ""
                                        onSearch(null, null, null, null)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f))
                                ) {
                                    Text("پاک کردن 🧹", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 🏆 PRE-LOAD FEATURED WEEKLY SLIDE (Connected to live database)
            val featuredManga = mangas.firstOrNull()
            if (featuredManga != null) {
                item {
                    Text(
                        text = "🔥 مانهوای برگزیده هفته (پورتال مانگاتا)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF03DAC6),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { onSelectManga(featuredManga) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
                        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF03DAC6), Color(0xFFBB86FC))))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val coverUrl = featuredManga.coverImage
                            if (!coverUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.35f
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                if (!coverUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = coverUrl,
                                        contentDescription = featuredManga.title,
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(Color(0xFF03DAC6))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("🏆 رتبه اول کاربری", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Text(
                                        text = featuredManga.title,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    
                                    Text(
                                        text = featuredManga.description,
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 🏷️ LIVE CHIPS CATEGORY ROW
            item {
                Text(
                    text = "🏷️ دسته‌بندی مانهواها براساس بیشترین لایک",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickGenres = listOf("همه", "اکشن", "کمدی", "درام", "فانتزی", "ماجراجویی", "عاشقانه")
                    items(quickGenres) { gen ->
                        val isSelected = (gen == "همه" && selectedGenre.isEmpty()) || (gen == selectedGenre)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(if (isSelected) Color(0xFFBB86FC) else Color(0xFF1E1B24))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFBB86FC) else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(30.dp)
                                )
                                .clickable {
                                    selectedGenre = if (gen == "همه") "" else gen
                                    onSearch(
                                        searchText.takeIf { it.isNotEmpty() },
                                        selectedGenre.takeIf { it.isNotEmpty() },
                                        selectedYear.takeIf { it.isNotEmpty() },
                                        selectedCharacter.takeIf { it.isNotEmpty() }
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = gen,
                                color = if (isSelected) Color.Black else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "لیست آثار مانهوا هوشمند",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                        chapters = chaptersMap[manga.id] ?: emptyList(),
                        onClick = { onSelectManga(manga) },
                        onQuickReadChapter = { ch -> onQuickReadChapter(manga, ch) }
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
    onClick: () -> Unit,
    onQuickReadChapter: (ChapterItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14121A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFBB86FC).copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Cover Image with rating badge
                val coverUrl = manga.coverImage
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 130.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    if (!coverUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = manga.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF231F2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Cover", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                    
                    // Rating tag floating overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("⭐ ۹.۸", color = Color(0xFFFFD700), fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Title + metadata column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = manga.title, 
                            fontWeight = FontWeight.ExtraBold, 
                            style = MaterialTheme.typography.titleMedium, 
                            color = Color.White
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF7597).copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, Color(0xFFFF7597).copy(alpha = 0.3f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("رایگان 🔓", color = Color(0xFFFF7597), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = manga.description, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.LightGray, 
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val genresList = manga.genres?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
                        genresList.take(2).forEach { g ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFFBB86FC).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = g, color = Color(0xFFBB86FC), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(text = "برند: مانگاتا", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }

            // Quick chapters reading section! (If there are any chapters loaded)
            if (chapters.isNotEmpty()) {
                Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🚀 خوانش سریع چپترهای اخیر پلتفرم:",
                        color = Color(0xFF03DAC6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chapters.sortedByDescending { it.chapterNumber }.take(2).forEach { ch ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onQuickReadChapter(ch) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B24)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, Color(0xFF03DAC6).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "چپتر " + if (ch.chapterNumber % 1.0 == 0.0) ch.chapterNumber.toInt() else ch.chapterNumber,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "ریدر 👁️",
                                        color = Color(0xFF03DAC6),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
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

private fun translateRole(role: String): String {
    return when (role) {
        "administrator" -> "مدیریت کل"
        "contributor", "author", "editor" -> "دستاندرکار مجاز"
        else -> "خواننده مانهوا"
    }
}
