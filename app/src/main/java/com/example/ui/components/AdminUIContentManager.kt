package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MovieViewModel

@Composable
fun AdminUIContentManager(viewModel: MovieViewModel) {
    val trnUploaded by viewModel.isTranslatorTestUploaded.collectAsState()
    val clnUploaded by viewModel.isCleanerTestUploaded.collectAsState()
    val typUploaded by viewModel.isTypistTestUploaded.collectAsState()

    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16191E)),
            border = BorderStroke(1.dp, Color(0xFF2D3139)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("مدیریت فایل‌های تست استخدامی", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("تیک‌زدن هر گزینه به معنای آپلود فایل تست برای آن بخش و باز شدن درخواست‌هاست:", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("تست ترجمه", color = Color.White, fontSize = 12.sp)
                    Checkbox(checked = trnUploaded, onCheckedChange = { viewModel.isTranslatorTestUploaded.value = it }, colors = CheckboxDefaults.colors(checkmarkColor = Color.Black, checkedColor = Color(0xFFFFD700)))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("تست کلینر", color = Color.White, fontSize = 12.sp)
                    Checkbox(checked = clnUploaded, onCheckedChange = { viewModel.isCleanerTestUploaded.value = it }, colors = CheckboxDefaults.colors(checkmarkColor = Color.Black, checkedColor = Color(0xFFFFD700)))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Text("تست تایپیست/ادیتور", color = Color.White, fontSize = 12.sp)
                    Checkbox(checked = typUploaded, onCheckedChange = { viewModel.isTypistTestUploaded.value = it }, colors = CheckboxDefaults.colors(checkmarkColor = Color.Black, checkedColor = Color(0xFFFFD700)))
                }
            }
        }
    }
}
