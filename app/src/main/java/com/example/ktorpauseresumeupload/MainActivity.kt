package com.example.ktorpauseresumeupload

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ktorpauseresumeupload.model.UploadMetaData
import com.example.ktorpauseresumeupload.ui.theme.KtorPauseResumeUploadTheme
import com.example.ktorpauseresumeupload.utility.ClientHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KtorPauseResumeUploadTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    val uploadMetadata = UploadMetaData(applicationContext, "http://10.0.2.2:8000/upload_file")
                    val clientHelper = ClientHelper.createInstance()
                    val openFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
                        if (it != null)
                        {
                            uploadMetadata.uriFile = it
                            coroutineScope.launch {
                                val response = clientHelper.upload(uploadMetadata)
                                Log.d("DEBUG_DATA", "Request response : ${response}")
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            coroutineScope.launch {
                                openFileLauncher.launch(arrayOf("image/*"))
                            }
                        }) {
                            Text(text = "Upload at once")
                        }

                        Button(onClick = {
                            coroutineScope.launch {
                                uploadMetadata.chunkSize = 1024
                                openFileLauncher.launch(arrayOf("image/*"))
                            }
                        }) {
                            Text(text = "Upload per part")
                        }
                    }
                }
            }
        }
    }
}
