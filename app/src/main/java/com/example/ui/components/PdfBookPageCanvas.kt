package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaperTheme
import com.example.data.pdf.PdfBookSession
import com.example.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.abs

val PdfParchmentColorFilter: ColorFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            0.96f, 0f, 0f, 0f, 0f,
            0f, 0.93f, 0f, 0f, 0f,
            0f, 0f, 0.85f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

val PdfBlueprintNightInvertFilter: ColorFilter = ColorFilter.colorMatrix(
    ColorMatrix(
        floatArrayOf(
            -0.86f, 0f, 0f, 0f, 232f,
            0f, -0.84f, 0f, 0f, 236f,
            0f, 0f, -0.78f, 0f, 242f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

fun pdfColorFilterForTheme(theme: PaperTheme): ColorFilter? = when (theme) {
    PaperTheme.CRISP -> null
    PaperTheme.PARCHMENT -> PdfParchmentColorFilter
    PaperTheme.BLUEPRINT_DARK -> PdfBlueprintNightInvertFilter
}

/**
 * Renders a single PDF page styled like a physical book leaf with:
 * - Guaranteed race-free rendering via PdfBookSession auto-open
 * - High-DPI re-rendering when zoomed into formulas/diagrams
 * - Optional smart white-margin cropping for scanned textbooks
 * - True 2-finger pinch-to-zoom from 1.0x without blocking 1-finger page-turn swipes
 * - Smart tap zones: left edge -> prev page, right edge -> next page, center -> toggle chrome
 */
@Composable
fun RealPdfBookPageView(
    session: PdfBookSession?,
    sessionReadyVersion: Int,
    pageIndex: Int,
    totalPages: Int,
    paperTheme: PaperTheme,
    backgroundColor: Color,
    accentColor: Color,
    subtleColor: Color,
    cropMargins: Boolean,
    zoomScale: Float,
    panOffset: Offset,
    isActivePage: Boolean,
    isSpreadHalf: Boolean = false,
    onZoomPanChange: (Float, Offset) -> Unit,
    onTapPreviousPage: () -> Unit,
    onTapNextPage: () -> Unit,
    onCenterTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val latestScale by rememberUpdatedState(zoomScale)
    val latestOffset by rememberUpdatedState(panOffset)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Request crisper resolution when zoomed into dense equations or engineering drawings
        val zoomBucket = if (isActivePage && zoomScale >= 1.55f) 2 else 1
        val targetWidthPx = remember(maxWidth, density, zoomBucket, isSpreadHalf) {
            val multiplier = when {
                zoomBucket == 2 -> 2.1f
                isSpreadHalf -> 1.35f
                else -> 1.55f
            }
            with(density) { (maxWidth.toPx() * multiplier).toInt().coerceIn(720, 2100) }
        }

        val renderedBitmap by produceState<Bitmap?>(
            initialValue = null,
            session,
            sessionReadyVersion,
            pageIndex,
            targetWidthPx,
            cropMargins
        ) {
            val active = session ?: return@produceState
            value = active.renderPage(
                pageIndex = pageIndex,
                targetWidthPx = targetWidthPx,
                cropMargins = cropMargins
            )
            if (isActivePage) {
                val neighborTargets = if (isSpreadHalf) {
                    listOf(pageIndex + 1, pageIndex + 2, pageIndex + 3, pageIndex - 1, pageIndex - 2)
                } else {
                    listOf(pageIndex + 1, pageIndex - 1, pageIndex + 2)
                }
                active.prefetchPages(neighborTargets, targetWidthPx, cropMargins)
            }
        }

        val pageFilter = remember(paperTheme) { pdfColorFilterForTheme(paperTheme) }

        val gestureModifier = if (isActivePage) {
            Modifier
                .pointerInput(pageIndex, isSpreadHalf) {
                    detectTapGestures(
                        onTap = { tapPos ->
                            if (latestScale > 1.08f) {
                                onCenterTap()
                            } else {
                                val widthF = size.width.toFloat().coerceAtLeast(1f)
                                val relX = tapPos.x / widthF
                                when {
                                    relX < 0.22f -> onTapPreviousPage()
                                    relX > 0.78f -> onTapNextPage()
                                    else -> onCenterTap()
                                }
                            }
                        },
                        onDoubleTap = { tapPos ->
                            if (latestScale > 1.15f) {
                                onZoomPanChange(1f, Offset.Zero)
                            } else {
                                val targetScale = 2.25f
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val maxPanX = (size.width * (targetScale - 1f)) / 2f
                                val maxPanY = (size.height * (targetScale - 1f)) / 2f
                                val focusOffset = Offset(
                                    x = ((centerX - tapPos.x) * (targetScale - 1f)).coerceIn(-maxPanX, maxPanX),
                                    y = ((centerY - tapPos.y) * (targetScale - 1f)).coerceIn(-maxPanY, maxPanY)
                                )
                                onZoomPanChange(targetScale, focusOffset)
                            }
                        }
                    )
                }
                .pointerInput(pageIndex) {
                    // Multi-touch pinch works from 1.0x; single-finger pan activates only when zoomed in
                    // so 1-finger horizontal drags pass cleanly up to InteractiveBookPageTurner!
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val pressedCount = event.changes.count { it.pressed }
                            if (pressedCount >= 2) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (abs(zoomChange - 1f) > 0.005f || panChange != Offset.Zero) {
                                    val nextScale = (latestScale * zoomChange).coerceIn(1f, 3.8f)
                                    val maxPanX = (size.width * (nextScale - 1f)) / 2f
                                    val maxPanY = (size.height * (nextScale - 1f)) / 2f
                                    val nextOffset = if (nextScale <= 1.02f) {
                                        Offset.Zero
                                    } else {
                                        Offset(
                                            x = (latestOffset.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                                            y = (latestOffset.y + panChange.y).coerceIn(-maxPanY, maxPanY)
                                        )
                                    }
                                    onZoomPanChange(nextScale, nextOffset)
                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) change.consume()
                                    }
                                }
                            } else if (pressedCount == 1 && latestScale > 1.05f) {
                                val panChange = event.calculatePan()
                                if (panChange != Offset.Zero) {
                                    val maxPanX = (size.width * (latestScale - 1f)) / 2f
                                    val maxPanY = (size.height * (latestScale - 1f)) / 2f
                                    val nextOffset = Offset(
                                        x = (latestOffset.x + panChange.x).coerceIn(-maxPanX, maxPanX),
                                        y = (latestOffset.y + panChange.y).coerceIn(-maxPanY, maxPanY)
                                    )
                                    onZoomPanChange(latestScale, nextOffset)
                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) change.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        } else {
            Modifier
        }

        val horizontalInset = if (isSpreadHalf) 4.dp else 10.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureModifier)
                .padding(start = horizontalInset, end = horizontalInset, top = 8.dp, bottom = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            val bmp = renderedBitmap
            if (bmp != null && !bmp.isRecycled) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Book page ${pageIndex + 1} of $totalPages",
                    contentScale = ContentScale.Fit,
                    colorFilter = pageFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (isActivePage) {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                        }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = accentColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Rendering Page ${pageIndex + 1}…",
                        style = MaterialTheme.typography.labelMedium,
                        color = subtleColor
                    )
                }
            }
        }

        // Authentic book running folio page number at bottom of leaf
        Text(
            text = "${pageIndex + 1}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = subtleColor.copy(alpha = 0.72f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}
