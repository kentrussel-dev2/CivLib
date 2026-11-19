package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Unified Warm Espresso, Olive-Sage & Sand-Cream ColorScheme (1-to-1 with reference mockup)
private val EspressoCreamColorScheme = darkColorScheme(
    primary = SandCream,
    onPrimary = EspressoInk,
    primaryContainer = SageOlive,
    onPrimaryContainer = EspressoInk,
    secondary = SageOlive,
    onSecondary = EspressoInk,
    secondaryContainer = EspressoCard,
    onSecondaryContainer = SandCreamLight,
    tertiary = DraftingBrassLight,
    onTertiary = EspressoInk,
    tertiaryContainer = EspressoDeep,
    onTertiaryContainer = SandCream,
    background = EspressoBg,
    onBackground = SandCreamLight,
    surface = EspressoCard,
    onSurface = SandCreamLight,
    surfaceVariant = EspressoDeep,
    onSurfaceVariant = SandCreamMuted,
    surfaceContainerLowest = EspressoDeep,
    surfaceContainerLow = EspressoCard,
    surfaceContainer = Color(0xFF5F4C44),
    surfaceContainerHigh = Color(0xFF6B574E),
    outline = DraftingLine,
    outlineVariant = Color(0xFF5E4B43)
)

val CivLibShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CivLibTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EspressoCreamColorScheme,
        typography = AppTypography,
        shapes = CivLibShapes
    ) {
        // Ensure no uncolored Text on Espresso backgrounds ever falls back to Compose's default Color.Black
        CompositionLocalProvider(LocalContentColor provides SandCreamLight) {
            content()
        }
    }
}
