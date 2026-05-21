package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecruitmentApplication
import com.example.data.SystemSettingsEntity
import com.example.data.UserAccount
import com.example.ui.MovieViewModel

@Composable
fun AdminPanel(viewModel: MovieViewModel) {
    val currentUser by viewModel.currentUserAccount.collectAsState()
    val accounts by viewModel.userAccounts.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val recruitments by viewModel.recruitmentApps.collectAsState()
    val allTickets by viewModel.allSupportTickets.collectAsState()
    val unansweredCount = allTickets.count { !it.isAnswered }
    val context = LocalContext.current

        var selectedAdminSubTab by remember { mutableStateOf("تنظیمات") } // "تنظیمات", "مشارکت‌ها", "استخدام‌ها", "نسخه‌ها", "مدیریت آثار & آپلود", "طراحی و محتوا"

        Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Welcoming Admin Banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1400)), // Gold-edged dark
            border = BorderStroke(1.dp, Color(0xFFFFD700)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Live Support Notification Ring
                if (unansweredCount > 0) {
                    val infiniteTransition = rememberInfiniteTransition(label = "neon_pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                color = Color(0xFFFF1744).copy(alpha = 0.15f * pulseAlpha),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.6.dp,
                                color = Color(0xFFFF1744).copy(alpha = pulseAlpha),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF1744), shape = CircleShape)
                        )
                        Text(
                            text = "$unansweredCount تیکت معلق",
                            color = Color(0xFFFFD2D2),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
                            .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(currentUser?.subRole ?: "مدیر کل", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "داشبورد ادمین کل (حالت کبریا / خدا)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تنظیم تمام متغیرها به صورت پویا، اهدای چپتر‌های رایگان همکاران و تسویه حساب کادر زحمت‌کش.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right
                )
            }
          }
        }

        // Sub tabs
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("تنظیمات", "مشارکت‌ها", "استخدام‌ها").forEach { tab ->
                    val isSelected = selectedAdminSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF0055B3) else Color(0xFF16191E))
                            .clickable { selectedAdminSubTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("نسخه‌ها", "مدیریت آثار", "محتوا").forEach { tab ->
                    val isSelected = selectedAdminSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF0055B3) else Color(0xFF16191E))
                            .clickable { selectedAdminSubTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        when (selectedAdminSubTab) {
            "تنظیمات" -> AdminConfigureSettings(viewModel, settings)
            "مشارکت‌ها" -> AdminUserCollaboration(viewModel, accounts)
            "استخدام‌ها" -> AdminRecruitmentsList(viewModel, recruitments)
            "نسخه‌ها" -> AdminVersionControl(viewModel)
            "مدیریت آثار" -> AdminMangaManager(viewModel)
            "محتوا" -> AdminUIContentManager(viewModel)
        }
    }
}

@Composable
fun AdminConfigureSettings(viewModel: MovieViewModel, settings: SystemSettingsEntity) {
    val context = LocalContext.current

    var basePrice by remember(settings) { mutableStateOf(settings.baseChapterPrice.toString()) }
    var disc50 by remember(settings) { mutableStateOf(settings.discountPercent50.toString()) }
    var disc100 by remember(settings) { mutableStateOf(settings.discountPercent100.toString()) }
    var staffReward by remember(settings) { mutableStateOf(settings.defaultStaffRewardChapters.toString()) }
    var minChaptersStory by remember(settings) { mutableStateOf(settings.minChaptersForStoryToken.toString()) }
    var storyTokensAdded by remember(settings) { mutableStateOf(settings.storyTokensAwarded.toString()) }

    var clnPct by remember(settings) { mutableStateOf(settings.shareCleanerPct.toString()) }
    var edtPct by remember(settings) { mutableStateOf(settings.shareEditorPct.toString()) }
    var trnPct by remember(settings) { mutableStateOf(settings.shareTranslatorPct.toString()) }
    var sysPct by remember(settings) { mutableStateOf(settings.sharePlatformPct.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
        border = BorderStroke(1.dp, Color(0xFF2D3139)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("پیکربندی داینامیک اقتصاد سیستم", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = basePrice,
                onValueChange = { basePrice = it },
                label = { Text("قیمت پایه چپترها (تومان)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = disc50,
                onValueChange = { disc50 = it },
                label = { Text("درصد تخفیف خرید بالای ۵۰ چپتر (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = disc100,
                onValueChange = { disc100 = it },
                label = { Text("درصد تخفیف خرید بالای ۱۰۰ چپتر (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("ساختار تقسیم درآمدهای مالی (%):", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = clnPct,
                    onValueChange = { clnPct = it },
                    label = { Text("سهم کلینر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = edtPct,
                    onValueChange = { edtPct = it },
                    label = { Text("سهم ادیتور") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = trnPct,
                    onValueChange = { trnPct = it },
                    label = { Text("سهم مترجم") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sysPct,
                    onValueChange = { sysPct = it },
                    label = { Text("سهم وب‌سایت") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("آیین‌نامه‌ها و مشوق همکاران:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            OutlinedTextField(
                value = staffReward,
                onValueChange = { staffReward = it },
                label = { Text("پاداش کوپن مفت برای هر فعالیت کار شده") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = minChaptersStory,
                onValueChange = { minChaptersStory = it },
                label = { Text("حداقل فعالیت مانهوا برای سهمیه استوری") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = storyTokensAdded,
                onValueChange = { storyTokensAdded = it },
                label = { Text("تعداد توکن اعطایی استوری در ماه") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color(0xFF2D3139), modifier = Modifier.padding(vertical = 12.dp))

            Text("وضعیت تست‌های استخدام کادر فنی", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            Text("نکته: غیرفعال کردن هر گزینه بلافاصله دکمه ارسال رزومه آن تخصص را در فرم ثبت‌نام اصلی غیرفعال می‌کند.", color = Color.Gray, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val tActive by viewModel.isTranslatorTestUploaded.collectAsState()
            val cActive by viewModel.isCleanerTestUploaded.collectAsState()
            val tyActive by viewModel.isTypistTestUploaded.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = tActive, onCheckedChange = { viewModel.setTranslatorTestUploaded(it) })
                Text("۱. فایل تست مترجمی فعال باشد", color = Color.LightGray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = cActive, onCheckedChange = { viewModel.setCleanerTestUploaded(it) })
                Text("۲. فایل تست کلینر فعال باشد", color = Color.LightGray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = tyActive, onCheckedChange = { viewModel.setTypistTestUploaded(it) })
                Text("۳. فایل تست ادیتور/تایپیست فعال باشد", color = Color.LightGray, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val p = basePrice.toIntOrNull() ?: 400
                    val d50 = disc50.toIntOrNull() ?: 20
                    val d100 = disc100.toIntOrNull() ?: 40
                    val reward = staffReward.toIntOrNull() ?: 5
                    val limitS = minChaptersStory.toIntOrNull() ?: 40
                    val tokens = storyTokensAdded.toIntOrNull() ?: 2

                    val cS = clnPct.toIntOrNull() ?: 30
                    val eS = edtPct.toIntOrNull() ?: 30
                    val tS = trnPct.toIntOrNull() ?: 20
                    val sS = sysPct.toIntOrNull() ?: 20

                    if (cS + eS + tS + sS == 100) {
                        viewModel.updateSystemSettings(
                            SystemSettingsEntity(
                                baseChapterPrice = p,
                                discountPercent50 = d50,
                                discountPercent100 = d100,
                                defaultStaffRewardChapters = reward,
                                minChaptersForStoryToken = limitS,
                                storyTokensAwarded = tokens,
                                shareCleanerPct = cS,
                                shareEditorPct = eS,
                                shareTranslatorPct = tS,
                                sharePlatformPct = sS
                            )
                        )
                        Toast.makeText(context, "پیکربندی با موفقیت روی رم ثبت و اعمال شد.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "خطا: جمع درصدهای تقسیم درآمد باید دقیقا ۱۰۰ باشد.", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ثبت و اعمال همگانی در دیتابیس", color = Color.White)
            }
        }
    }
}

@Composable
fun AdminUserCollaboration(viewModel: MovieViewModel, accounts: List<UserAccount>) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
        border = BorderStroke(1.dp, Color(0xFF2D3139)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("تغییر سطوح دسترسی، ترفیع و اهدای کوپن هدیه", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            accounts.forEach { account ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
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
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when (account.role) {
                                            "SUPER_ADMIN" -> Color(0xFFFFD700)
                                            "ADMIN" -> Color(0xFFE57373)
                                            "DEPT_ADMIN" -> Color(0xFF9C27B0)
                                            "STAFF" -> Color(0xFF4CAF50)
                                            else -> Color(0xFF90A4AE)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(account.role, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(account.displayName + " (${account.subRole})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("اعتبار نقدی: ${account.walletRial} تومان", color = Color.LightGray, fontSize = 10.sp)
                            Text("کوپن خوانش: ${account.walletGiftChapters} چپتر", color = Color.LightGray, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Actions for admin
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.awardGiftChapters(account.id, 10)
                                        Toast.makeText(context, "۱۰ فصل هدیه با کرم مدیر تایید و تزریق شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(28.dp)
                                ) {
                                    Text("+۱۰ کوپن ویژه", fontSize = 8.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val updated = account.copy(role = "STAFF", subRole = "مترجم تیم", storyTokens = 4)
                                        viewModel.updateUserAccount(updated)
                                        Toast.makeText(context, "نقش ${account.displayName} به عضویت کادر فنی مانگا ترفیع یافت.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(28.dp)
                                ) {
                                    Text("ارتقا به کادر فنی", fontSize = 8.sp, color = Color.White)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val updated = account.copy(role = "DEPT_ADMIN", subRole = "مدیر بخش تیکت‌ها")
                                        viewModel.updateUserAccount(updated)
                                        Toast.makeText(context, "${account.displayName} به رول ادمین بخش (پاسخ‌دهنده تیکت) ارتقا یافت.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1.1f).height(28.dp)
                                ) {
                                    Text("انتصاب به عنوان ناظر تیکت", fontSize = 8.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        val updated = account.copy(role = "NORMAL_USER", subRole = "خواننده عادی")
                                        viewModel.updateUserAccount(updated)
                                        Toast.makeText(context, "دسترسی‌های ${account.displayName} بازنشانی و عادی شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(0.9f).height(28.dp)
                                ) {
                                    Text("تنزل به کاربر عادی", fontSize = 8.sp, color = Color.White)
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val toPay = account.walletRial
                                        if (toPay > 0) {
                                            val updated = account.copy(walletRial = 0)
                                            viewModel.updateUserAccount(updated)
                                            Toast.makeText(context, "تسویه حساب: مبلغ ${toPay} تومان برای ${account.displayName} واریز و صفر شد.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "موجودی این کاربر صفر است.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(28.dp)
                                ) {
                                    Text("تسویه حساب مالی دوره‌ای", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val updated = account.copy(role = "BANNED", subRole = "کاربر مسدود شده")
                                        viewModel.updateUserAccount(updated)
                                        Toast.makeText(context, "کاربر ${account.displayName} با موفقیت مسدود و تعلیق شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(28.dp)
                                ) {
                                    Text("مسدودسازی / تعلیق اکانت", fontSize = 8.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Revenue Monitoring & Error Adjustment Tool Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "مالی: تراز زنده مشارکت‌ها و اصلاح واریزی‌های اشتباه",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "در این بخش می‌توانید درآمد جزء به جزء هر چپتر مانهوا را مشاهده، تحلیل و مانیتور کرده و در صورت بروز خطا در واریزی دستی یا اتوماتیک همکاران، فوراً مبالغ را بازگردانی و اصلاح فرمایید.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 16.sp
                    )

                    val chapterWorks by viewModel.allChapterWorks.collectAsState()
                    
                    // Correction fields states
                    var selectedWorkForAdj by remember { mutableStateOf<com.example.data.ChapterWork?>(null) }
                    var adjustTransId by remember { mutableStateOf("") }
                    var adjustCleanId by remember { mutableStateOf("") }
                    var adjustEditId by remember { mutableStateOf("") }
                    var adjustmentError by remember { mutableStateOf("") }

                    if (selectedWorkForAdj != null) {
                        val wk = selectedWorkForAdj!!
                        // Adjustment Form Box
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF231C13)),
                            border = BorderStroke(1.dp, Color(0xFFFF9800)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "فرم لایو جابجایی کادر همکاران مانهوا و اصلاح خودکار مالی",
                                    color = Color(0xFFFF9800),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "مانهوا #${wk.mangaId}، فصل #${wk.chapterNumber}. با وارد نمودن کدهای همکاران معتبر جدید، کل درآمد این چپتر مستقیماً از اعضای قبلی برداشته شده و به کادر جدید تخصیص می‌یابد.",
                                    color = Color.LightGray,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = adjustTransId,
                                    onValueChange = { adjustTransId = it },
                                    placeholder = { Text("آیدی مترجم معتبر جدید (فعلی: ${wk.translatorId})") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = adjustCleanId,
                                    onValueChange = { adjustCleanId = it },
                                    placeholder = { Text("آیدی کلینر معتبر جدید (فعلی: ${wk.cleanerId})") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = adjustEditId,
                                    onValueChange = { adjustEditId = it },
                                    placeholder = { Text("آیدی تایپیست/ادیتور معتبر جدید (فعلی: ${wk.editorId})") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                if (adjustmentError.isNotBlank()) {
                                    Text(adjustmentError, color = Color.Red, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val trans = adjustTransId.toIntOrNull() ?: wk.translatorId
                                            val clean = adjustCleanId.toIntOrNull() ?: wk.cleanerId
                                            val edit = adjustEditId.toIntOrNull() ?: wk.editorId

                                            viewModel.adjustChapterRevenueAllocation(
                                                mangaId = wk.mangaId,
                                                chapterNumber = wk.chapterNumber,
                                                correctTranslatorId = trans,
                                                correctCleanerId = clean,
                                                correctEditorId = edit,
                                                onSuccess = {
                                                    selectedWorkForAdj = null
                                                    adjustTransId = ""
                                                    adjustCleanId = ""
                                                    adjustEditId = ""
                                                    adjustmentError = ""
                                                    Toast.makeText(context, "سهم همکاران اصلاح و مبالغ تراکنش عودت داده شد.", Toast.LENGTH_LONG).show()
                                                },
                                                onError = {
                                                    adjustmentError = it
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("تایید و تراز معکوس وجه", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { selectedWorkForAdj = null; adjustmentError = "" },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        modifier = Modifier.weight(0.7f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("انصراف", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF272C35))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("داشبورد تحلیلی مجموع درآمدها و کسری‌ها:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val totalRevenue = chapterWorks.sumOf { it.revenueEarned }
                    val totalPlatformEarned = chapterWorks.sumOf { it.platformEarned }
                    val totalPaidToStaff = totalRevenue - totalPlatformEarned

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E14)),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${totalRevenue} تومان", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("۱. مجموع فروش کل چپترها", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${totalPlatformEarned} تومان", color = Color(0xFF00C6FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("۲. سود خالص پلتفرم (سیستم)", color = Color.LightGray, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${totalPaidToStaff} تومان", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("۳. مجموع حقوق پخش‌شده همکاران", color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF272C35))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("جدول ثبت درآمدهای تفکیکی فصول و مانهواها:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (chapterWorks.isEmpty()) {
                        Text("تراکنش مشارکتی ضبط شده‌ای یافت نشد.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        chapterWorks.forEach { work ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2126)),
                                border = BorderStroke(1.dp, Color(0xFF2D3139))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "کل درآمد: ${work.revenueEarned} تومان",
                                                color = Color(0xFFFFD700),
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "آیدی اثر #${work.mangaId} - فصل ${work.chapterNumber}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("کلینر: ${work.cleanerPaid} ت", color = Color.LightGray, fontSize = 9.sp)
                                        Text("ادیتور: ${work.editorPaid} ت", color = Color.LightGray, fontSize = 9.sp)
                                        Text("مترجم: ${work.translatorPaid} ت", color = Color.LightGray, fontSize = 9.sp)
                                        Text("سیستم: ${work.platformEarned} ت", color = Color(0xFF00C6FF), fontSize = 9.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFF272C35))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                selectedWorkForAdj = work
                                                adjustTransId = ""
                                                adjustCleanId = ""
                                                adjustEditId = ""
                                                adjustmentError = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("اصلاح و جابجایی سهم اشتباه ⇄", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Text(
                                            text = "شناسه کادر تولید: مترجم ${work.translatorId} / کلینر ${work.cleanerId} / ادیت ${work.editorId}",
                                            color = Color.Gray,
                                            fontSize = 8.sp
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
fun AdminRecruitmentsList(viewModel: MovieViewModel, recruitments: List<RecruitmentApplication>) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
        border = BorderStroke(1.dp, Color(0xFF2D3139)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("درخواست‌های استخدام ورودی همکاران جدید", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (recruitments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("درخواست استخدام جدیدی ثبت نشده است.", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                recruitments.forEach { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
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
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (app.status == "PENDING") Color(0xFFFF9800)
                                            else if (app.status == "APPROVED") Color(0xFF4CAF50)
                                            else Color(0xFFF44336),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(app.status, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(app.fullName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Text("تخصص انتخابی: ${app.specialty}", color = Color.LightGray, fontSize = 11.sp)
                            Text("شناسه ارتباطی: ${app.messengerId}", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("پاسخ تست ارسالی: ${app.uploadedWorkName}", color = Color.Gray, fontSize = 9.sp)

                            if (app.status == "PENDING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.reviewRecruitment(app, false)
                                            Toast.makeText(context, "درخواست متقاضی رد گردید.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("رد کارنامه", fontSize = 10.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.reviewRecruitment(app, true)
                                            Toast.makeText(context, "عضو تایید و به صورت اتوماتیک به Staff انتقال فرستاد!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("قبولی و ارتقای متقاضی", fontSize = 10.sp, color = Color.White)
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
fun AdminVersionControl(viewModel: MovieViewModel) {
    val serverVersion by viewModel.serverVersionCode.collectAsState()
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
        border = BorderStroke(1.dp, Color(0xFF2D3139)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text("سیستم تایید اعتبار نسخه و آپدیت اجباری برخط", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "با بالا بردن نسخه مورد نیاز سرور، تمام کاربران اپ در حالت قفل کامل غیرقابل خروج مسدود شده و مجبور به ارتقا به نسخه آخر می‌شوند.",
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 17.sp,
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("وضعیت نسخه سرور فعلی (داینامیک):", fontSize = 12.sp, color = Color.White)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Version 2 option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (serverVersion == 2) Color(0xFF59B259) else Color(0xFF1D2024))
                        .clickable {
                            viewModel.updateServerVersionCode(2)
                            Toast.makeText(context, "نسخه کلاینت تایید شد. قفل لغو گردید.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("نسخه ۲ (عادی)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("بدون محدودیت", color = Color.LightGray, fontSize = 9.sp)
                    }
                }

                // Version 3 option (Triggers block)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (serverVersion == 3) Color(0xFFE53935) else Color(0xFF1D2024))
                        .clickable {
                            viewModel.updateServerVersionCode(3)
                            Toast.makeText(context, "نسخه جدید منتشر شد! قفل آپدیت اجباری فعال گردید.", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("نسخه ۳ (جدید)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("فعال‌سازی قفل آپدیت", color = Color.LightGray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMangaManager(viewModel: MovieViewModel) {
    val context = LocalContext.current
    val mangas by viewModel.mangas.collectAsState()
    val featuredIds by viewModel.featuredMangaIds.collectAsState()
    val startsFromZeroMap by viewModel.mangaStartsFromZero.collectAsState()
    val workflows by viewModel.uploadWorkflow.collectAsState()

    var selectedMangaIndex by remember { mutableStateOf<Int?>(null) }
    var expandedMangaDropdown by remember { mutableStateOf(false) }

    val selectedManga = selectedMangaIndex?.let { mangas.getOrNull(it) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
        border = BorderStroke(1.dp, Color(0xFF2D3139)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "مدیریت کارها، خط تولید و آپلود چپترها",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown selector for Manga
            Text("انتخاب مانهوا:", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1D2024), RoundedCornerShape(8.dp))
                    .clickable { expandedMangaDropdown = true }
                    .padding(12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                    Text(
                        text = selectedManga?.titleFa ?: "--- یک مانهوا انتخاب کنید ---",
                        color = if (selectedManga != null) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = expandedMangaDropdown,
                    onDismissRequest = { expandedMangaDropdown = false },
                    modifier = Modifier.background(Color(0xFF1D2024))
                ) {
                    mangas.forEachIndexed { index, manga ->
                        DropdownMenuItem(
                            text = { Text(manga.titleFa, color = Color.White, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                            onClick = {
                                selectedMangaIndex = index
                                expandedMangaDropdown = false
                            }
                        )
                    }
                }
            }

            if (selectedManga != null) {
                val mangaId = selectedManga.id
                val isFeatured = featuredIds.contains(mangaId)
                val startsFromZero = startsFromZeroMap[mangaId] ?: false
                val workflowState = workflows[mangaId] ?: MovieViewModel.UploadWorkflowState()

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF2D3139))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Dynamic configurations
                Text("تنظیمات و چینش آثار", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Starts From Zero
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setMangaStartsFromZero(mangaId, !startsFromZero) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = startsFromZero,
                        onCheckedChange = { viewModel.setMangaStartsFromZero(mangaId, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFD700),
                            checkedTrackColor = Color(0xFF554400)
                        )
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("شروع چپترها از صفر (0)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("گاهی مانهواها دارای چپتر ۰ هستند و شمارنده باید از صفر بچرخد", color = Color.Gray, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle Featured Slider
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleFeaturedManga(mangaId) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { viewModel.toggleFeaturedManga(mangaId) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF8E2DE2),
                            checkedTrackColor = Color(0xFF3B1062)
                        )
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("قرار گرفتن در اسلایدر ویژه", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("نمایش این اثر در هدر داغ بالای صفحه هوم سایت و اپلیکیشن", color = Color.Gray, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF2D3139))
                Spacer(modifier = Modifier.height(12.dp))

                // Section 2: Edit Metadata info
                Text("ویرایش مشخصات مانهوا", fontWeight = FontWeight.Bold, color = Color(0xFFA8C7FA), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                var editTitleFa by remember(mangaId) { mutableStateOf(selectedManga.titleFa) }
                var editTitleEn by remember(mangaId) { mutableStateOf(selectedManga.titleEn) }
                var editDescFa by remember(mangaId) { mutableStateOf(selectedManga.descriptionFa) }
                var editCover by remember(mangaId) { mutableStateOf(selectedManga.coverUrl) }
                var editBanner by remember(mangaId) { mutableStateOf(selectedManga.bannerUrl) }

                OutlinedTextField(
                    value = editTitleFa,
                    onValueChange = { editTitleFa = it },
                    label = { Text("نام فارسی مانهوا") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = editTitleEn,
                    onValueChange = { editTitleEn = it },
                    label = { Text("نام انگلیسی / نام آلترناتیو") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = editDescFa,
                    onValueChange = { editDescFa = it },
                    label = { Text("خلاصه مانهوا") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 4.dp),
                    maxLines = 4
                )

                OutlinedTextField(
                    value = editCover,
                    onValueChange = { editCover = it },
                    label = { Text("لینک کاور مانهوا (JPG/PNG)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = editBanner,
                    onValueChange = { editBanner = it },
                    label = { Text("لینک بنر مانهوا (JPG/PNG)") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        viewModel.updateMangaDetails(
                            mangaId = mangaId,
                            titleFa = editTitleFa,
                            titleEn = editTitleEn,
                            descriptionFa = editDescFa,
                            coverUrl = editCover,
                            bannerUrl = editBanner
                        )
                        Toast.makeText(context, "اطلاعات ${editTitleFa} ارتقا و ذخیره گردید.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055B3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("به‌روزرسانی کل مشخصات در دیتابیس", color = Color.White, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF2D3139))
                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Workflow production pipeline
                Text("خط تولید و پورتال استخدام آپلود همکاران", fontWeight = FontWeight.Bold, color = Color(0xFF59B259), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "مترجم فایل Word را آپلود کرده، کلینر و تایپیست هم فایل ZIP می‌فرستند. با تایید نهایی مدیریت کل تفاله‌های Word/Clean جهت حفظ دیسک سرور حذف و فایل نهایی زیپ تصاویر به ترتیب عددی کامپایل می‌گردد.",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Pipeline Step 1: Translator
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
                    border = BorderStroke(1.dp, Color(0xFF2D3139))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.uploadWorkflowProgress(mangaId, "WORD", "translation_ch_fa.docx")
                                Toast.makeText(context, "فایل ورد مجمع ترجمه با موفقیت آپلود گردید.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23262B)),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("آپلود Word", fontSize = 9.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("۱. مترجم مانهوا (فایل Word)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (workflowState.translatorWordFile != null) "آپلود شد: ${workflowState.translatorWordFile}" else "در انتظار فایل مترجم...",
                                color = if (workflowState.translatorWordFile != null) Color(0xFF59B259) else Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Pipeline Step 2: Cleaner
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
                    border = BorderStroke(1.dp, Color(0xFF2D3139))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.uploadWorkflowProgress(mangaId, "CLEANER_ZIP", "cleaned_pages_images.zip")
                                Toast.makeText(context, "فایل زیپ تصاویر تصفیه شده با موفقیت آپلود شد.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23262B)),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("آپلود ZIP کلینر", fontSize = 9.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("۲. کلینر تصاویر (فایل ZIP دی‌کلین)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (workflowState.cleanerZipFile != null) "آپلود شد: ${workflowState.cleanerZipFile}" else "در انتظار فایل کلینر...",
                                color = if (workflowState.cleanerZipFile != null) Color(0xFF59B259) else Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Pipeline Step 3: Typesetter
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2024)),
                    border = BorderStroke(1.dp, Color(0xFF2D3139))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.uploadWorkflowProgress(mangaId, "EDITOR_ZIP", "typesetter_final_ch.zip")
                                Toast.makeText(context, "فایل نهایی ویرایش و چسباندن مجاز فونت آپلود گردید.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23262B)),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("آپلود ZIP نهایی", fontSize = 9.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("۳. ادیتور و تایپیست (فایل ZIP نهایی)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (workflowState.typesetterZipFile != null) "آپلود شد: ${workflowState.typesetterZipFile}" else "در انتظار فایل تایپیست...",
                                color = if (workflowState.typesetterZipFile != null) Color(0xFF59B259) else Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Pipeline Step 4: Approved & Published by Manager (Freeing Draft Storage and Compiling final sequential ZIP)
                var destChapterNum by remember(mangaId) { mutableStateOf((selectedManga.chaptersCount + 1).toString()) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = destChapterNum,
                        onValueChange = { destChapterNum = it },
                        label = { Text("شماره چپتر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )

                    Button(
                        onClick = {
                            val chNum = destChapterNum.toIntOrNull() ?: (selectedManga.chaptersCount + 1)
                            viewModel.approveAndPublishWorkflow(mangaId, chNum, if (startsFromZero) 0 else 1)
                            Toast.makeText(context, "کارهای موافقت تایید قرار گرفت و با موفقیت کامپایل و تمیز شد!", Toast.LENGTH_LONG).show()
                        },
                        enabled = workflowState.translatorWordFile != null && workflowState.cleanerZipFile != null && workflowState.typesetterZipFile != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تایید نهایی، آزادسازی دیسک و انتشار در سایت", color = Color.White, fontSize = 10.sp)
                    }
                }

                // Show Memory Cleanup Real Log Console
                if (workflowState.hasFinishedCompilation) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1410)),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("انتشار نهایی چپتر ${workflowState.chapterToPublish} موفقیت‌آمیز بود", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("⚡ لاگ آزاد‌سازی هوشمند حافظه سرور:", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("🗑️ فایل پیش‌نویس ورد ${workflowState.translatorWordFile} کاملا حذف فیزیکی گردید (حافظه آزاد شد).", color = Color.LightGray, fontSize = 9.sp)
                            Text("🗑️ فایل زیپ کلین ${workflowState.cleanerZipFile} به طور کامل پاک شد تا دیسک بدون دلیل پر نشود.", color = Color.LightGray, fontSize = 9.sp)
                            Text("📦 فقط فایل نهایی زیپ بهینه شده ${workflowState.outputZipFileName} از طریق سرور کلاینت با چینش عددی تصاویر ارسال می‌گردد.", color = Color(0xFF81C784), fontSize = 9.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF2D3139))
                Spacer(modifier = Modifier.height(12.dp))

                // Section 4: Fix / Delete rotten bugged chapters
                Text("تغییر یا تعویض چپتر خراب دیتابیس", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                var repairChapterNum by remember(mangaId) { mutableStateOf("1") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = repairChapterNum,
                        onValueChange = { repairChapterNum = it },
                        label = { Text("شماره چپتر خراب") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(120.dp)
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val chNum = repairChapterNum.toIntOrNull() ?: 1
                                viewModel.deleteOrReplaceChapter(mangaId, chNum, isReplace = false)
                                Toast.makeText(context, "چپتر معیوب ${chNum} با موفقیت حذف گردید.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("حذف کامل چپتر خراب", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                val chNum = repairChapterNum.toIntOrNull() ?: 1
                                viewModel.deleteOrReplaceChapter(mangaId, chNum, isReplace = true)
                                Toast.makeText(context, "صفحات چپتر ${chNum} با نسخه سالم تعویض گردید.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("تعویض با صفحات سالم", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
