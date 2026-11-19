package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.model.CivilSubject
import com.example.ui.theme.DraftingBrassLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NewsreaderFontFamily
import java.io.File

fun resolveLocalCoverDrawable(coverArtKey: String): Int {
    return when (coverArtKey.lowercase()) {
        "hydraulics" -> R.drawable.img_cover_hydraulics_1795058943040
        "geotech" -> R.drawable.img_cover_geotech_1795058984143
        else -> R.drawable.img_cover_structural_1795058942818
    }
}

/**
 * Editorial Hardcover Monograph Cover.
 * - When a real book cover (extracted PDF Page 1 or repository jacket) is available,
 *   displays it cleanly without muddy gradients or redundant overlaid text—adding only
 *   authentic bookbinding spine indentation, paper fore-edge, and a quiet offline dot.
 * - When no image jacket exists, composes a Swiss / Birkhäuser-inspired clothbound
 *   typographic monograph cover with disciplined grid hairlines and generous negative space.
 */
@Composable
fun HardcoverBookThumbnail(
    title: String,
    author: String,
    courseCode: String,
    subject: CivilSubject,
    coverUrl: String?,
    coverArtKey: String,
    isOfflineAvailable: Boolean,
    modifier: Modifier = Modifier,
    localCoverPath: String? = null,
    totalPages: Int = 0,
    showTextOverlay: Boolean = true,
    showRealCover: Boolean = false,
    elevation: Dp = 4.dp,
    aspectRatio: Float? = 0.70f,
    cornerRadius: Dp = 6.dp
) {
    val coverShape = if (cornerRadius <= 0.dp) {
        RoundedCornerShape(0.dp)
    } else {
        RoundedCornerShape(
            topStart = (cornerRadius / 3f).coerceAtLeast(2.dp),
            bottomStart = (cornerRadius / 3f).coerceAtLeast(2.dp),
            topEnd = cornerRadius,
            bottomEnd = cornerRadius
        )
    }

    val imageModel: Any? = remember(localCoverPath, coverUrl, showRealCover) {
        if (!showRealCover) return@remember null
        val localFile = localCoverPath?.let { File(it) }
        when {
            localFile != null && localFile.exists() -> localFile
            !coverUrl.isNullOrBlank() -> coverUrl
            else -> null
        }
    }

    Box(
        modifier = modifier
            .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier)
            .shadow(
                elevation = elevation,
                shape = coverShape,
                ambientColor = Color.Black.copy(alpha = 0.24f),
                spotColor = Color.Black.copy(alpha = 0.26f)
            )
            .clip(coverShape)
            .background(subject.accentColor)
            .border(0.5.dp, Color.Black.copy(alpha = 0.14f), coverShape)
    ) {
        if (showRealCover && imageModel != null) {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = "$title book cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = {
                    TypographicClothCover(
                        title = title,
                        author = author,
                        courseCode = courseCode,
                        subject = subject,
                        totalPages = totalPages,
                        compact = !showTextOverlay
                    )
                },
                loading = {
                    TypographicClothCover(
                        title = title,
                        author = author,
                        courseCode = courseCode,
                        subject = subject,
                        totalPages = totalPages,
                        compact = !showTextOverlay
                    )
                }
            )
        } else {
            TypographicClothCover(
                title = title,
                author = author,
                courseCode = courseCode,
                subject = subject,
                totalPages = totalPages,
                compact = !showTextOverlay
            )
        }

        // Authentic pressed cloth-binding hinge groove on left edge
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(10.dp)
                .background(
                    Brush.horizontalGradient(
                        0.0f to Color.Black.copy(alpha = 0.34f),
                        0.42f to Color.White.copy(alpha = 0.10f),
                        0.68f to Color.Black.copy(alpha = 0.22f),
                        1.0f to Color.Transparent
                    )
                )
        )

        // Crisp paper block fore-edge trim on right border
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(2.5.dp)
                .background(Color(0xFFF2ECE0).copy(alpha = 0.72f))
        )

        // Quiet offline indicator pip (no noisy floating badge clutter)
        if (isOfflineAvailable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2D8A61))
                    .border(1.dp, Color(0xFFFAF7F0), CircleShape)
            )
        }
    }
}

/**
 * Swiss / Architectural Monograph Clothbound Cover.
 * Pure typographic hierarchy, intentional negative space, and a hairline rule.
 */
@Composable
private fun TypographicClothCover(
    title: String,
    author: String,
    courseCode: String,
    subject: CivilSubject,
    totalPages: Int,
    compact: Boolean
) {
    val creamInk = Color(0xFFF6F1E6)
    val subtleInk = Color(0xFFF6F1E6).copy(alpha = 0.68f)
    val hairline = Color(0xFFF6F1E6).copy(alpha = 0.24f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(subject.accentColor)
    ) {
        if (compact) {
            // Minimalist spine-grade monogram for small list thumbnails
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 11.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = courseCode,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.5.sp,
                    color = DraftingBrassLight
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.75.dp)
                        .background(hairline)
                )
                Text(
                    text = title.take(22),
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 8.5.sp,
                    lineHeight = 10.sp,
                    color = creamInk,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 11.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top classification line
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = courseCode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.8.sp,
                            color = DraftingBrassLight
                        )
                        if (totalPages > 0) {
                            Text(
                                text = "${totalPages}P",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 8.sp,
                                color = subtleInk
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.75.dp)
                            .background(hairline)
                    )
                }

                // Editorial title block anchored in upper-middle with clean breathing room
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = title,
                        fontFamily = NewsreaderFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        color = creamInk,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom author colophon
                Column {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(1.dp)
                            .background(DraftingBrassLight.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = author,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        letterSpacing = 0.3.sp,
                        color = subtleInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

fun Modifier.draftingGridBackground(lineColor: Color, stepDp: Dp = 24.dp): Modifier =
    this.drawBehind {
        val stepPx = stepDp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.75f
            )
            x += stepPx
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.75f
            )
            y += stepPx
        }
    }
