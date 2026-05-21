package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MovieViewModel

@Composable
fun RecruitmentPortalCard(
    viewModel: MovieViewModel,
    modifier: Modifier = Modifier
) {
    var showApplyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                )
            )
            .clickable { showApplyDialog = true }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("شروع ثبت‌نام", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("پورتال استخدام هوشمند تیم مانگاتا", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("کسب درآمد پویا + اهدای چپتر‌های رایگان همکاران", color = Color.White.copy(alpha = 0.82f), fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Work, contentDescription = null, tint = Color.White)
                }
            }
        }
    }

    if (showApplyDialog) {
        RecruitmentApplyDialog(
            viewModel = viewModel,
            onDismiss = { showApplyDialog = false }
        )
    }
}

@Composable
fun RecruitmentApplyDialog(
    viewModel: MovieViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var selectedSpecialty by remember { mutableStateOf("مترجم") } // "مترجم", "تایپیست/ادیتور", "کلینر"
    var fullName by remember { mutableStateOf("") }
    var messengerId by remember { mutableStateOf("") }
    var rawTestFileDownloaded by remember { mutableStateOf(false) }
    var solutionUploaded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "استخدام کادر ترجمه تیم مانگاتا (Mangata)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "ارزیابی و تایید کارها مستقیماً در پنل مدیران ارشد بخش‌ها انجام می‌شود.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("نام و نام خانوادگی شما") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = messengerId,
                    onValueChange = { messengerId = it },
                    label = { Text("شناسه پیامرسان شما (تلگرام، ایتا، بله...)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                val trnActive by viewModel.isTranslatorTestUploaded.collectAsState()
                val clnActive by viewModel.isCleanerTestUploaded.collectAsState()
                val typActive by viewModel.isTypistTestUploaded.collectAsState()

                Text("تخصص درخواستی را انتخاب کنید:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val specialties = listOf(
                        Triple("کلینر", "کلینر", clnActive),
                        Triple("تایپیست/ادیتور", "تایپیست/ادیتور", typActive),
                        Triple("مترجم", "مترجم", trnActive)
                    )
                    specialties.forEach { (label, specialty, active) ->
                        val isSelected = selectedSpecialty == specialty
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        !active -> Color(0xFF151515) // Disabled state color
                                        isSelected -> Color(0xFF0072FF)
                                        else -> Color(0xFF1D2024)
                                    }
                                )
                                .clickable(enabled = active) {
                                    selectedSpecialty = specialty
                                    rawTestFileDownloaded = false
                                    solutionUploaded = false
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (active) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color(0xFF2D3139))

                Spacer(modifier = Modifier.height(12.dp))

                // Action Step 1: Download Test file
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (rawTestFileDownloaded) Color(0xFF1E3A1E) else Color(0xFF16191E))
                        .clickable {
                            rawTestFileDownloaded = true
                            Toast
                                .makeText(
                                    context,
                                    "فایل خام ارزیابی (${selectedSpecialty}) دانلود شد.",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = if (rawTestFileDownloaded) Color(0xFF59B259) else Color(0xFF00C6FF)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (rawTestFileDownloaded) "۱. تست خام با موفقیت دانلود شد ✓" else "۱. دانلود تست خام تخصصی",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("صفحات خام مانهوا با رزولوشن اصلی جهت آزمایش", color = Color.Gray, fontSize = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Step 2: Upload Test solutions file
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (solutionUploaded) Color(0xFF1E3A1E)
                            else if (!rawTestFileDownloaded) Color(0xFF111215)
                            else Color(0xFF16191E)
                        )
                        .clickable(enabled = rawTestFileDownloaded) {
                            solutionUploaded = true
                            Toast
                                .makeText(context, "پاسخ ادیت شده با موفقیت به سرور پیوست شد.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = if (solutionUploaded) Color(0xFF59B259) else if (rawTestFileDownloaded) Color(0xFFFFD700) else Color.Gray
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (solutionUploaded) "۲. فایل شما با موفقیت پیوست شد ✓" else "۲. آپلود پاسخ حل‌شده تست",
                            color = if (rawTestFileDownloaded) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("آرشیو زیپ‌شده ترجمه / ادیت تمیز شده شما", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotEmpty() && messengerId.isNotEmpty() && solutionUploaded) {
                        viewModel.applyForRecruitment(
                            fullName = fullName,
                            messengerId = messengerId,
                            specialty = selectedSpecialty,
                            onSuccess = {
                                Toast.makeText(context, "درخواست با موفقیت ثبت شد! وضعیت: در حال بررسی مدیریت", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        )
                    } else {
                        Toast.makeText(context, "لطفا نام، شناسه پیامرسان و فایل پاسخ را تکمیل کنید.", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("ثبت درخواست استخدام رسمی", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E2229),
        shape = RoundedCornerShape(20.dp)
    )
}
