package io.github.takusan23.androidultrahdrapi

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.takusan23.androidultrahdrapi.ui.theme.AndroidUltraHdrApiTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidUltraHdrApiTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    val isEnableUltraHdr = remember { mutableStateOf(false) }

    // 画像を選ぶ
    val photoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri ->
        uri ?: return@rememberLauncherForActivityResult
        bitmap.value = context.contentResolver.openInputStream(uri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    })

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            // Bitmap を表示する ImageView
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                factory = { context -> ImageView(context) },
                update = { imageView -> imageView.setImageBitmap(bitmap.value) }
            )

            // Ultra HDR の表示をするか、Ultra HDR 画像のみ
            if (bitmap.value?.hasGainmap() == true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Ultra HDR を有効にする")
                    Switch(
                        checked = isEnableUltraHdr.value,
                        onCheckedChange = { isEnable ->
                            isEnableUltraHdr.value = isEnable
                            // Activity の設定が必要
                            (context as Activity).window.colorMode = if (isEnable) {
                                ActivityInfo.COLOR_MODE_HDR
                            } else {
                                ActivityInfo.COLOR_MODE_DEFAULT
                            }
                        }
                    )
                }
            } else {
                Text(text = "Ultra HDR 画像ではありません")
            }

            // 選ぶ
            Button(onClick = {
                photoPicker.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text(text = "画像を選ぶ") }

        }
    }
}
