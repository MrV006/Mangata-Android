package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myket.MyketBillingHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyketPurchaseDialog(
    skuDetails: MyketBillingHelper.SkuDetails,
    onDismiss: () -> Unit,
    onConfirmPurchase: (String) -> Unit
) {
    // Custom Farsi colors representing Myket billing environment
    val myketPrimaryBrandColor = Color(0xFF21A653) // Myket green
    val myketDarkGray = Color(0xFF1E1E1E)
    val myketBackground = Color(0xFF121212)
    val myketTextMuted = Color(0xFF9E9E9E)

    var selectedPaymentMethod by remember { mutableStateOf(0) } // 0 = کیف پول مایکت, 1 = درگاه بانکی مستقیم

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .testTag("myket_purchase_dialog_card"),
                colors = CardDefaults.cardColors(containerColor = myketBackground),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.End // Supports RTL Farsi
                ) {
                    // Header line
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .background(Color(0xFF3C3C3C), RoundedCornerShape(2.dp))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Myket branding and Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Secure shopping label with lock
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "پرداخت امن",
                                color = myketPrimaryBrandColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "امنیت",
                                tint = myketPrimaryBrandColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Logo label
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "درگاه رسمی پرداخت مایکت",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Right
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(myketPrimaryBrandColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "M",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFF222222), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Product Box details
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = skuDetails.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = skuDetails.description,
                                color = myketTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = skuDetails.priceFa,
                                    color = myketPrimaryBrandColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "مبلغ تمدید:",
                                    color = myketTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Payment Method title
                    Text(
                        "روش پرداخت را انتخاب کنید:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Wallet choice (Method 0)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(
                                if (selectedPaymentMethod == 0) Color(0xFF263238) else Color(0xFF1E1E1E),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (selectedPaymentMethod == 0) myketPrimaryBrandColor else Color(0xFF2C2C2C),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPaymentMethod = 0 }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedPaymentMethod == 0) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "انتخاب شده",
                                    tint = myketPrimaryBrandColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .border(1.dp, myketTextMuted, RoundedCornerShape(9.dp))
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "کیف پول مایکت (سریع و آسان)",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "موجودی کافی است",
                                        color = myketPrimaryBrandColor,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    Icons.Default.Payment,
                                    contentDescription = "کیف پول",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Direct Gateway choice (Method 1)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedPaymentMethod == 1) Color(0xFF263238) else Color(0xFF1E1E1E),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (selectedPaymentMethod == 1) myketPrimaryBrandColor else Color(0xFF2C2C2C),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPaymentMethod = 1 }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedPaymentMethod == 1) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "انتخاب شده",
                                    tint = myketPrimaryBrandColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .border(1.dp, myketTextMuted, RoundedCornerShape(9.dp))
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "پرداخت آنلاین با کارت‌های شتاب",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "اتصال به کلیه درگاه‌های بانکی",
                                        color = myketTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = "درگاه بانکی",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Button(
                        onClick = { onConfirmPurchase(skuDetails.sku) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("myket_complete_purchase_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = myketPrimaryBrandColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "پرداخت و فعال‌سازی اشتراک VIP",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("myket_cancel_purchase_button")
                    ) {
                        Text(
                            text = "انصراف و بازگشت به برنامه",
                            color = myketTextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
