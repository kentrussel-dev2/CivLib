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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.DmSansFontFamily
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.EspressoInkMuted
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Circular library emblem matching Profile portrait framing
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(10.dp, CircleShape)
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CivLib",
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = (-0.5).sp,
                    color = SandCreamLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your personal Civil Engineering library,\nYear 1–4 roadmap & 2-page PDF book reader.",
                    fontFamily = LoraFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = SandCreamMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Prominent Sign in with Google CTA placed clearly above the fold
                if (errorMessage != null) {
                    Surface(
                        color = EspressoCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            fontFamily = DmSansFontFamily,
                            fontSize = 12.5.sp,
                            color = SandCream,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
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
                    enabled = !isSigningIn,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandCream,
                        contentColor = EspressoInk,
                        disabledContainerColor = SageOlive,
                        disabledContentColor = EspressoInk
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EspressoInk,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Signing in with Google…",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = EspressoInk
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Text(
                                text = "G",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = EspressoInk
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isAutoChecking) "Sign in with Google" else "Sign in with Google",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.5.sp,
                            color = EspressoInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Warm Sand-Cream editorial feature card
                Surface(
                    color = SandCream,
                    contentColor = EspressoInk,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            title = "2-Page Landscape Book Reader",
                            subtitle = "Read downloaded or imported PDFs as an open two-page textbook"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Your profile, bookmarks, and notes stay private to your Google account.",
                    fontFamily = DmSansFontFamily,
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
                .size(34.dp)
                .clip(CircleShape)
                .background(SageOlive.copy(alpha = 0.45f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EspressoInk,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = EspressoInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.5.sp,
                color = EspressoInkMuted
            )
        }
    }
}
