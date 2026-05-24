package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MangaItem
import com.example.ui.MangaViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboard(
    viewModel: MangaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val mangas by viewModel.mangas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedManga by remember { mutableStateOf<MangaItem?>(null) }
    var chapterNumberText by remember { mutableStateOf("") }
    var chapterTitleText by remember { mutableStateOf("") }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val zipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = "chapter_package.zip"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پنل آپلود زیپ و مأموریت‌ها دستاندرکاران") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "راهنمای آپلود فایل مانهوا فشرده",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "فایل فشرده آپلودی شما مستقیماً در سرور وردپرس از حالت فشرده خارج شده و صفحات آن در ریدر قرار می‌گیرد. این پنل به دستاندرکاران متصل به این مانهوا محدود شده است.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // 1. Selector of Manhwas
            item {
                Text(
                    text = "۱. مانهوای مربوطه را انتخاب کنید:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (mangas.isEmpty()) {
                item {
                    Text(
                        text = "هیچ مانهوایی ثبت نشده است.",
                        modifier = Modifier.padding(8.dp),
                        color = Color.LightGray
                    )
                }
            } else {
                items(mangas) { manga ->
                    val isSelected = selectedManga?.id == manga.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedManga = manga },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) borderStrokeFor(MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = manga.title, fontWeight = FontWeight.Bold)
                                Text(text = manga.description, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                            }
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Forms
            item {
                if (selectedManga != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "۲. اطلاعات چپتر جدید:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = chapterNumberText,
                            onValueChange = { chapterNumberText = it },
                            label = { Text("شماره چپتر (مثلاً 1 یا 14.5)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = chapterTitleText,
                            onValueChange = { chapterTitleText = it },
                            label = { Text("عنوان چپتر (مثلا: نبرد پایانی)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    zipLauncher.launch("application/zip")
                                }
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (selectedFileUri == null) {
                                    Text("انتخاب فایل ZIP مانهوا", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("(تصاویر بایستی به ترتیب عددی باشند: 1.jpg , 2.jpg)", style = MaterialTheme.typography.labelMedium)
                                } else {
                                    Text("فایل فشرده چپتر آماده است", fontWeight = FontWeight.Bold, color = Color.Green)
                                    Text(selectedFileName, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val mangaId = selectedManga?.id ?: return@Button
                                val chapNum = chapterNumberText.toDoubleOrNull()
                                val uri = selectedFileUri

                                if (chapNum == null) {
                                    statusMessage = "شماره چپتر نامعتبر است."
                                    return@Button
                                }
                                if (uri == null) {
                                    statusMessage = "لطفا فایل ZIP را انتخاب کنید."
                                    return@Button
                                }

                                val tempZip = createTempFileFromUri(context, uri, "payload.zip")
                                if (tempZip != null) {
                                    viewModel.uploadChapterZip(
                                        tempZip,
                                        mangaId,
                                        chapNum,
                                        chapterTitleText.ifEmpty { "بدون عنوان" }
                                    )
                                    statusMessage = "فایل برای سرور ارسال شد. پردازش فشرده‌سازی شروع شد..."
                                } else {
                                    statusMessage = "خطا در کپی کردن محتوای فایل مانهوا."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("شروع استخراج و بارگذاری زنده زیپ در ریدر", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (statusMessage.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                            Text(text = statusMessage, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStrokeFor(color: Color) = androidx.compose.foundation.BorderStroke(2.dp, color)

private fun createTempFileFromUri(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val tempFile = File(context.cacheDir, fileName)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val outputStream = java.io.FileOutputStream(tempFile)
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.close()
        inputStream.close()
        tempFile
    } catch (e: Exception) {
        null
    }
}
