package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    val context = LocalContext.current

    var selectedMangaToLog by remember { mutableStateOf("سولو لولینگ (تک‌رو)") }
    var chapterToLogInput by remember { mutableStateOf("") }
    var workedChaptersLoggedToast by remember { mutableStateOf(false) }

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
                    text = "به کادر مقتدر و پویای کرونکو خوش آمدید. عملکرد و فعالیت‌های شما مستقیما محاسبه و به درآمد متوازن شما افزوده می‌شود.",
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
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E24)),
            border = BorderStroke(1.dp, Color(0xFF2D3139)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "ثبت مانهوای کار شده (دریافت پاداش کوپن)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("آدرس مانهوا کار شده:", fontSize = 11.sp, color = Color.LightGray)
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
                                .background(if (isSelected) Color(0xFF0055B3) else Color(0xFF16191E))
                                .clickable { selectedMangaToLog = name }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = chapterToLogInput,
                    onValueChange = { chapterToLogInput = it },
                    label = { Text("شماره فصلی که ترجمه/ادیت کرده‌اید") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val chNum = chapterToLogInput.toIntOrNull()
                        if (currentUser != null && chNum != null) {
                            viewModel.addStaffContribution(staffId = currentUser!!.id, countAdded = 1)
                            chapterToLogInput = ""
                            workedChaptersLoggedToast = true
                            Toast.makeText(context, "فعالیت ثبت شد! به طور خودکار $activeRewardCount چپتر هدیه به حسابتان شارژ شد.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "لطفا شماره فصل معتبر وارد کنید.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ثبت فعالیت و دریافت خودکار پاداش (+ $activeRewardCount کوپن)", fontSize = 11.sp)
                }
            }
        }
    }
}
