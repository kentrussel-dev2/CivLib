package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CivilSubject
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.NavBarBottomBevel
import com.example.ui.theme.NavBarCenterCream
import com.example.ui.theme.NavBarTopBevel
import com.example.ui.theme.SageOliveDark

/**
 * Soft-beveled warm Sand-Cream bottom navigation bar matching the reference design 1-to-1.
 * Respects system navigation bar insets so gesture pills never clip the icons.
 */
@Composable
fun CreamBevelBottomNavBar(
    items: List<NavBarIconSpec>,
    selectedRoute: String?,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, barShape)
            .clip(barShape)
            .background(
                Brush.verticalGradient(
                    0.0f to NavBarTopBevel,
                    0.22f to NavBarCenterCream,
                    0.78f to NavBarCenterCream,
                    1.0f to NavBarBottomBevel
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedRoute == item.route
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) EspressoInk.copy(alpha = 0.11f)
                            else Color.Transparent
                        )
                        .clickable { onSelectRoute(item.route) }
                        .testTag(item.testTag)
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) EspressoInk else EspressoInk.copy(alpha = 0.72f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

data class NavBarIconSpec(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

/**
 * Flowing organic contour ribbon curves drawn on the right edge of the cream featured card
 * (1-to-1 recreation of the signature wavy background motif in the center screen mockup).
 */
@Composable
fun FeaturedBannerContourWaves(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val baseStartX = w * 0.68f

        for (i in 0 until 5) {
            val offsetX = baseStartX + (i * (w * 0.072f))
            val path = Path().apply {
                moveTo(offsetX, -10f)
                cubicTo(
                    offsetX + w * 0.08f, h * 0.22f,
                    offsetX - w * 0.07f, h * 0.44f,
                    offsetX + w * 0.03f, h * 0.68f
                )
                cubicTo(
                    offsetX + w * 0.09f, h * 0.84f,
                    offsetX - w * 0.03f, h * 0.94f,
                    offsetX + w * 0.04f, h + 12f
                )
            }
            // Subtle warm shadow behind each contour wave
            drawPath(
                path = path,
                color = SageOliveDark.copy(alpha = 0.24f),
                style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
            )
            // Soft raised cream-olive ridge
            drawPath(
                path = path,
                color = Color(0xFFE2DBBD),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Bold mid-century flat editorial poster illustrations for the 3-column "My Library" subject grid,
 * visually matching the vibrant Art / Sports / Science / Business / Education / History cards
 * in the user's reference mockup while portraying Civil Engineering disciplines.
 */
@Composable
fun CivilSubjectPosterCard(
    subject: CivilSubject,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    ) {
        val w = size.width
        val h = size.height

        when (subject) {
            CivilSubject.STRUCTURAL -> {
                // Warm muted taupe/burgundy-dark architectural arch & truss (echoing Art card palette)
                drawRect(Color(0xFF7B776F))
                drawRect(Color(0xFF4B2822), topLeft = Offset(0f, h * 0.62f), size = Size(w, h * 0.38f))
                // Bold bridge arch silhouette
                val arch = Path().apply {
                    moveTo(w * 0.12f, h * 0.72f)
                    lineTo(w * 0.12f, h * 0.32f)
                    cubicTo(w * 0.12f, h * 0.10f, w * 0.88f, h * 0.10f, w * 0.88f, h * 0.32f)
                    lineTo(w * 0.88f, h * 0.72f)
                    close()
                }
                drawPath(arch, Color(0xFF2B1B18))
                // Inner warm parchment arch cutout
                val innerArch = Path().apply {
                    moveTo(w * 0.27f, h * 0.72f)
                    lineTo(w * 0.27f, h * 0.38f)
                    cubicTo(w * 0.27f, h * 0.22f, w * 0.73f, h * 0.22f, w * 0.73f, h * 0.38f)
                    lineTo(w * 0.73f, h * 0.72f)
                    close()
                }
                drawPath(innerArch, Color(0xFFD5B79C))
                // Truss steel struts
                drawLine(Color(0xFF3E2620), Offset(w * 0.27f, h * 0.56f), Offset(w * 0.73f, h * 0.56f), 4.dp.toPx())
                drawLine(Color(0xFF3E2620), Offset(w * 0.50f, h * 0.25f), Offset(w * 0.50f, h * 0.72f), 3.dp.toPx())
                drawLine(Color(0xFF3E2620), Offset(w * 0.27f, h * 0.56f), Offset(w * 0.50f, h * 0.34f), 2.5.dp.toPx())
                drawLine(Color(0xFF3E2620), Offset(w * 0.73f, h * 0.56f), Offset(w * 0.50f, h * 0.34f), 2.5.dp.toPx())
            }

            CivilSubject.HYDRAULICS -> {
                // Vivid sky-blue / cobalt & cream fluid channel (echoing Sports card freshness)
                drawRect(Color(0xFFEDF3FA))
                drawCircle(Color(0xFF2B6CB0), radius = w * 0.58f, center = Offset(w * 0.22f, h * 0.32f))
                // Dynamic white flow streamlines
                for (i in 0..3) {
                    val y = h * (0.50f + i * 0.11f)
                    val wave = Path().apply {
                        moveTo(-10f, y)
                        cubicTo(w * 0.30f, y - h * 0.08f, w * 0.65f, y + h * 0.08f, w + 10f, y - h * 0.02f)
                        lineTo(w + 10f, h + 10f)
                        lineTo(-10f, h + 10f)
                        close()
                    }
                    drawPath(
                        wave,
                        when (i) {
                            0 -> Color(0xFF1F5494)
                            1 -> Color(0xFF3B82C4)
                            2 -> Color(0xFF60A5DA)
                            else -> Color(0xFF163B6B)
                        }
                    )
                }
                // Warm orange spillway accent
                drawRoundRect(
                    color = Color(0xFFE06D3B),
                    topLeft = Offset(w * 0.56f, h * 0.14f),
                    size = Size(w * 0.32f, h * 0.28f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }

            CivilSubject.TRANSPORTATION -> {
                // Bright editorial sun-yellow & navy overpass/helix (echoing Science yellow card!)
                drawRect(Color(0xFFFCD64C))
                // Sweeping twin highway ribbons
                val road1 = Path().apply {
                    moveTo(w * 0.18f, -10f)
                    cubicTo(w * 0.85f, h * 0.26f, w * 0.15f, h * 0.62f, w * 0.78f, h + 10f)
                }
                val road2 = Path().apply {
                    moveTo(w * 0.80f, -10f)
                    cubicTo(w * 0.18f, h * 0.28f, w * 0.82f, h * 0.64f, w * 0.22f, h + 10f)
                }
                drawPath(road1, Color(0xFF28578A), style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round))
                drawPath(road2, Color(0xFF183659), style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round))
                drawPath(road1, Color(0xFFFAF6E8), style = Stroke(width = 2.dp.toPx()))
                // Bridge piers rungs
                for (i in 1..6) {
                    val y = h * (i / 7f)
                    drawLine(
                        color = Color(0xFF22456D),
                        start = Offset(w * 0.34f, y),
                        end = Offset(w * 0.66f, y),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            CivilSubject.CONSTRUCTION -> {
                // Clean sky-mint & ascending coral/navy stepped towers (echoing Business card!)
                drawRect(
                    Brush.verticalGradient(
                        0f to Color(0xFFE8F4F2),
                        0.68f to Color(0xFFCDE5E2),
                        1f to Color(0xFF9AC8C4)
                    )
                )
                // Stepped structural blocks ascending diagonally
                val stepColors = listOf(
                    Color(0xFF1E375A),
                    Color(0xFFD65162),
                    Color(0xFF284973),
                    Color(0xFFE26674),
                    Color(0xFF1B2F4A)
                )
                stepColors.forEachIndexed { idx, col ->
                    val bw = w * (0.58f - idx * 0.08f)
                    val bh = h * 0.12f
                    val topY = h * (0.76f - idx * 0.12f)
                    val leftX = w * (0.20f + idx * 0.06f)
                    drawRect(col, topLeft = Offset(leftX, topY), size = Size(bw, bh))
                }
                // Crane mast accent on top
                drawLine(Color(0xFF1B2F4A), Offset(w * 0.56f, h * 0.28f), Offset(w * 0.56f, h * 0.12f), 3.dp.toPx())
                drawLine(Color(0xFF1B2F4A), Offset(w * 0.40f, h * 0.14f), Offset(w * 0.80f, h * 0.14f), 2.5.dp.toPx())
            }

            CivilSubject.GEOTECHNICAL -> {
                // Vibrant warm amber-orange & deep teal strata/foundation profile (echoing Education card!)
                drawRect(Color(0xFFF29224))
                // Deep teal layered cliff/soil profile silhouette
                val soilMass = Path().apply {
                    moveTo(0f, h * 0.16f)
                    lineTo(w * 0.58f, h * 0.16f)
                    cubicTo(w * 0.72f, h * 0.28f, w * 0.66f, h * 0.44f, w * 0.78f, h * 0.56f)
                    lineTo(w * 0.64f, h * 0.72f)
                    lineTo(w * 0.86f, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(soilMass, Color(0xFF245A73))
                // Foundation piles driven into soil strata
                for (i in 0..2) {
                    val px = w * (0.20f + i * 0.18f)
                    drawRect(Color(0xFFF6D27B), topLeft = Offset(px, h * 0.46f), size = Size(w * 0.09f, h * 0.42f))
                }
                drawRect(Color(0xFF163B4D), topLeft = Offset(w * 0.14f, h * 0.40f), size = Size(w * 0.54f, h * 0.09f))
            }

            CivilSubject.MATERIALS -> {
                // Iconic vermilion-red & sculptural marble column (echoing History red card!)
                drawRect(Color(0xFFDC2E23))
                // Classical Doric fluted concrete/stone column in crisp white & soft warm gray
                drawRoundRect(
                    color = Color(0xFFF3ECE6),
                    topLeft = Offset(w * 0.22f, h * 0.22f),
                    size = Size(w * 0.56f, h * 0.12f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRect(
                    color = Color(0xFFE7DFD8),
                    topLeft = Offset(w * 0.28f, h * 0.34f),
                    size = Size(w * 0.44f, h * 0.54f)
                )
                // Fluting vertical grooves
                for (i in 1..4) {
                    val fx = w * (0.28f + i * 0.088f)
                    drawLine(
                        color = Color(0xFFC0B6AE),
                        start = Offset(fx, h * 0.36f),
                        end = Offset(fx, h * 0.86f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                // Dark base pedestal
                drawRect(Color(0xFF2A1412), topLeft = Offset(w * 0.18f, h * 0.86f), size = Size(w * 0.64f, h * 0.14f))
            }

            CivilSubject.SURVEYING -> {
                // Deep emerald-pine topographic contour map with brass plumb target
                drawRect(Color(0xFF245944))
                val center = Offset(w * 0.52f, h * 0.48f)
                for (r in 1..5) {
                    drawCircle(
                        color = Color(0xFFE9DFBE).copy(alpha = 0.26f + r * 0.08f),
                        radius = w * (0.12f * r),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                // Crosshair & tripod silhouette
                drawLine(Color(0xFFF6CF46), Offset(center.x, h * 0.16f), Offset(center.x, h * 0.80f), 2.dp.toPx())
                drawLine(Color(0xFFF6CF46), Offset(w * 0.16f, center.y), Offset(w * 0.88f, center.y), 2.dp.toPx())
                drawCircle(Color(0xFFF6CF46), radius = 6.dp.toPx(), center = center)
            }

            CivilSubject.MECHANICS -> {
                // Warm terracotta & ink statics force-vector equilibrium composition
                drawRect(Color(0xFFD1654C))
                drawCircle(Color(0xFFF3E9D2), radius = w * 0.36f, center = Offset(w * 0.50f, h * 0.42f))
                // Equilibrium triangle / fulcrum silhouette
                val fulcrum = Path().apply {
                    moveTo(w * 0.50f, h * 0.56f)
                    lineTo(w * 0.24f, h * 0.86f)
                    lineTo(w * 0.76f, h * 0.86f)
                    close()
                }
                drawPath(fulcrum, Color(0xFF2B1C19))
                // Balanced beam + vector arrows
                drawLine(Color(0xFF2B1C19), Offset(w * 0.14f, h * 0.48f), Offset(w * 0.86f, h * 0.36f), 6.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color(0xFF245A73), radius = 9.dp.toPx(), center = Offset(w * 0.22f, h * 0.41f))
                drawCircle(Color(0xFFF6CF46), radius = 11.dp.toPx(), center = Offset(w * 0.78f, h * 0.29f))
            }

            CivilSubject.ENVIRONMENTAL -> {
                // Editorial sage-teal & warm ivory water-cycle & treatment basin poster
                drawRect(Color(0xFF2D6A62))
                drawCircle(Color(0xFFEBE4C8), radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.34f))
                // Stylized water drop & leaf geometry
                val drop = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    cubicTo(w * 0.72f, h * 0.40f, w * 0.70f, h * 0.64f, w * 0.50f, h * 0.64f)
                    cubicTo(w * 0.30f, h * 0.64f, w * 0.28f, h * 0.40f, w * 0.50f, h * 0.16f)
                    close()
                }
                drawPath(drop, Color(0xFF1F4E5F))
                // Horizontal water horizon bands
                drawRect(Color(0xFF83B8A6), topLeft = Offset(0f, h * 0.72f), size = Size(w, h * 0.12f))
                drawRect(Color(0xFF1A3E3A), topLeft = Offset(0f, h * 0.84f), size = Size(w, h * 0.16f))
            }
        }
    }
}
