package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MovieViewModel
import com.example.data.SupportTicket

@Composable
fun SupportTicketSystem(viewModel: MovieViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUserAccount.collectAsState()
    val userTickets by viewModel.userSupportTickets.collectAsState()
    val allTickets by viewModel.allSupportTickets.collectAsState()

    var ticketTitle by remember { mutableStateOf("") }
    var ticketDesc by remember { mutableStateOf("") }

    if (currentUser == null) return

    val isAdmin = currentUser?.role == "SUPER_ADMIN" || currentUser?.role == "DEPT_ADMIN" || currentUser?.role == "ADMIN"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14171C)),
        border = BorderStroke(1.dp, Color(0xFF272C35)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "مرکز پشتیبانی و فاکتور شارژ حساب مانگاتا",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Right
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info notice on manual payment / charging
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF332000))
                    .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "💵 برای شارژ کیف پول خود، لطفا یک تیکت فاقد ابهام از طریق فیلدهای زیر ثبت کنید و مبلغ مورد نیاز را بیان نمایید. شماره کارت بانکی مستقیماً از طریق پاسخ همین تیکت جهت واریز برای شما ارسال خواهد شد.",
                    color = Color(0xFFFFF3CD),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ticket Submission Form
            Text(
                "ارسال تیکت جدید به کارشناسان پشتیبانی",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ticketTitle,
                onValueChange = { ticketTitle = it },
                label = { Text("موضوع تیکت (مانند: شارژ حساب ۵۰ هزار تومان)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ticketDesc,
                onValueChange = { ticketDesc = it },
                label = { Text("توضیحات درخواست") },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (ticketTitle.isBlank() || ticketDesc.isBlank()) {
                        Toast.makeText(context, "لطفا موضوع و شرح تیکت را پر کنید.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.createSupportTicket(ticketTitle, ticketDesc)
                    Toast.makeText(context, "تیکت شما با موفقیت ثبت شد و در اولویت بررسی قرار گرفت.", Toast.LENGTH_LONG).show()
                    ticketTitle = ""
                    ticketDesc = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055B3)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ثبت تیکت پشتیبانی", color = Color.White, fontSize = 12.sp)
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFF272C35))
            Spacer(modifier = Modifier.height(16.dp))

            // Check roles
            if (isAdmin) {
                Text(
                    "پنل مدیریت تیکت‌های پاسخ‌نشده و شارژ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE040FB),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allTickets.isEmpty()) {
                    Text(
                        "هیچ تیکت پشتیبانی در دیتابیس ثبت نشده است.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    allTickets.forEach { ticket ->
                        var isRepExpanded by remember { mutableStateOf(false) }
                        var replyText by remember { mutableStateOf("") }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E24)),
                            border = BorderStroke(1.dp, if (ticket.isAnswered) Color(0xFF334033) else Color(0xFF5E2E5E)),
                            shape = RoundedCornerShape(12.dp)
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
                                                if (ticket.isAnswered) Color(0xFF1E3A1E) else Color(0xFF3E1D3E),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (ticket.isAnswered) "پاسخ داده شده" else "در انتظار پاسخ ادمین",
                                            color = if (ticket.isAnswered) Color(0xFF81C784) else Color(0xFFF06292),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = ticket.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ارسال‌کننده: ${ticket.senderUsername} (کد کاربر: ${ticket.userId})",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ticket.description,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (ticket.isAnswered) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF142416), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "پاسخ رسمی پشتیبانی (توسط: ${ticket.replierName}):",
                                                    color = Color(0xFF4CAF50),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = ticket.replyMessage ?: "",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick charge button contextually inside ticket response for admins!
                                    Button(
                                        onClick = {
                                            viewModel.awardGiftChapters(ticket.userId, 10)
                                            Toast.makeText(context, "۱۰ کوپن خوانش جهت افزایش آنی شارژ هدیه تخصیص یافت.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("شارژ هدیه ۱۰ چپتر", fontSize = 9.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = { isRepExpanded = !isRepExpanded },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C323C)),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            text = if (isRepExpanded) "بستن کادر" else "ارسال پاسخ متنی",
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isRepExpanded) {
                                    Column(
                                        modifier = Modifier.padding(top = 8.dp),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("پاسخ به تیکت یا شماره کارت در اینجا...") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                if (replyText.isBlank()) {
                                                    Toast.makeText(context, "پاسخ خالی نیست.", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                viewModel.answerSupportTicket(ticket.id, replyText)
                                                Toast.makeText(context, "پاسخ با موفقیت به کاربر فرستاده شد.", Toast.LENGTH_SHORT).show()
                                                replyText = ""
                                                isRepExpanded = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("ارسال پاسخ رسمی ادمین", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "تاریخچه تیکت‌های شما",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (userTickets.isEmpty()) {
                    Text(
                        "تیکتی از سوی شما به ثبت نرسیده است. از کادر بالا تیکت ارسال کنید تا شارژ حساب یا راهنمایی بانکی اعمال شود.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    userTickets.forEach { ticket ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E24)),
                            border = BorderStroke(1.dp, Color(0xFF272C35)),
                            shape = RoundedCornerShape(12.dp)
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
                                                if (ticket.isAnswered) Color(0xFF1E3A1E) else Color(0xFF332000),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (ticket.isAnswered) "پاسخ داده شده" else "در انتظار بررسی پشتیبان",
                                            color = if (ticket.isAnswered) Color(0xFF81C784) else Color(0xFFFFA000),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = ticket.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ticket.description,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (ticket.isAnswered) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF142416), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "پاسخ رسمی پشتیبان (${ticket.replierName}):",
                                                    color = Color(0xFF4CAF50),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = ticket.replyMessage ?: "",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
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
}
