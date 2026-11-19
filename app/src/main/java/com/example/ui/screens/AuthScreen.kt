package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.onGoogleSignInClicked
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.EspressoInkMuted
import com.example.ui.theme.NewsreaderFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isAutoChecking by remember { mutableStateOf(true) }
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isAutoChecking = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isAutoChecking = false
            },
            scope = scope
        )
    }

    Surface(
        color = EspressoBg,
        contentColor = SandCreamLight,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular library emblem matching Profile portrait framing
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .border(3.dp, SageOlive, CircleShape)
                        .background(EspressoCard)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_library_hero_1795058942930),
                        contentDescription = "CivLib Civil Engineering Library",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "CivLib",
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = SandCreamLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your personal Civil Engineering college library,\nchronological curriculum & 3D PDF book reader.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = SandCreamMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Warm Sand-Cream feature card
                Surface(
                    color = SandCream,
                    contentColor = EspressoInk,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AuthBenefitRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Chronological Year 1–4 Library",
                            subtitle = "Curated full-length open-access engineering textbooks in sequence"
                        )
                        AuthBenefitRow(
                            icon = Icons.Filled.Bookmark,
                            title = "Personal Bookmarks & Progress",
                            subtitle = "Save your shelf, current pages, and exam notes to your Google account"
                        )
                        AuthBenefitRow(
                            icon = Icons.Filled.CloudDone,
                            title = "Offline PDF Book Reader",
                            subtitle = "Download textbooks once or import local PDFs with 3D page-turn reading"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        color = SandCream,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    )
                }

                Button(
                    onClick = {
                        errorMessage = null
                        isSigningIn = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isSigningIn = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isSigningIn = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isSigningIn = false
                            }
                        )
                    },
                    enabled = !isSigningIn && !isAutoChecking,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandCream,
                        contentColor = EspressoInk,
                        disabledContainerColor = SageOlive,
                        disabledContentColor = EspressoInk
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isSigningIn || isAutoChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EspressoInk,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isAutoChecking) "Checking account…" else "Signing in…",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = EspressoInk
                        )
                    } else {
                        Text(
                            text = "G",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = EspressoInk
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.5.sp,
                            color = EspressoInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Your library profile, bookmarks, and notes stay private to your account.",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 12.sp,
                    color = SandCreamMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthBenefitRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SageOlive.copy(alpha = 0.45f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EspressoInk,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = EspressoInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = EspressoInkMuted
            )
        }
    }
}
