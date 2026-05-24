package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanel(
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val exams by viewModel.exams.collectAsState()
    val mangas by viewModel.mangas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Exams, 1: Add Manhwa, 2: Assign Crew

    // Add Manga state
    var newMangaTitle by remember { mutableStateOf("") }
    var newMangaDesc by remember { mutableStateOf("") }
    var newMangaCover by remember { mutableStateOf("") }

    // Assign Crew state
    var staffIdText by remember { mutableStateOf("") }
    var selectedMangaId by remember { mutableStateOf(0) }
    var staffRoleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchExams()
        viewModel.fetchManhwas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پنل فوق‌تخصصی مدیریت کل (Super Admin)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            TabRow(selectedTabIndex = activeTab) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("آزمون‌های استخدام", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("افزودن مانهوا به سایت", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("تخصیص نیرو", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTab == 0) {
                    // Exams list
                    if (exams.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("هیچ آزمون استخدامی در دیتابیس وردپرس یافت نشد.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(exams) { exam ->
                            var gradingScore by remember { mutableStateOf(exam.score?.toString() ?: "80") }
                            var gradingStatus by remember { mutableStateOf(exam.status) }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "متقاضی: ${exam.username ?: "نامعلوم"}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (exam.status == "Accepted") Color(0xFF1B5E20)
                                                    else if (exam.status == "Rejected") Color(0xFFB71C1C)
                                                    else Color(0xFFE65100)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(text = exam.status, color = Color.White, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }

                                    Text(text = "پست الکترونیکی کادر: ${exam.fileUrl}", style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "نام فایل ارسالی واقعی: ${exam.fileName}", style = MaterialTheme.typography.labelMedium)
                                    Text(
                                        text = "نمره ثبت شده فعلی: " + (exam.score ?: "هنوز نمره‌دهی نشده"),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                                    // Grading controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = gradingScore,
                                            onValueChange = { gradingScore = it },
                                            label = { Text("SCORE (0-100)") },
                                            modifier = Modifier.width(100.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { gradingStatus = "Accepted" },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (gradingStatus == "Accepted") Color(0xFF4CAF50) else Color.Gray
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("قبولی", color = Color.White)
                                                }
                                                Button(
                                                    onClick = { gradingStatus = "Rejected" },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (gradingStatus == "Rejected") Color(0xFFF44336) else Color.Gray
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("رد کردن", color = Color.White)
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val scoreInt = gradingScore.toIntOrNull() ?: 0
                                            viewModel.gradeExam(exam.id, gradingStatus, scoreInt)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("ثبت نمره و تأیید نهایی آزمون", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else if (activeTab == 1) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("افزودن اثر مانهوا جدید به کاتالوگ وبسایت و اپلیکیشن", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = newMangaTitle,
                                onValueChange = { newMangaTitle = it },
                                label = { Text("عنوان مانهوا (فارسی)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newMangaDesc,
                                onValueChange = { newMangaDesc = it },
                                label = { Text("توضیحات خلاصه داستان جنجالی") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newMangaCover,
                                onValueChange = { newMangaCover = it },
                                label = { Text("آدرس فیزیکی تصویر کاور (URL)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (newMangaTitle.isNotEmpty()) {
                                        viewModel.createManhwa(newMangaTitle, newMangaDesc, newMangaCover)
                                        newMangaTitle = ""
                                        newMangaDesc = ""
                                        newMangaCover = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("ثبت مانهوا در دیتابیس سایت", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("تخصیص عضو تیم به مانهوا خاص (مترجم / تاپ)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = staffIdText,
                                onValueChange = { staffIdText = it },
                                label = { Text("شناسه عددی کاربر متقاضی (User ID)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = staffRoleText,
                                onValueChange = { staffRoleText = it },
                                label = { Text("نقش (Translator, Redrawer, Cleaner, TS)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("مانهوای هدف را انتخاب کنید:", fontWeight = FontWeight.Bold)

                            mangas.forEach { m ->
                                val isSelected = selectedMangaId == m.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedMangaId = m.id }
                                        .padding(12.dp)
                                ) {
                                    Text(m.title, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    val sId = staffIdText.toIntOrNull()
                                    if (sId != null && selectedMangaId > 0 && staffRoleText.isNotEmpty()) {
                                        viewModel.assignStaff(sId, selectedMangaId, staffRoleText)
                                        staffIdText = ""
                                        staffRoleText = ""
                                        selectedMangaId = 0
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ثبت دسترسی عضو تیم ترجمه", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
