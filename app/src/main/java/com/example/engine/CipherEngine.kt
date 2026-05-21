package com.example.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.SecureRandom

object CipherEngine {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    // AES-256 requires a 32-byte key. In a real scenario, retrieve this from Keystore.
    private val SECRET_KEY = SecretKeySpec("MangataSecuredAppKey^32Byte$1234".toByteArray(), "AES")

    suspend fun downloadAndEncryptChapter(
        context: Context,
        mangaId: Int,
        chapterNumber: Int,
        pageUrls: List<String>,
        onProgress: (Int, Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val chapterDir = File(context.filesDir, "encrypted_manga/${mangaId}/${chapterNumber}")
        if (!chapterDir.exists()) {
            chapterDir.mkdirs()
        }

        var downloadedCount = 0
        pageUrls.forEachIndexed { index, url ->
            try {
                // 1. Download
                val imageBytes = java.net.URL(url).readBytes()
                
                // 2. Encrypt
                val cipher = Cipher.getInstance(ALGORITHM)
                val iv = ByteArray(16)
                SecureRandom().nextBytes(iv)
                val ivSpec = IvParameterSpec(iv)
                
                cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, ivSpec)
                val encryptedBytes = cipher.doFinal(imageBytes)
                
                // 3. Save (prepend IV to file)
                val outputFile = File(chapterDir, "page_${index + 1}.enc")
                outputFile.writeBytes(iv + encryptedBytes)
                
                downloadedCount++
                withContext(Dispatchers.Main) {
                    onProgress(downloadedCount, pageUrls.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun loadEncryptedPage(context: Context, mangaId: Int, chapterNumber: Int, pageNumber: Int): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, "encrypted_manga/${mangaId}/${chapterNumber}/page_${pageNumber}.enc")
            if (!file.exists()) return@withContext null

            val fileBytes = file.readBytes()
            if (fileBytes.size < 16) return@withContext null

            val iv = fileBytes.copyOfRange(0, 16)
            val encryptedBytes = fileBytes.copyOfRange(16, fileBytes.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, ivSpec)
            
            cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isChapterDownloaded(context: Context, mangaId: Int, chapterNumber: Int, expectedPages: Int): Boolean {
        val chapterDir = File(context.filesDir, "encrypted_manga/${mangaId}/${chapterNumber}")
        if (!chapterDir.exists()) return false
        
        var count = 0
        for (i in 1..expectedPages) {
            if (File(chapterDir, "page_$i.enc").exists()) {
                count++
            }
        }
        return count > 0 && count == expectedPages
    }
    
    fun getDownloadedChaptersList(context: Context): Map<Int, List<Int>> {
        val rootDir = File(context.filesDir, "encrypted_manga")
        if (!rootDir.exists()) return emptyMap()
        
        val result = mutableMapOf<Int, MutableList<Int>>()
        rootDir.listFiles()?.forEach { mangaDir ->
            val mangaId = mangaDir.name.toIntOrNull()
            if (mangaId != null) {
                val chapters = mutableListOf<Int>()
                mangaDir.listFiles()?.forEach { chapterDir ->
                    chapterDir.name.toIntOrNull()?.let { chapters.add(it) }
                }
                result[mangaId] = chapters
            }
        }
        return result
    }
}
