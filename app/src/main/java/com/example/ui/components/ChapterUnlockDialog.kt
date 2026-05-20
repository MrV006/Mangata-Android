package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Stars
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
fun ChapterUnlockDialog(
    mangaId: Int,
    chapterNumber: Int,
    viewModel: MovieViewModel,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit
) {
    val currentUser by viewModel.currentUserAccount.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val context = LocalContext.current

    var selectedBulkCount by remember { mutableStateOf(1) } // Bulk purchase selection: 1, 50, 100 chapters

    val basePrice = settings.baseChapterPrice
    val group50Pct = settings.discountPercent50
    val group100Pct = settings.discountPercent100

    val totalPrice = remember(selectedBulkCount, basePrice, group50Pct, group100Pct) {
        val raw = basePrice * selectedBulkCount
        if (selectedBulkCount >= 100) {
            raw * (100 - group100Pct) / 100
        } else if (selectedBulkCount >= 50) {
            raw * (100 - group50Pct) / 100
        } else {
            raw
        }
    }

    val activeDiscountText = remember(selectedBulkCount) {
        if (selectedBulkCount >= 100) {
            "تخفیف گروهی خرید عمده: $group100Pct% 💥"
        } else if (selectedBulkCount >= 50) {
            "تخفیف گروهی خرید عمده: $group50Pct% 🔥"
        } else {
            ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "قفل این بخش را باز کنید",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
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
                    text = "فصل $chapterNumber به کادر همکاران کرونکو کردیت شده است.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wallets summary
                currentUser?.let { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("موجودی کیف پول ها:", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${user.walletGiftChapters} چپتر", color = Color(0xFF00C6FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("۱. کیف پول هدیه (رایگان):", color = Color.White, fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${user.walletRial} تومان", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("۲. کیف پول ریالی (شارژ شده):", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("انتخاب حجم خرید:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1 to "فقط ۱ فصل",
                        50 to "۵۰ فصل (تخفیف $group50Pct%)",
                        100 to "۱۰۰ فصل (تخفیف $group100Pct%)"
                    ).forEach { (count, label) ->
                        val isSelected = selectedBulkCount == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0055B3) else Color(0xFF1D2024))
                                .clickable { selectedBulkCount = count }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (activeDiscountText.isNotEmpty()) {
                    Text(
                        text = activeDiscountText,
                        color = Color(0xFF59B259),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider(color = Color(0xFF2D3139))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selectedBulkCount == 1) "۱ چپتر هدیه" else "غیرقابل خرید با کوپن",
                        color = if (selectedBulkCount == 1) Color(0xFF00C6FF) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text("قیمت با چپتر هدیه:", color = Color.White, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$totalPrice تومان", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("قیمت نقدی (ریالی / شتاب):", color = Color.White, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedBulkCount == 1) {
                    Button(
                        onClick = {
                            viewModel.purchaseSingleChapter(
                                mangaId = mangaId,
                                chapterNumber = chapterNumber,
                                useGiftPoints = true,
                                onSuccess = {
                                    Toast.makeText(context, "پرداخت موفق! چپتر آزاد شد.", Toast.LENGTH_SHORT).show()
                                    onUnlockSuccess()
                                    onDismiss()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خرید با ۱ چپتر هدیه", fontSize = 12.sp, color = Color.Black)
                    }
                }

                Button(
                    onClick = {
                        if (selectedBulkCount == 1) {
                            viewModel.purchaseSingleChapter(
                                mangaId = mangaId,
                                chapterNumber = chapterNumber,
                                useGiftPoints = false,
                                onSuccess = {
                                    Toast.makeText(context, "سهم مترجم و ادیتورها پرداخت گردید! چپتر آزاد شد.", Toast.LENGTH_SHORT).show()
                                    onUnlockSuccess()
                                    onDismiss()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            viewModel.purchaseBulkChapters(
                                mangaId = mangaId,
                                startChapter = chapterNumber,
                                count = selectedBulkCount,
                                onSuccess = {
                                    Toast.makeText(context, "$selectedBulkCount چپتر به صورت گروهی با تخفیف آزاد شدند!", Toast.LENGTH_SHORT).show()
                                    onUnlockSuccess()
                                    onDismiss()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF59B259)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.LocalAtm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (selectedBulkCount == 1) "پرداخت مستقیم ریالی ($totalPrice تومان)"
                        else "خرید بسته گروهی ($totalPrice تومان)",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color.Gray, fontSize = 12.sp)
            }
        },
        containerColor = Color(0xFF1E2229),
        shape = RoundedCornerShape(20.dp)
    )
}
