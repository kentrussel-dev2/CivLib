package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SubjectConstructionColor
import com.example.ui.theme.SubjectGeotechColor
import com.example.ui.theme.SubjectHydraulicsColor
import com.example.ui.theme.SubjectMaterialsColor
import com.example.ui.theme.SubjectStructuralColor
import com.example.ui.theme.SubjectSurveyingColor
import com.example.ui.theme.SubjectTransportationColor

/**
 * Civil Engineering curriculum categories arranged in chronological college sequence
 * (Year 1 Foundations -> Year 2 Core Surveying & Materials -> Year 3 Major Disciplines -> Year 4 Design & Management)
 * so students never lose track of what subject comes next.
 */
enum class CivilSubject(
    val displayName: String,
    val shortName: String,
    val courseCode: String,
    val curriculumYear: Int,
    val semesterLabel: String,
    val sequenceOrder: Int,
    val accentColor: Color,
    val defaultCoverArtKey: String
) {
    MECHANICS(
        displayName = "Statics, Dynamics & Mechanics",
        shortName = "Mechanics",
        courseCode = "CE 101",
        curriculumYear = 1,
        semesterLabel = "Year 1 · Sem 1–2",
        sequenceOrder = 1,
        accentColor = Color(0xFFB55D4C),
        defaultCoverArtKey = "structural"
    ),
    SURVEYING(
        displayName = "Engineering Surveying & Geomatics",
        shortName = "Surveying",
        courseCode = "CE 201",
        curriculumYear = 2,
        semesterLabel = "Year 2 · Sem 1",
        sequenceOrder = 2,
        accentColor = SubjectSurveyingColor,
        defaultCoverArtKey = "hydraulics"
    ),
    MATERIALS(
        displayName = "Strength of Materials & Concrete",
        shortName = "Materials",
        courseCode = "CE 205",
        curriculumYear = 2,
        semesterLabel = "Year 2 · Sem 2",
        sequenceOrder = 3,
        accentColor = SubjectMaterialsColor,
        defaultCoverArtKey = "structural"
    ),
    STRUCTURAL(
        displayName = "Structural Analysis, Steel & RC",
        shortName = "Structural",
        courseCode = "CE 301",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 1",
        sequenceOrder = 4,
        accentColor = SubjectStructuralColor,
        defaultCoverArtKey = "structural"
    ),
    HYDRAULICS(
        displayName = "Hydraulics, Fluids & Hydrology",
        shortName = "Hydraulics",
        courseCode = "CE 302",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 1",
        sequenceOrder = 5,
        accentColor = SubjectHydraulicsColor,
        defaultCoverArtKey = "hydraulics"
    ),
    GEOTECHNICAL(
        displayName = "Soil Mechanics & Foundations",
        shortName = "Geotechnical",
        courseCode = "CE 304",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 2",
        sequenceOrder = 6,
        accentColor = SubjectGeotechColor,
        defaultCoverArtKey = "geotech"
    ),
    TRANSPORTATION(
        displayName = "Highway & Transportation Eng.",
        shortName = "Transport",
        courseCode = "CE 306",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 2",
        sequenceOrder = 7,
        accentColor = SubjectTransportationColor,
        defaultCoverArtKey = "geotech"
    ),
    ENVIRONMENTAL(
        displayName = "Water Supply & Environmental",
        shortName = "Environment",
        courseCode = "CE 401",
        curriculumYear = 4,
        semesterLabel = "Year 4 · Sem 1",
        sequenceOrder = 8,
        accentColor = Color(0xFF4A8B7C),
        defaultCoverArtKey = "hydraulics"
    ),
    CONSTRUCTION(
        displayName = "Construction Mgmt & Capstones",
        shortName = "Construction",
        courseCode = "CE 405",
        curriculumYear = 4,
        semesterLabel = "Year 4 · Sem 2",
        sequenceOrder = 9,
        accentColor = SubjectConstructionColor,
        defaultCoverArtKey = "structural"
    );

    companion object {
        fun fromKey(key: String?): CivilSubject {
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: STRUCTURAL
        }

        fun inferFromText(text: String): CivilSubject {
            val lower = text.lowercase()
            return when {
                lower.contains("statics") || lower.contains("dynamics") ||
                    lower.contains("theoretical mechanics") || lower.contains("engineering mechanics") -> MECHANICS
                lower.contains("survey") || lower.contains("geodesy") || lower.contains("topograph") ||
                    lower.contains("photogram") || lower.contains("geomatics") -> SURVEYING
                lower.contains("strength of material") || lower.contains("mechanics of material") ||
                    lower.contains("elasticity") || lower.contains("building material") ||
                    lower.contains("concrete tech") -> MATERIALS
                lower.contains("soil") || lower.contains("geotech") || lower.contains("foundation") ||
                    lower.contains("slope") || lower.contains("earth") || lower.contains("tunnel") -> GEOTECHNICAL
                lower.contains("environmental") || lower.contains("wastewater") || lower.contains("sanitary") ||
                    lower.contains("water treatment") || lower.contains("sewerage") -> ENVIRONMENTAL
                lower.contains("hydraulic") || lower.contains("fluid") || lower.contains("hydrology") ||
                    lower.contains("water") || lower.contains("channel") || lower.contains("dam") -> HYDRAULICS
                lower.contains("highway") || lower.contains("traffic") || lower.contains("transport") ||
                    lower.contains("pavement") || lower.contains("railway") || lower.contains("road") -> TRANSPORTATION
                lower.contains("construction") || lower.contains("project manage") || lower.contains("estimat") ||
                    lower.contains("contract") || lower.contains("scheduling") || lower.contains("sustainable") -> CONSTRUCTION
                else -> STRUCTURAL
            }
        }
    }
}
