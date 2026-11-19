package com.example

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.CivLibApp
import com.example.ui.theme.CivLibTheme

class MainActivity : ComponentActivity() {
    private var incomingPdfUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        incomingPdfUri = extractPdfUri(intent)
        setContent {
            CivLibTheme {
                CivLibApp(
                    incomingPdfUri = incomingPdfUri,
                    onConsumeIncomingPdf = { incomingPdfUri = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractPdfUri(intent)?.let { uri ->
            incomingPdfUri = uri
        }
    }

    private fun extractPdfUri(intent: Intent?): Uri? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val uri = intent.data ?: return null
        val type = intent.type?.lowercase().orEmpty()
        val path = uri.toString().lowercase()
        return if (type == "application/pdf" || path.endsWith(".pdf")) uri else null
    }
}
