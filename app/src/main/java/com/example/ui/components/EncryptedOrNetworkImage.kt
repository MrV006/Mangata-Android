package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.engine.CipherEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun EncryptedOrNetworkImage(
    url: String,
    mangaId: Int,
    chapterNumber: Int,
    pageNumber: Int,
    isDownloaded: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    compressionActive: Boolean = false
) {
    val context = LocalContext.current
    
    if (isDownloaded) {
        var bitmap by remember(mangaId, chapterNumber, pageNumber) {
            mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
        }
        
        LaunchedEffect(mangaId, chapterNumber, pageNumber) {
            withContext(Dispatchers.IO) {
                val bytes = CipherEngine.loadEncryptedPage(context, mangaId, chapterNumber, pageNumber)
                if (bytes != null) {
                    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bitmap = decoded?.asImageBitmap()
                }
            }
        }
        
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            androidx.compose.foundation.layout.Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color(0xFF00C6FF))
            }
        }
    } else {
        val optimizedUrl = remember(url, compressionActive) {
            if (compressionActive && url.contains("unsplash.com")) {
                var opt = url
                opt = opt.replace(Regex("&w=\\d+"), "")
                    .replace(Regex("w=\\d+&?"), "")
                    .replace(Regex("&q=\\d+"), "")
                    .replace(Regex("q=\\d+&?"), "")
                    .replace(Regex("&fm=\\w+"), "")
                    .replace(Regex("fm=\\w+&?"), "")
                if (opt.contains("?")) {
                    opt + "&w=480&q=40&fm=webp"
                } else {
                    opt + "?w=480&q=40&fm=webp"
                }
            } else {
                url
            }
        }

        val imageRequest = remember(optimizedUrl, compressionActive) {
            val builder = ImageRequest.Builder(context)
                .data(optimizedUrl)
            
            if (compressionActive) {
                builder.diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                    .precision(coil.size.Precision.INEXACT)
                    .size(480, 800)
            } else {
                builder.diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
            }
            builder.build()
        }
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
