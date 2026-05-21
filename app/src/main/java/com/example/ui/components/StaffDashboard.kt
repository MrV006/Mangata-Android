package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MovieViewModel

@Composable
fun StaffDashboard(viewModel: MovieViewModel) {
    val currentUser by viewModel.currentUserAccount.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val userAccounts by viewModel.userAccounts.collectAsState()
    val context = LocalContext.current

    var selectedMangaToLog by remember { mutableStateOf("سولو لولینگ (تک‌رو)") }
    var chapterToLogInput by remember { mutableStateOf("") }
    var workedChaptersLoggedToast by remember { mutableStateOf(false) }

    var selectedTranslatorId by remember { mutableStateOf(5) } // نازنین راد
    var selectedCleanerId by remember { mutableStateOf(3) } // سینا زارع
    var selectedEditorId by remember { mutableStateOf(4) } // تینا مهدوی

    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    var isDraggingOver by remember { mutableStateOf(false) }

    val activeRewardCount = currentUser?.customRewardRate ?: settings.defaultStaffRewardChapters

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Staff Welcoming Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF2D3139))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF59B259), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(currentUser?.subRole ?: "مترجم", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "داشبورد همکاران: ${currentUser?.displayName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "به کادر مقتدر و پویای مانگاتا خوش آمدید. عملکرد و فعالیت‌های شما مستقیما محاسبه و به درآمد متوازن شما افزوده می‌شود.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Staff Wallets summary
        Text("امور مالی و اعتباری همکار", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2229))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("درآمد نقدی انباشته", fontSize = 11.sp, color = Color.Gray)
                    Text("${currentUser?.walletRial} تومان", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2229))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color(0xFF00C6FF))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("کوپن خوانش رایگان", fontSize = 11.sp, color = Color.Gray)
                    Text("${currentUser?.walletGiftChapters} فصل هدیه", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Stats summary for stories
        currentUser?.let { user ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1F24)),
                border = BorderStroke(1.dp, Color(0xFF2D3139))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${user.chaptersContributedThisMonth} فصل مانهوا",
                            color = Color(0xFF59B259),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text("فعالیت‌ها در ماه ثبت شده جاری:", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${user.storyTokens} عدد",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text("توکن‌های ارسال استوری در دسترس:", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "توضیح: هر همکار که حداقل ${settings.minChaptersForStoryToken} فصل فعالیت در ماه جاری داشته باشد، در آغاز ماه بعد ${settings.storyTokensAwarded} توکن استوری برای تبلیغ مانهواها دریافت می‌کند.",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }

        // Log Chapter Contribution Form (Item 4: Reward Auto Injection Check)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E24)),
            border = BorderStroke(1.dp, Color(0xFF2D3139)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "بارگذاری فوق پیشرفته فصل جدید و تفکیک درآمد همکاران",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "فایل های ترجمه، کلین یا تایپ شده فصل را مشخص کرده و تخصیص همکاران را انجام دهید تا فروش حاصل از خرید چپتر توسط کاربران، به صورت زنده و دقیق به کیف پول هر شخص واریز شود.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("۱. مانهوای مربوطه را انتخاب کنید:", fontSize = 11.sp, color = Color.LightGray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("سولو لولینگ (تک‌رو)", "برج خدا", "حرامزاده").forEach { name ->
                        val isSelected = selectedMangaToLog == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF00C6FF) else Color(0xFF16191E))
                                .clickable { selectedMangaToLog = name }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("۲. شماره این فصل (چپتر):", fontSize = 11.sp, color = Color.LightGray)
                OutlinedTextField(
                    value = chapterToLogInput,
                    onValueChange = { chapterToLogInput = it },
                    placeholder = { Text("مثال: 5") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // DRAG AND DROP / FILE SELECTOR SECTION
                Text("۳. بارگذاری فایل های فصل (انتخاب سند یا رهاسازی در کادر):", fontSize = 11.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDraggingOver) Color(0xFF132235) else Color(0xFF14161A))
                        .clickable {
                            // Mock File selection
                            selectedFileName = "manga_chapter_" + (chapterToLogInput.ifBlank { "0" }) + "_release.zip"
                            selectedFileSize = "34.5 مگابایت"
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedFileName.isBlank()) {
                            Text("📁 جهت انتخاب فایل مانهوا کلیک کنید یا آن را به اینجا بکشید (درگ اند دراپ)", color = Color.Gray, fontSize = 9.sp, textAlign = TextAlign.Center)
                            Text("(نوع مجاز: ZIP, RAR, PDF)", color = Color.DarkGray, fontSize = 8.sp, textAlign = TextAlign.Center)
                        } else {
                            Text("✔️ فایل دریافت و راستی‌آزمایی شد:", color = Color(0xFF69F0AE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$selectedFileName ($selectedFileSize)", color = Color.White, fontSize = 9.sp)
                            Text("جهت تعویض کلیک کنید", color = Color.Gray, fontSize = 8.sp)
                        }
                    }
                }

                // Simulate Drag over with a button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = {
                            isDraggingOver = !isDraggingOver
                            if (isDraggingOver) {
                                selectedFileName = "manga_chapter_dragged_source.rar"
                                selectedFileSize = "29.1 مگابایت"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isDraggingOver) "شبیه‌سازی رها کردن فایل" else "شبیه‌سازی گرفتن و کشیدن فایل", fontSize = 9.sp, color = Color(0xFF00C6FF))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CONTRIBUTOR SELECTOR SECTION (DYNAMIC PERSONNEL SELECTION FROM DATABASE COWORKERS!)
                Text("۴. تعیین وظایف و کادر دست‌اندرکاران این فصل:", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                
                val staffUsers = userAccounts.filter { it.role == "STAFF" || it.role == "DEPT_ADMIN" || it.role == "SUPER_ADMIN" }
                
                // 4A: Translator Selection
                Spacer(modifier = Modifier.height(6.dp))
                Text("مترجم این فصل:", color = Color.LightGray, fontSize = 10.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    staffUsers.forEach { staff ->
                        val selected = selectedTranslatorId == staff.id
                        FilterChip(
                            selected = selected,
                            onClick = { selectedTranslatorId = staff.id },
                            label = { Text(staff.displayName, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0055B3), selectedLabelColor = Color.White)
                        )
                    }
                }

                // 4B: Cleaner Selection
                Spacer(modifier = Modifier.height(4.dp))
                Text("کلینر این فصل:", color = Color.LightGray, fontSize = 10.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    staffUsers.forEach { staff ->
                        val selected = selectedCleanerId == staff.id
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCleanerId = staff.id },
                            label = { Text(staff.displayName, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0055B3), selectedLabelColor = Color.White)
                        )
                    }
                }

                // 4C: Editor/Typist Selection
                Spacer(modifier = Modifier.height(4.dp))
                Text("ادیتور/تایپیست این فصل:", color = Color.LightGray, fontSize = 10.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    staffUsers.forEach { staff ->
                        val selected = selectedEditorId == staff.id
                        FilterChip(
                            selected = selected,
                            onClick = { selectedEditorId = staff.id },
                            label = { Text(staff.displayName, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0055B3), selectedLabelColor = Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val chNum = chapterToLogInput.toIntOrNull()
                        if (chNum == null) {
                            Toast.makeText(context, "لطفا شماره فصل معتبر وارد کنید.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedFileName.isBlank()) {
                            Toast.makeText(context, "لطفا فایل فصول مانهوا را انتخاب یا رها کنید.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val matchedMangaObj = viewModel.mangas.value.find { it.titleFa == selectedMangaToLog }
                        val mangaIdToAssign = matchedMangaObj?.id ?: 1

                        viewModel.addChapterUploadAndWork(
                            mangaId = mangaIdToAssign,
                            chapterNumber = chNum,
                            translatorId = selectedTranslatorId,
                            cleanerId = selectedCleanerId,
                            editorId = selectedEditorId,
                            uploadFileUri = selectedFileName,
                            onSuccess = {
                                chapterToLogInput = ""
                                selectedFileName = ""
                                Toast.makeText(context, "فصل $chNum با موفقیت آپلود و در صف انتشار ثبت گردید. پاداش کوپن همکاران و سیستم تقسیم درآمد هوشمند با موفقیت پایه‌گذاری شد!", Toast.LENGTH_LONG).show()
                            },
                            onError = {
                                Toast.makeText(context, "خطا: $it", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بارگذاری نهایی چپتر و فعال‌سازی توزیع عادلانه درآمد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
