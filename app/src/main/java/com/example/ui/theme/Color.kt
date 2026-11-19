package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Reference-Matched Warm Espresso, Olive-Sage & Sand-Cream Palette
val EspressoBg = Color(0xFF4D3C36)          // Primary warm dark espresso background
val EspressoDeep = Color(0xFF3E2F2A)        // Deeper cocoa for pressed / subtle borders
val EspressoCard = Color(0xFF5A4740)        // Elevated cocoa surface
val EspressoInk = Color(0xFF2D221E)         // Dark ink on cream & sage surfaces
val EspressoInkMuted = Color(0xFF5E4E46)    // Secondary ink on cream & sage surfaces

val SageOlive = Color(0xFF9C9A76)           // Signature olive-sage panel & medallion fill
val SageOliveLight = Color(0xFFAAA885)      // Lighter sage highlight
val SageOliveDark = Color(0xFF838160)       // Subtle sage border / contour stroke

val SandCream = Color(0xFFEAE4C9)           // Featured banner, search bar, active pill
val SandCreamLight = Color(0xFFF3EFE0)      // Bright warm cream text on dark espresso
val SandCreamMuted = Color(0xFFCBBFA9)      // Muted warm sand secondary text on espresso

// Bottom Navigation Bar Soft-Bevel Gradient Stops (1-to-1 with mockup)
val NavBarTopBevel = Color(0xFFCBC6A5)
val NavBarCenterCream = Color(0xFFEEEAD2)
val NavBarBottomBevel = Color(0xFFB7B28E)

// Compatibility aliases used across existing theme & reader references
val BlueprintNavy = EspressoBg
val BlueprintNavyDark = EspressoDeep
val BlueprintNavyContainer = SageOlive
val OnBlueprintNavyContainer = EspressoInk

val TerracottaAccent = Color(0xFFC95A3A)
val TerracottaContainer = Color(0xFFF3DFD4)
val OnTerracottaContainer = Color(0xFF421709)

val DraftingBrass = Color(0xFF9E7B34)
val DraftingBrassLight = Color(0xFFD8BE78)
val BrassContainer = SandCream
val OnBrassContainer = EspressoInk

val ArchivalPaper = EspressoBg
val ArchivalPaperVariant = EspressoCard
val ArchivalCardSurface = EspressoCard
val ArchivalCardElevated = Color(0xFF634F47)
val InkCharcoal = SandCreamLight
val SurveyorSlate = SandCreamMuted
val DraftingLine = Color(0xFF6B574F)

// Dark Theme Tokens (harmonized deeper cocoa for system dark mode)
val DarkBlueprintBg = Color(0xFF3D2E29)
val DarkBlueprintSurface = Color(0xFF493832)
val DarkBlueprintCard = Color(0xFF55423B)
val DarkBlueprintElevated = Color(0xFF624D45)
val DarkBlueprintPrimary = SandCream
val DarkOnBlueprintPrimary = EspressoInk
val DarkPrimaryContainer = SageOlive
val DarkTerracotta = Color(0xFFE08B6B)
val DarkTerracottaContainer = Color(0xFF5E2B1C)
val DarkBrass = DraftingBrassLight
val DarkBrassContainer = Color(0xFF564728)
val DarkArchivalInk = SandCreamLight
val DarkSlateMuted = SandCreamMuted
val DarkDraftingLine = Color(0xFF65524B)

// Vibrant Editorial Poster Subject Colors (matching the 3-column My Library grid)
val SubjectStructuralColor = Color(0xFF3A445D)     // Editorial Slate Navy
val SubjectGeotechColor = Color(0xFFD9722E)        // Warm Terracotta Amber
val SubjectHydraulicsColor = Color(0xFF2B6CB0)     // Cerulean Cobalt
val SubjectTransportationColor = Color(0xFFF6CF46) // Vivid Editorial Yellow
val SubjectSurveyingColor = Color(0xFF2F7D5B)      // Emerald Pine
val SubjectConstructionColor = Color(0xFF2E6F7E)   // Deep Petrol Teal
val SubjectMaterialsColor = Color(0xFFD83B2D)      // Vermilion Red

// Reader Paper Themes
val ParchmentBg = Color(0xFFF3ECDA)
val ParchmentSurface = Color(0xFFE8E0C8)
val ParchmentInk = Color(0xFF241C18)
val ParchmentSubtle = Color(0xFF66584D)

val CrispPaperBg = Color(0xFFFCFBF9)
val CrispPaperSurface = Color(0xFFF1EFE9)
val CrispPaperInk = Color(0xFF181513)
val CrispPaperSubtle = Color(0xFF5E5650)

val BlueprintNightBg = Color(0xFF241B18)
val BlueprintNightSurface = Color(0xFF332722)
val BlueprintNightInk = Color(0xFFF0E9D8)
val BlueprintNightSubtle = Color(0xFFB7AA98)
