package com.ptit.predicttuand20

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ptit.predicttuand20.ui.theme.PredictTuanD20Theme
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object YoloRetrofitClient {
    private const val BASE_URL = "http://100.68.49.61:8080/"

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }).build()
            )
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}

object FasterRetrofitClient {
    private const val BASE_URL = "http://100.68.49.61:8181/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
    }
}


object ApiClient {
    val yolo: PredictAPI by lazy {
        YoloRetrofitClient.retrofit.create(PredictAPI::class.java)
    }

    val faster: PredictAPI by lazy {
        FasterRetrofitClient.retrofit.create(PredictAPI::class.java)
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val scope = rememberCoroutineScope()
            val bitmap = remember { mutableStateOf<Bitmap?>(null) }
            val selectedImageUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
            val isLoading = remember { mutableStateOf(false) }

            val pickerLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    uri?.let {
                        selectedImageUri.value = it
                    } ?: run {
                        selectedImageUri.value = Uri.EMPTY
                    }
                }

            PredictTuanD20Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentColor = Color.White
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "PHÁT HIỆN BỆNH RĂNG SÂU",
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (bitmap.value == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Green,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = {
                                        isLoading.value = true
                                        scope.launch {
                                            try {
                                                val res = ApiClient.faster.predict(
                                                    Request(
                                                        img_base64 = encodeImageToBase64(
                                                            this@MainActivity,
                                                            selectedImageUri.value
                                                        )
                                                    )
                                                )
                                                bitmap.value =
                                                    res.byteStream().use {
                                                        BitmapFactory.decodeStream(it)
                                                    }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                isLoading.value = false
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "Faster R-CNN",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Green,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = {
                                        isLoading.value = true
                                        scope.launch {
                                            try {
                                                val res = ApiClient.yolo.predict(
                                                    Request(
                                                        img_base64 = encodeImageToBase64(
                                                            this@MainActivity,
                                                            selectedImageUri.value
                                                        )
                                                    )
                                                )
                                                bitmap.value =
                                                    res.byteStream().use {
                                                        BitmapFactory.decodeStream(it)
                                                    }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            } finally {
                                                isLoading.value = false
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "YOLOv3",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Green,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    pickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = "Tải ảnh lên",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color.Gray)
                            ) {
                                if (selectedImageUri.value != Uri.EMPTY) {
                                    Image(
                                        bitmap = getImageBitmap(
                                            contentResolver,
                                            selectedImageUri.value
                                        ).asImageBitmap(),
                                        contentDescription = "Image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = Color.White,
                                        )
                                        Text(
                                            text = "Your image",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                        )
                                    }

                                if (isLoading.value){
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.8f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        } else {
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Green,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    bitmap.value = null
                                }
                            ) {
                                Text(
                                    text = "Quay lại",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Image(
                                bitmap = bitmap.value!!.asImageBitmap(),
                                contentDescription = "Image"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PredictTuanD20Theme {
        Greeting("Android")
    }
}

private fun getImageBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap {
    val inputStream = contentResolver.openInputStream(uri)
    val image = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    return image
}

private fun getRequestBodyFromUri(context: Context, uri: Uri): MultipartBody.Part {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "upload_image.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()

    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", file.name, requestFile)
}

fun encodeImageToBase64(context: Context, imageUri: Uri): String {
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    return inputStream?.let {
        val bytes = it.readBytes()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } ?: ""
}