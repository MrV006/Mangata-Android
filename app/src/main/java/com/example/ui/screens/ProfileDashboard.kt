package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.ui.MovieViewModel
import com.example.ui.components.EditProfileDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileDashboard(viewModel: MovieViewModel) {
    val currentUserAccount by viewModel.currentUserAccount.collectAsState()

    var loginMode by remember { mutableStateOf(true) } // true: Login, false: Register
    var uUsername by remember { mutableStateOf("") }
    var uPassword by remember { mutableStateOf("") }
    var uDisplayName by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf("") }
    var authSuccess by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (currentUserAccount != null) {
            val user = currentUserAccount!!
            Text(
                "پروفایل کاربری من",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, size.height * 0.7f)
                            quadraticBezierTo(
                                size.width / 2, size.height * 1.1f,
                                0f, size.height * 0.7f
                            )
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0072FF), Color(0xFF00C6FF))
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(30.dp))

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF14171C),
                            border = BorderStroke(3.dp, Color(0xFF1A1D24)),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.displayName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "@${user.username}",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = when(user.role) {
                                "SUPER_ADMIN" -> Color(0xFFE53935)
                                "DEPT_ADMIN" -> Color(0xFFE64A19)
                                "STAFF" -> Color(0xFF1E88E5)
                                else -> Color(0xFF4CAF50)
                            }.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, when(user.role) {
                                "SUPER_ADMIN" -> Color(0xFFFF5252)
                                "DEPT_ADMIN" -> Color(0xFFFF7043)
                                "STAFF" -> Color(0xFF40C4FF)
                                else -> Color(0xFF69F0AE)
                            }.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (user.role.contains("ADMIN")) Icons.Default.Verified else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = when(user.role) {
                                        "SUPER_ADMIN" -> Color(0xFFFF5252)
                                        "DEPT_ADMIN" -> Color(0xFFFF7043)
                                        "STAFF" -> Color(0xFF40C4FF)
                                        else -> Color(0xFF69F0AE)
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = user.subRole,
                                    color = when(user.role) {
                                        "SUPER_ADMIN" -> Color(0xFFFF5252)
                                        "DEPT_ADMIN" -> Color(0xFFFF7043)
                                        "STAFF" -> Color(0xFF40C4FF)
                                        else -> Color(0xFF69F0AE)
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("موجودی کیف پول", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${user.walletRial} تومان", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            androidx.compose.material3.VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFF272C35))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("اعتبار فصول هدیه", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF00C6FF), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${user.walletGiftChapters} چپتر", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        var showEditDialog by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showEditDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14171C)),
                                border = BorderStroke(1.dp, Color(0xFF00C6FF).copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00C6FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ویرایش اطلاعات", color = Color(0xFF00C6FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    viewModel.switchUser(6)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14171C)),
                                border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("خروج", color = Color(0xFFFF5252), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (showEditDialog) {
                            EditProfileDialog(
                                user = user,
                                onDismiss = { showEditDialog = false },
                                onSave = { newName, newUsername, newPass, newPic ->
                                    viewModel.updateUserProfile(user, newName, newUsername, newPass, newPic)
                                    showEditDialog = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Not Logged In
            Text(
                "ورود یا ثبت‌نام",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                "برای دسترسی به پنل‌های پیشرفته با توجه به نقش خود از درگاه امن زیر وارد شوید.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14171C)),
                border = BorderStroke(1.dp, Color(0xFF272C35)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { loginMode = false; authError = ""; authSuccess = "" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!loginMode) Color(0xFF1A2D42) else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f),
                            border = if (!loginMode) BorderStroke(1.dp, Color(0xFF00C6FF)) else null,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تولید حساب (ثبت‌نام)", color = if (!loginMode) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { loginMode = true; authError = ""; authSuccess = "" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (loginMode) Color(0xFF1A2D42) else Color.Transparent
                            ),
                            modifier = Modifier.weight(1f),
                            border = if (loginMode) BorderStroke(1.dp, Color(0xFF00C6FF)) else null,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ورود با گذرواژه", color = if (loginMode) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (authError.isNotBlank()) {
                        Text(authError, color = Color(0xFFFF5252), fontSize = 11.sp, modifier = Modifier.padding(bottom = 10.dp), fontWeight = FontWeight.Medium)
                    }
                    if (authSuccess.isNotBlank()) {
                        Text(authSuccess, color = Color(0xFF69F0AE), fontSize = 11.sp, modifier = Modifier.padding(bottom = 10.dp), fontWeight = FontWeight.Medium)
                    }

                    if (loginMode) {
                        Text("نام کاربری انگلیسی خود را وارد کنید (*)", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = uUsername,
                            onValueChange = { uUsername = it },
                            placeholder = { Text("god_admin یا guest_user") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("گذرواژه امن خود را وارد کنید (*)", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = uPassword,
                            onValueChange = { uPassword = it },
                            placeholder = { Text("مثال: 123456") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (uUsername.isBlank() || uPassword.isBlank()) {
                                    authError = "نام کاربری و کلمه‌ی عبور الزامی است."
                                } else {
                                    viewModel.loginUser(
                                        usernameInput = uUsername.trim(),
                                        passwordInput = uPassword,
                                        onSuccess = {
                                            authSuccess = "ورود با موفقیت انجام شد!"
                                            authError = ""
                                            uUsername = ""
                                            uPassword = ""
                                        },
                                        onError = {
                                            authError = it
                                            authSuccess = ""
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ورود امن به پنل", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("نام کاربری انگلیسی جدید (*)", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = uUsername,
                            onValueChange = { uUsername = it },
                            placeholder = { Text("مثال: sina_cleaner") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("نام و نام خانوادگی فارسی (*)", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = uDisplayName,
                            onValueChange = { uDisplayName = it },
                            placeholder = { Text("مثال: سینا شاهین") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("گذرواژه انتخابی شما (*)", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = uPassword,
                            onValueChange = { uPassword = it },
                            placeholder = { Text("حداقل ۵ کاراکتر") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (uUsername.isBlank() || uDisplayName.isBlank() || uPassword.length < 5) {
                                    authError = "نام کاربری، نام واقعی و گذرواژه (حداقل ۵ کاراکتر) الزامی است."
                                } else {
                                    viewModel.registerNewUser(
                                        username = uUsername.trim(),
                                        displayName = uDisplayName.trim(),
                                        passwordInput = uPassword,
                                        role = "NORMAL_USER",
                                        subRole = "کاربر عادی",
                                        onSuccess = {
                                            authSuccess = "حساب جدید با موفقیت ایجاد و فعال شد!"
                                            authError = ""
                                            uUsername = ""
                                            uDisplayName = ""
                                            uPassword = ""
                                        },
                                        onError = {
                                            authError = it
                                            authSuccess = ""
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ثبت‌نام و عضویت متمرکز", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
