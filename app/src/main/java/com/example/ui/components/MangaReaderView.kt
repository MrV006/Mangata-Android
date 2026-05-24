package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.ChapterItem
import com.example.ui.theme.SlateDarkBackground
import com.google.gson.Gson

@Composable
fun MangaReaderView(
    chapter: ChapterItem,
    mangaTitle: String,
    onCloseReader: () -> Unit
) {
    // Parse the image urls list from gson string
    val imagesList: List<String> = try {
        if (!chapter.imagesJson.isNullOrEmpty()) {
            Gson().fromJson(chapter.imagesJson, Array<String>::class.java).toList()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mangaTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "چپتر ${chapter.chapterNumber} - ${chapter.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = onCloseReader) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        containerColor = SlateDarkBackground
    ) { innerPadding ->
        if (imagesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "هیچ تصویری در این چپتر یافت نشد.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "(ممکن است فایل فشرده فاقد پسوندهای عکس معتبر باشد)",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
            ) {
                items(imagesList) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Manga Page",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}
