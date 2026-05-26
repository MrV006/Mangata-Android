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
                                        isLoading = isLoading,
                                        onNavigateToRecruitment = { currentScreen = "recruitment" },
                                        onNavigateToStaff = { currentScreen = "staff" },
                                        onNavigateToAdmin = { currentScreen = "admin" },
                                        onSelectManga = { manga ->
                                            selectedMangaForDetails = manga
                                            viewModel.fetchChapters(manga.id)
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
    isLoading: Boolean,
    onNavigateToRecruitment: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSelectManga: (MangaItem) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141218))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7597).copy(alpha = 0.2f))
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Job", tint = Color(0xFFFF7597), modifier = Modifier.size(16.dp))
                            Text("آزمون استخدام", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    // ZIP upload button (for crew & admin)
                    val isCrewOrAdmin = userRole == "administrator" || userRole == "contributor" || userRole == "author" || userRole == "editor"
                    if (isCrewOrAdmin) {
                        Button(
                            onClick = onNavigateToStaff,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                                Text("پنل ادمین کل", color = Color.White, fontSize = 11.sp)
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
                        onClick = { onSelectManga(manga) }
                    )
                }
            }
        }
    }
}

@Composable
fun MangaItemCard(
    manga: MangaItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16141F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover
            val coverUrl = manga.coverImage
            if (!coverUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = manga.title,
                    modifier = Modifier
                        .size(width = 85.dp, height = 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFF7597).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

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
                            .background(Color(0xFFFF7597).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("مشاهده جزئیات", color = Color(0xFFFF7597), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = manga.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.LightGray, 
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF231F2E))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("مجموعه مانگاتا", color = Color(0xFF03DAC6), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "آخرین انتشار: " + manga.createdAt.take(10), color = Color.Gray, fontSize = 9.sp)
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
