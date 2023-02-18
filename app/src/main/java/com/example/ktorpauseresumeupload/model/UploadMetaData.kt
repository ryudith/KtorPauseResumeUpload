package com.example.ktorpauseresumeupload.model

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UploadMetaData(
    val context: Context,
    var url: String,
    var uriFile: Uri? = null,
    var isComplete: Boolean = false,
    var chunkSize: Int = 0,
    var currentContentPosition: Long = 0,
    var fileSize: Int = 0,
) {
    suspend fun  getFileContent (): ByteArray?
    {
        uriFile ?: return null

        var content: ByteArray? = null
        val file = context.contentResolver.openInputStream(uriFile!!)
        if (file != null)
        {
            withContext(Dispatchers.IO)
            {
                fileSize = file.available()

                var byteArraySize = chunkSize
                if (byteArraySize == 0)
                {
                    byteArraySize = fileSize
                }
                else if ((currentContentPosition + chunkSize) >= fileSize)
                {
                    byteArraySize = (fileSize - currentContentPosition).toInt()
                }

                content = ByteArray(byteArraySize)
                file.skip(currentContentPosition)
                file.read(content)
                file.close()
            }
        }

        return content
    }
}










