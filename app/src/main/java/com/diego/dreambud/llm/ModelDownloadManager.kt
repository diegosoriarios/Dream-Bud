package com.diego.dreambud.llm

import android.content.Context
import com.diego.dreambud.data.model.LlmModelPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data class Success(val filePath: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class ModelDownloadManager(private val context: Context) {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var isCancelled = false

    fun getModelsDirectory(): File {
        val dir = File(context.filesDir, "models")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun isModelDownloaded(preset: LlmModelPreset): Boolean {
        val file = File(getModelsDirectory(), preset.fileName)
        return file.exists() && file.length() > 0
    }

    fun getDownloadedModelPath(preset: LlmModelPreset): String? {
        val file = File(getModelsDirectory(), preset.fileName)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    suspend fun downloadModelPreset(preset: LlmModelPreset) = withContext(Dispatchers.IO) {
        isCancelled = false
        val targetFile = File(getModelsDirectory(), preset.fileName)
        val tempFile = File(getModelsDirectory(), "${preset.fileName}.tmp")

        try {
            _downloadState.value = DownloadState.Downloading(0, 0, 0)

            val url = URL(preset.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                _downloadState.value = DownloadState.Error("Server returned HTTP $responseCode")
                return@withContext
            }

            val totalBytes = connection.contentLengthLong

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesDownloaded = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        if (isCancelled) {
                            tempFile.delete()
                            _downloadState.value = DownloadState.Idle
                            return@withContext
                        }

                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead

                        val progressPercent = if (totalBytes > 0) {
                            ((bytesDownloaded * 100) / totalBytes).toInt()
                        } else {
                            0
                        }

                        _downloadState.value = DownloadState.Downloading(
                            progressPercent = progressPercent,
                            bytesDownloaded = bytesDownloaded,
                            totalBytes = totalBytes
                        )
                    }
                }
            }

            // Rename temp to target file
            if (tempFile.exists()) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)
            }

            _downloadState.value = DownloadState.Success(targetFile.absolutePath)
        } catch (e: Exception) {
            if (tempFile.exists()) {
                tempFile.delete()
            }
            _downloadState.value = DownloadState.Error(e.localizedMessage ?: "Download failed")
        }
    }

    fun cancelDownload() {
        isCancelled = true
        _downloadState.value = DownloadState.Idle
    }

    fun deleteDownloadedModel(preset: LlmModelPreset): Boolean {
        val file = File(getModelsDirectory(), preset.fileName)
        return if (file.exists()) {
            file.delete()
        } else false
    }
}
