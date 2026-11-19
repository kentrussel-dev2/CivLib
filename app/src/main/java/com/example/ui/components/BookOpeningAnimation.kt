package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookEntity
import com.example.data.model.CivilSubject
import com.example.ui.theme.DraftingBrassLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NewsreaderFontFamily
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentInk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Full-screen 3D Hardcover Book Opening Animation.
 * Swings the book front cover smoothly around its left spine hinge (0 -> -132 deg)
 * while lifting and fanning the first paper leaf, then reveals the real open book surface.
 */
@Composable
fun BookOpeningOverlay(
    book: BookEntity,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    val density = LocalDensity.current.density

    val coverAngle = remember { Animatable(0f) }
    val innerLeafAngle = remember { Animatable(0f) }
    val bookScale = remember { Animatable(0.86f) }
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(book.id) {
        launch {
            bookScale.animateTo(
                targetValue = 1.08f,
                animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
            )
        }
        delay(80)
        launch {
            coverAngle.animateTo(
                targetValue = -132f,
                animationSpec = tween(durationMillis = 580, easing = FastOutSlowInEasing)
            )
        }
        delay(190)
        launch {
            innerLeafAngle.animateTo(
                targetValue = -105f,
                animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing)
            )
        }
        delay(440)
        launch {
            bookScale.animateTo(
                targetValue = 1.22f,
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            )
        }
        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        )
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value }
            .background(Color(0xFF0E1824).copy(alpha = 0.94f))
            .draftingGridBackground(Color.White.copy(alpha = 0.045f), stepDp = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(248.dp)
                .height(352.dp)
                .graphicsLayer {
                    scaleX = bookScale.value
                    scaleY = bookScale.value
                }
        ) {
            // Back cover & stacked paper block underneath
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(18.dp, RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                    .background(ParchmentBg)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD2C7B2),
                        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp)
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Black.copy(alpha = 0.22f),
                                1f to Color.Transparent
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${book.courseCode} · ${subject.shortName.uppercase()}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = subject.accentColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = book.title,
                        fontFamily = NewsreaderFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = ParchmentInk,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(1.5.dp)
                            .background(DraftingBrassLight)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.author,
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 12.sp,
                        color = ParchmentInk.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = buildString {
                            if (book.totalPages > 0) append("${book.totalPages} PAGES · ")
                            append("ISBN ${book.isbn}")
                        },
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = ParchmentInk.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(Color(0xFFE7DFCC))
                ) {
                    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFC9BEA8)))
                    Spacer(modifier = Modifier.width(1.5.dp))
                    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFFC9BEA8)))
                }
            }

            // First fanning flyleaf page
            if (innerLeafAngle.value > -90f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            rotationY = innerLeafAngle.value
                            cameraDistance = 14f * density
                        }
                        .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                        .background(Color(0xFFF9F4E8))
                ) {
                    val leafShade = (abs(innerLeafAngle.value) / 90f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0f to Color.Black.copy(alpha = 0.14f * leafShade),
                                    0.4f to Color.White.copy(alpha = 0.18f * leafShade),
                                    1f to Color.Black.copy(alpha = 0.20f * leafShade)
                                )
                            )
                    )
                }
            }

            // Front Hardcover hinging open around left spine
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        rotationY = coverAngle.value
                        cameraDistance = 14f * density
                    }
            ) {
                if (coverAngle.value > -90f) {
                    HardcoverBookThumbnail(
                        title = book.title,
                        author = book.author,
                        courseCode = book.courseCode,
                        subject = subject,
                        coverUrl = book.coverUrl,
                        localCoverPath = book.localCoverPath,
                        coverArtKey = book.coverArtKey,
                        totalPages = book.totalPages,
                        isOfflineAvailable = book.isOfflineAvailable,
                        modifier = Modifier.fillMaxSize(),
                        elevation = 12.dp
                    )
                    val tiltFraction = (abs(coverAngle.value) / 90f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0f to Color.Black.copy(alpha = 0.08f * tiltFraction),
                                    0.6f to Color.Black.copy(alpha = 0.28f * tiltFraction),
                                    1f to Color.Black.copy(alpha = 0.48f * tiltFraction)
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
                            .background(subject.accentColor.copy(alpha = 0.92f))
                    )
                }
            }
        }
    }
}

/**
 * Interactive 3D Single-Page Book Surface.
 * Renders the page inside a realistic hardcover book binding with:
 * - Progress-proportional stacked paper edges on left & right
 * - True 3D spine-hinge page turns in BOTH forward and backward directions
 * - Live finger-tracking drag + smooth programmatic page flip animations
 */
@Composable
fun InteractiveBookPageTurner(
    currentPageIndex: Int,
    pageCount: Int,
    paperColor: Color,
    spineBorderColor: Color,
    hardcoverBoardColor: Color,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    allowHorizontalSwipe: Boolean = true,
    pageContent: @Composable (pageIndex: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    // flipProgress: 0f = resting; -1f = forward turn complete; +1f = backward turn complete
    val flipProgress = remember { Animatable(0f) }
    var displayedPage by remember { mutableIntStateOf(currentPageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentPageIndex, pageCount) {
        val clampedTarget = currentPageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        if (clampedTarget != displayedPage) {
            val diff = clampedTarget - displayedPage
            if (diff == 1) {
                flipProgress.animateTo(
                    targetValue = -1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                displayedPage = clampedTarget
                flipProgress.snapTo(0f)
            } else if (diff == -1) {
                flipProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                displayedPage = clampedTarget
                flipProgress.snapTo(0f)
            } else {
                displayedPage = clampedTarget
                flipProgress.snapTo(0f)
            }
        }
    }

    val progress = flipProgress.value
    val isFlippingForward = progress < -0.005f
    val isFlippingBackward = progress > 0.005f
    val readFraction = (displayedPage.toFloat() / (pageCount - 1).coerceAtLeast(1)).coerceIn(0f, 1f)

    val swipeModifier = if (allowHorizontalSwipe) {
        Modifier.pointerInput(displayedPage, pageCount) {
            detectHorizontalDragGestures(
                onDragStart = { dragAccumulator = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    val widthPx = size.width.toFloat().coerceAtLeast(1f)
                    dragAccumulator += dragAmount
                    val rawRatio = (dragAccumulator / (widthPx * 0.68f)).coerceIn(-1f, 1f)
                    val clamped = when {
                        rawRatio < 0f && displayedPage >= pageCount - 1 -> rawRatio * 0.14f
                        rawRatio > 0f && displayedPage <= 0 -> rawRatio * 0.14f
                        else -> rawRatio
                    }
                    scope.launch { flipProgress.snapTo(clamped) }
                },
                onDragEnd = {
                    val currentVal = flipProgress.value
                    scope.launch {
                        when {
                            currentVal < -0.18f && displayedPage < pageCount - 1 -> {
                                val nextIdx = displayedPage + 1
                                flipProgress.animateTo(
                                    targetValue = -1f,
                                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                                )
                                displayedPage = nextIdx
                                flipProgress.snapTo(0f)
                                onPageChanged(nextIdx)
                            }
                            currentVal > 0.18f && displayedPage > 0 -> {
                                val prevIdx = displayedPage - 1
                                flipProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                                )
                                displayedPage = prevIdx
                                flipProgress.snapTo(0f)
                                onPageChanged(prevIdx)
                            }
                            else -> {
                                flipProgress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                    }
                },
                onDragCancel = {
                    scope.launch {
                        flipProgress.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                    }
                }
            )
        }
    } else {
        Modifier
    }

    // Outer hardcover board resting on the reading surface
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(12.dp, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp))
            .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 14.dp, bottomEnd = 14.dp))
            .background(hardcoverBoardColor)
            .padding(start = 5.dp, top = 4.dp, bottom = 4.dp, end = 6.dp)
            .then(swipeModifier)
    ) {
        // Base layer: reveals next page when flipping forward, or stays on current page while previous folds back
        if (isFlippingForward && displayedPage + 1 < pageCount) {
            BookSheetFrame(
                paperColor = paperColor,
                spineBorderColor = spineBorderColor,
                readFraction = readFraction,
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.runtime.key(displayedPage + 1) {
                    pageContent(displayedPage + 1)
                }
                val shadowAlpha = (1f - abs(progress)).coerceIn(0f, 1f) * 0.34f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Black.copy(alpha = shadowAlpha),
                                0.52f to Color.Black.copy(alpha = shadowAlpha * 0.38f),
                                1f to Color.Transparent
                            )
                        )
                )
            }
        } else {
            BookSheetFrame(
                paperColor = paperColor,
                spineBorderColor = spineBorderColor,
                readFraction = readFraction,
                modifier = Modifier.fillMaxSize()
            ) {
                androidx.compose.runtime.key(displayedPage) {
                    pageContent(displayedPage)
                }
                if (isFlippingBackward) {
                    val coverShadow = progress.coerceIn(0f, 1f) * 0.30f
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0f to Color.Black.copy(alpha = coverShadow),
                                    0.6f to Color.Black.copy(alpha = coverShadow * 0.3f),
                                    1f to Color.Transparent
                                )
                            )
                    )
                }
            }
        }

        // Turning leaf layer
        if (isFlippingForward) {
            val angleY = progress * 112f // 0 -> -112 deg around left spine
            val bendAmount = abs(progress).coerceIn(0f, 1f)
            if (angleY > -90f) {
                BookSheetFrame(
                    paperColor = paperColor,
                    spineBorderColor = spineBorderColor,
                    readFraction = readFraction,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            rotationY = angleY
                            cameraDistance = 16f * density
                        }
                ) {
                    androidx.compose.runtime.key(displayedPage) {
                        pageContent(displayedPage)
                    }
                    PageBendLightingOverlay(bendAmount = bendAmount)
                }
            }
        } else if (isFlippingBackward && displayedPage - 1 >= 0) {
            // Previous page unfolds smoothly from left spine (-102 deg -> 0 deg) onto the book!
            val unfoldAngle = (1f - progress.coerceIn(0f, 1f)) * -102f
            val bendAmount = (1f - progress).coerceIn(0f, 1f)
            if (unfoldAngle > -90f) {
                BookSheetFrame(
                    paperColor = paperColor,
                    spineBorderColor = spineBorderColor,
                    readFraction = readFraction,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0.5f)
                            rotationY = unfoldAngle
                            cameraDistance = 16f * density
                        }
                ) {
                    androidx.compose.runtime.key(displayedPage - 1) {
                        pageContent(displayedPage - 1)
                    }
                    PageBendLightingOverlay(bendAmount = bendAmount)
                }
            }
        }
    }
}

/**
 * Interactive 3D Two-Page Open Book Spread Turner.
 * Displays Left Page + Center Binding Gutter + Right Page inside an open hardcover frame,
 * and flips 2 pages at a time across the center spine with 3D rotationY and zero jitter.
 */
@Composable
fun InteractiveOpenBookSpreadTurner(
    leftPageIndex: Int,
    pageCount: Int,
    paperColor: Color,
    spineBorderColor: Color,
    hardcoverBoardColor: Color,
    onSpreadChanged: (newLeftPageIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    allowHorizontalSwipe: Boolean = true,
    pageContent: @Composable (pageIndex: Int, isRightPage: Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    val targetNormalizedLeft = ((leftPageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))) / 2) * 2
    val flipProgress = remember { Animatable(0f) }
    var displayedLeft by remember { mutableIntStateOf(targetNormalizedLeft) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(targetNormalizedLeft, pageCount) {
        if (targetNormalizedLeft != displayedLeft) {
            val diff = targetNormalizedLeft - displayedLeft
            if (diff == 2) {
                flipProgress.animateTo(
                    targetValue = -1f,
                    animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)
                )
                displayedLeft = targetNormalizedLeft
                flipProgress.snapTo(0f)
            } else if (diff == -2) {
                flipProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)
                )
                displayedLeft = targetNormalizedLeft
                flipProgress.snapTo(0f)
            } else {
                displayedLeft = targetNormalizedLeft
                flipProgress.snapTo(0f)
            }
        }
    }

    val hasNextSpread = displayedLeft + 2 < pageCount
    val hasPrevSpread = displayedLeft - 2 >= 0

    val progress = flipProgress.value
    val isFlippingForward = progress < -0.005f && hasNextSpread
    val isFlippingBackward = progress > 0.005f && hasPrevSpread

    val swipeModifier = if (allowHorizontalSwipe) {
        Modifier.pointerInput(displayedLeft, pageCount) {
            detectHorizontalDragGestures(
                onDragStart = { dragAccumulator = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    val widthPx = size.width.toFloat().coerceAtLeast(1f)
                    dragAccumulator += dragAmount
                    val rawRatio = (dragAccumulator / (widthPx * 0.55f)).coerceIn(-1f, 1f)
                    val clamped = when {
                        rawRatio < 0f && !hasNextSpread -> rawRatio * 0.12f
                        rawRatio > 0f && !hasPrevSpread -> rawRatio * 0.12f
                        else -> rawRatio
                    }
                    scope.launch { flipProgress.snapTo(clamped) }
                },
                onDragEnd = {
                    val v = flipProgress.value
                    scope.launch {
                        when {
                            v < -0.18f && hasNextSpread -> {
                                val nextLeft = displayedLeft + 2
                                flipProgress.animateTo(-1f, tween(190, easing = FastOutSlowInEasing))
                                displayedLeft = nextLeft
                                flipProgress.snapTo(0f)
                                onSpreadChanged(nextLeft)
                            }
                            v > 0.18f && hasPrevSpread -> {
                                val prevLeft = displayedLeft - 2
                                flipProgress.animateTo(1f, tween(190, easing = FastOutSlowInEasing))
                                displayedLeft = prevLeft
                                flipProgress.snapTo(0f)
                                onSpreadChanged(prevLeft)
                            }
                            else -> {
                                flipProgress.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                            }
                        }
                    }
                },
                onDragCancel = {
                    scope.launch {
                        flipProgress.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                    }
                }
            )
        }
    } else {
        Modifier
    }

    // Open Hardcover Book Frame
    Box(
        modifier = modifier
            .fillMaxSize()
            .shadow(14.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(hardcoverBoardColor)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(swipeModifier)
    ) {
        // Static base spread
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(paperColor)
        ) {
            // Left Half of Spread
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val baseLeftIdx = if (isFlippingBackward) displayedLeft - 2 else displayedLeft
                if (baseLeftIdx in 0 until pageCount) {
                    androidx.compose.runtime.key(baseLeftIdx) {
                        pageContent(baseLeftIdx, false)
                    }
                }
                // Left outer fore-edge lines
                ForeEdgePaperStack(
                    spineBorderColor = spineBorderColor,
                    isRightEdge = false,
                    thicknessDp = 5,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                // Inner center spine shadow (right side of left page)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(22.dp)
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Transparent,
                                0.7f to Color.Black.copy(alpha = 0.08f),
                                1f to Color.Black.copy(alpha = 0.22f)
                            )
                        )
                )
            }

            // Center Book Spine Crease Divider
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color.Black.copy(alpha = 0.28f))
            )

            // Right Half of Spread
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val baseRightIdx = if (isFlippingForward) displayedLeft + 3 else displayedLeft + 1
                if (baseRightIdx in 0 until pageCount) {
                    androidx.compose.runtime.key(baseRightIdx) {
                        pageContent(baseRightIdx, true)
                    }
                } else {
                    // Clean inner endpaper when book ends on an odd page
                    Box(modifier = Modifier.fillMaxSize().background(paperColor.copy(alpha = 0.92f)))
                }
                // Inner center spine shadow (left side of right page)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(22.dp)
                        .background(
                            Brush.horizontalGradient(
                                0f to Color.Black.copy(alpha = 0.20f),
                                0.35f to Color.Black.copy(alpha = 0.07f),
                                1f to Color.Transparent
                            )
                        )
                )
                // Right outer fore-edge lines
                ForeEdgePaperStack(
                    spineBorderColor = spineBorderColor,
                    isRightEdge = true,
                    thicknessDp = 5,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        // Animated 3D Center-Hinge Turning Page for Forward Flip
        if (isFlippingForward) {
            val frac = abs(progress).coerceIn(0f, 1f) // 0 -> 1
            if (frac < 0.5f) {
                // First half: Right page lifts from 0 deg to -90 deg around its left edge (center spine)
                val angle = -(frac * 2f) * 90f
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                rotationY = angle
                                cameraDistance = 18f * density
                            }
                            .background(paperColor)
                    ) {
                        if (displayedLeft + 1 < pageCount) {
                            androidx.compose.runtime.key(displayedLeft + 1) {
                                pageContent(displayedLeft + 1, true)
                            }
                        }
                        PageBendLightingOverlay(bendAmount = frac * 2f)
                    }
                }
            } else {
                // Second half: Next spread's left page lands from +90 deg down to 0 deg on the left half
                val angle = (1f - (frac - 0.5f) * 2f) * 90f
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                rotationY = angle
                                cameraDistance = 18f * density
                            }
                            .background(paperColor)
                    ) {
                        if (displayedLeft + 2 < pageCount) {
                            androidx.compose.runtime.key(displayedLeft + 2) {
                                pageContent(displayedLeft + 2, false)
                            }
                        }
                        PageBendLightingOverlay(bendAmount = (1f - frac) * 2f)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        } else if (isFlippingBackward) {
            val frac = progress.coerceIn(0f, 1f) // 0 -> 1
            if (frac < 0.5f) {
                // First half: Left page lifts from 0 deg to +90 deg around its right edge (center spine)
                val angle = (frac * 2f) * 90f
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(1f, 0.5f)
                                rotationY = angle
                                cameraDistance = 18f * density
                            }
                            .background(paperColor)
                    ) {
                        if (displayedLeft in 0 until pageCount) {
                            androidx.compose.runtime.key(displayedLeft) {
                                pageContent(displayedLeft, false)
                            }
                        }
                        PageBendLightingOverlay(bendAmount = frac * 2f)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                // Second half: Previous spread's right page lands from -90 deg to 0 deg on the right half
                val angle = -(1f - (frac - 0.5f) * 2f) * 90f
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                rotationY = angle
                                cameraDistance = 18f * density
                            }
                            .background(paperColor)
                    ) {
                        val prevRight = displayedLeft - 1
                        if (prevRight in 0 until pageCount) {
                            androidx.compose.runtime.key(prevRight) {
                                pageContent(prevRight, true)
                            }
                        }
                        PageBendLightingOverlay(bendAmount = (1f - frac) * 2f)
                    }
                }
            }
        }
    }
}

@Composable
private fun PageBendLightingOverlay(bendAmount: Float) {
    val clamped = bendAmount.coerceIn(0f, 1f)
    if (clamped <= 0.01f) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    0f to Color.Black.copy(alpha = 0.18f * clamped),
                    0.35f to Color.White.copy(alpha = 0.12f * clamped),
                    0.82f to Color.Black.copy(alpha = 0.15f * clamped),
                    1f to Color.Black.copy(alpha = 0.30f * clamped)
                )
            )
    )
}

@Composable
private fun BookSheetFrame(
    paperColor: Color,
    spineBorderColor: Color,
    readFraction: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val rightThickness = (3 + ((1f - readFraction) * 4f).toInt()).coerceIn(2, 7)
    val leftThickness = (2 + (readFraction * 3f).toInt()).coerceIn(2, 5)

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
            .clip(RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp))
            .background(paperColor)
    ) {
        content()

        // Inner bookbinding spine crease shadow + left stack hint
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(16.dp)
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.18f),
                        0.45f to Color.Black.copy(alpha = 0.06f),
                        1f to Color.Transparent
                    )
                )
        )
        if (readFraction > 0.02f) {
            ForeEdgePaperStack(
                spineBorderColor = spineBorderColor,
                isRightEdge = false,
                thicknessDp = leftThickness,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }

        // Stacked page fore-edge lines on right border proportional to remaining pages
        ForeEdgePaperStack(
            spineBorderColor = spineBorderColor,
            isRightEdge = true,
            thicknessDp = rightThickness,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun ForeEdgePaperStack(
    spineBorderColor: Color,
    isRightEdge: Boolean,
    thicknessDp: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .width(thicknessDp.dp)
            .background(spineBorderColor.copy(alpha = 0.25f))
    ) {
        if (isRightEdge) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(spineBorderColor.copy(alpha = 0.50f))
            )
            if (thicknessDp >= 4) {
                Spacer(modifier = Modifier.width(1.5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(spineBorderColor.copy(alpha = 0.36f))
                )
            }
            if (thicknessDp >= 6) {
                Spacer(modifier = Modifier.width(1.5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(spineBorderColor.copy(alpha = 0.26f))
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(spineBorderColor.copy(alpha = 0.38f))
            )
            if (thicknessDp >= 4) {
                Spacer(modifier = Modifier.width(1.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(spineBorderColor.copy(alpha = 0.26f))
                )
            }
        }
    }
}
