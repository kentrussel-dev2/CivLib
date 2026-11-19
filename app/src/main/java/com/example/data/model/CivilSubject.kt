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
    MATH_PHYSICS(
        displayName = "Engineering Math, Calculus & Physics",
        shortName = "Math & Physics",
        courseCode = "ENGR 101",
        curriculumYear = 1,
        semesterLabel = "Year 1 · Foundation STEM",
        sequenceOrder = 1,
        accentColor = Color(0xFF3B6E8C),
        defaultCoverArtKey = "hydraulics"
    ),
    MECHANICS(
        displayName = "Statics, Dynamics & Mechanics",
        shortName = "Mechanics",
        courseCode = "CE 101",
        curriculumYear = 1,
        semesterLabel = "Year 1 · Sem 1–2",
        sequenceOrder = 2,
        accentColor = Color(0xFFB55D4C),
        defaultCoverArtKey = "structural"
    ),
    SURVEYING(
        displayName = "Engineering Surveying & Geomatics",
        shortName = "Surveying",
        courseCode = "CE 201",
        curriculumYear = 2,
        semesterLabel = "Year 2 · Sem 1",
        sequenceOrder = 3,
        accentColor = SubjectSurveyingColor,
        defaultCoverArtKey = "hydraulics"
    ),
    MATERIALS(
        displayName = "Strength of Materials & Concrete",
        shortName = "Materials",
        courseCode = "CE 205",
        curriculumYear = 2,
        semesterLabel = "Year 2 · Sem 2",
        sequenceOrder = 4,
        accentColor = SubjectMaterialsColor,
        defaultCoverArtKey = "structural"
    ),
    ALLIED_ENGINEERING(
        displayName = "Mechanical, Electrical & General Eng.",
        shortName = "Allied Eng.",
        courseCode = "ENGR 210",
        curriculumYear = 2,
        semesterLabel = "Year 2 · Allied Engineering",
        sequenceOrder = 5,
        accentColor = Color(0xFF7B5B3A),
        defaultCoverArtKey = "geotech"
    ),
    STRUCTURAL(
        displayName = "Structural Analysis, Steel & RC",
        shortName = "Structural",
        courseCode = "CE 301",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 1",
        sequenceOrder = 6,
        accentColor = SubjectStructuralColor,
        defaultCoverArtKey = "structural"
    ),
    HYDRAULICS(
        displayName = "Hydraulics, Fluids & Hydrology",
        shortName = "Hydraulics",
        courseCode = "CE 302",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 1",
        sequenceOrder = 7,
        accentColor = SubjectHydraulicsColor,
        defaultCoverArtKey = "hydraulics"
    ),
    GEOTECHNICAL(
        displayName = "Soil Mechanics & Foundations",
        shortName = "Geotechnical",
        courseCode = "CE 304",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 2",
        sequenceOrder = 8,
        accentColor = SubjectGeotechColor,
        defaultCoverArtKey = "geotech"
    ),
    TRANSPORTATION(
        displayName = "Highway & Transportation Eng.",
        shortName = "Transport",
        courseCode = "CE 306",
        curriculumYear = 3,
        semesterLabel = "Year 3 · Sem 2",
        sequenceOrder = 9,
        accentColor = SubjectTransportationColor,
        defaultCoverArtKey = "geotech"
    ),
    ENVIRONMENTAL(
        displayName = "Water Supply & Environmental",
        shortName = "Environment",
        courseCode = "CE 401",
        curriculumYear = 4,
        semesterLabel = "Year 4 · Sem 1",
        sequenceOrder = 10,
        accentColor = Color(0xFF4A8B7C),
        defaultCoverArtKey = "hydraulics"
    ),
    CONSTRUCTION(
        displayName = "Construction Mgmt & Capstones",
        shortName = "Construction",
        courseCode = "CE 405",
        curriculumYear = 4,
        semesterLabel = "Year 4 · Sem 2",
        sequenceOrder = 11,
        accentColor = SubjectConstructionColor,
        defaultCoverArtKey = "structural"
    ),
    MASTERS_GRADUATE(
        displayName = "Masters, Seismic, FEM & Bridge Eng.",
        shortName = "Masters / MSCE",
        courseCode = "MSCE 501",
        curriculumYear = 5,
        semesterLabel = "Masters & Advanced Graduate",
        sequenceOrder = 12,
        accentColor = Color(0xFF5D467E),
        defaultCoverArtKey = "structural"
    );

    companion object {
        fun fromKey(key: String?): CivilSubject {
            return entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: STRUCTURAL
        }

        fun inferFromText(text: String): CivilSubject {
            val lower = text.lowercase()
            return when {
                lower.contains("calculus") || lower.contains("differential equation") ||
                    lower.contains("linear algebra") || lower.contains("engineering math") ||
                    lower.contains("higher mathematics") || lower.contains("physics") ||
                    lower.contains("trigonometry") || lower.contains("numerical method") -> MATH_PHYSICS
                lower.contains("finite element") || lower.contains("earthquake") ||
                    lower.contains("seismic") || lower.contains("structural dynamics") ||
                    lower.contains("prestressed") || lower.contains("advanced structural") ||
                    lower.contains("graduate") || lower.contains("plates and shells") -> MASTERS_GRADUATE
                lower.contains("thermodynamics") || lower.contains("electrical") ||
                    lower.contains("electronics") || lower.contains("circuit") ||
                    lower.contains("machine design") || lower.contains("mechanical engineering") ||
                    lower.contains("chemical engineering") || lower.contains("electromagnetism") -> ALLIED_ENGINEERING
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
