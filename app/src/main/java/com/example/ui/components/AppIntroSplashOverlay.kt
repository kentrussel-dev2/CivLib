package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DmSansFontFamily
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Brand launch intro overlay:
 * - Icon animates in smoothly from nothing
 * - App name transitions: "" -> "CivLib" -> "Civil Library"
 * - Smoothly fades out into the app (or tap anywhere to skip)
 */
@Composable
fun AppIntroSplashOverlay(
    onFinished: () -> Unit
) {
    // 0 = nothing, 1 = "CivLib", 2 = "Civil Library"
    var titleStage by remember { mutableIntStateOf(0) }
    val iconScale = remember { Animatable(0.62f) }
    val iconAlpha = remember { Animatable(0f) }
    val containerAlpha = remember { Animatable(1f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(120)
        // Step 1: Icon enters smoothly
        launch {
            iconAlpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        }
        launch {
            iconScale.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
        delay(320)
        // Step 2: Nothing -> "CivLib"
        titleStage = 1
        delay(760)
        // Step 3: "CivLib" -> "Civil Library"
        titleStage = 2
        launch {
            subtitleAlpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        }
        delay(950)
        // Step 4: Smooth exit fade
        containerAlpha.animateTo(0f, animationSpec = tween(360, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(containerAlpha.value)
            .background(EspressoBg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onFinished()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
                    .size(112.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .border(2.5.dp, SandCream.copy(alpha = 0.85f), CircleShape)
                    .background(EspressoCard),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_library_hero_1795058942930),
                    contentDescription = "CivLib Civil Library Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = titleStage,
                transitionSpec = {
                    (fadeIn(tween(380, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(380, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(tween(240)))
                },
                label = "intro_brand_title"
            ) { stage ->
                val text = when (stage) {
                    1 -> "CivLib"
                    2 -> "Civil Library"
                    else -> ""
                }
                Text(
                    text = text,
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (stage == 2) 36.sp else 40.sp,
                    letterSpacing = if (stage == 2) 0.5.sp else 1.2.sp,
                    color = SandCreamLight,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ENGINEERING & ACADEMIC ARCHIVE",
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                letterSpacing = 2.2.sp,
                color = SageOlive,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}
