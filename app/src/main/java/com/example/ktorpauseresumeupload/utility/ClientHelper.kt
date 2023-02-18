package com.example.ktorpauseresumeupload.utility

import android.util.Log
import com.example.ktorpauseresumeupload.model.UploadMetaData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay

class ClientHelper
{
    companion object
    {
        private var instance: ClientHelper? = null
        fun createInstance (): ClientHelper
        {
            if (instance == null)
            {
                instance = ClientHelper()
                instance!!.initialize()
            }

            return instance!!
        }
    }

    private var client: HttpClient? = null
    private fun initialize ()
    {
        client = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    suspend fun upload (uploadMetadata: UploadMetaData): String?
    {
        if (uploadMetadata.chunkSize > 0)
        {
            return perPart(uploadMetadata)
        }

        return runRequest(uploadMetadata)
    }

    private suspend fun perPart (uploadMetadata: UploadMetaData): String?
    {
        try
        {
            var downloadPercent: Double
            while (!uploadMetadata.isComplete)
            {
                runRequest(uploadMetadata)
                if (uploadMetadata.currentContentPosition >= uploadMetadata.fileSize)
                {
                    uploadMetadata.isComplete = true
                }

                downloadPercent = uploadMetadata.currentContentPosition / uploadMetadata.fileSize.toDouble() * 100
                if (downloadPercent > 60)
                {
                    Log.d("DEBUG_DATA", "Pause upload for 5s")
                    delay(5000)
                }
            }

            return "Total bytes ${uploadMetadata.currentContentPosition} - file size: ${uploadMetadata.fileSize}"
        }
        catch (e: Exception)
        {
            return null
        }
    }

    private suspend fun runRequest (uploadMetadata: UploadMetaData): String?
    {
        try
        {
            val response = client!!.post(uploadMetadata.url) {
                val content = uploadMetadata.getFileContent()

                headers {
                    val rangeStart = uploadMetadata.currentContentPosition
                    val rangeEnd = rangeStart + uploadMetadata.chunkSize
                    val fileSize = uploadMetadata.fileSize
                    append(HttpHeaders.ContentRange, "bytes ${rangeStart}-${rangeEnd}/${fileSize}")
                }

                setBody(content)
            }

            val responseHeaders = response.headers
            val rangeHeader = responseHeaders[HttpHeaders.Range]
            if (rangeHeader != null)
            {
                val tmp = rangeHeader.split("=")
                if (tmp.size > 1)
                {
                    uploadMetadata.currentContentPosition = tmp[1].split("-")[0].toLong()
                }
            }

            return response.body()
        }
        catch (e: Exception)
        {
            return null
        }
    }
}
















