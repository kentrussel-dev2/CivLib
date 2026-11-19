package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.BookReadingLayout
import com.example.data.model.LibrarySortMode
import com.example.data.model.LibraryViewMode
import com.example.data.model.PaperTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readerPrefsDataStore by preferencesDataStore(name = "civlib_reader_prefs")

data class ReaderPreferences(
    val fontSizeSp: Float = 17f,
    val paperTheme: PaperTheme = PaperTheme.PARCHMENT,
    val sortMode: LibrarySortMode = LibrarySortMode.BY_SUBJECT,
    val viewMode: LibraryViewMode = LibraryViewMode.BOOKSHELF,
    val readingLayout: BookReadingLayout = BookReadingLayout.TWO_PAGE_SPREAD,
    val cropPdfMargins: Boolean = false
)

class ReaderPreferencesStore(private val context: Context) {
    private object Keys {
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val PAPER_THEME = stringPreferencesKey("paper_theme")
        val SORT_MODE = stringPreferencesKey("sort_mode")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val READING_LAYOUT = stringPreferencesKey("reading_layout")
        val CROP_PDF_MARGINS = booleanPreferencesKey("crop_pdf_margins")
    }

    val preferencesFlow: Flow<ReaderPreferences> = context.readerPrefsDataStore.data.map { prefs ->
        ReaderPreferences(
            fontSizeSp = (prefs[Keys.FONT_SIZE_SP] ?: 17f).coerceIn(14f, 24f),
            paperTheme = prefs[Keys.PAPER_THEME]?.let { name ->
                PaperTheme.entries.firstOrNull { it.name == name }
            } ?: PaperTheme.PARCHMENT,
            sortMode = prefs[Keys.SORT_MODE]?.let { name ->
                LibrarySortMode.entries.firstOrNull { it.name == name }
            } ?: LibrarySortMode.BY_SUBJECT,
            viewMode = prefs[Keys.VIEW_MODE]?.let { name ->
                LibraryViewMode.entries.firstOrNull { it.name == name }
            } ?: LibraryViewMode.BOOKSHELF,
            readingLayout = prefs[Keys.READING_LAYOUT]?.let { name ->
                BookReadingLayout.entries.firstOrNull { it.name == name }
            } ?: BookReadingLayout.TWO_PAGE_SPREAD,
            cropPdfMargins = prefs[Keys.CROP_PDF_MARGINS] ?: false
        )
    }

    suspend fun updateFontSize(fontSizeSp: Float) {
        context.readerPrefsDataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE_SP] = fontSizeSp.coerceIn(14f, 24f)
        }
    }

    suspend fun updatePaperTheme(theme: PaperTheme) {
        context.readerPrefsDataStore.edit { prefs ->
            prefs[Keys.PAPER_THEME] = theme.name
        }
    }

    suspend fun updateSortMode(sortMode: LibrarySortMode) {
        context.readerPrefsDataStore.edit { prefs ->
            prefs[Keys.SORT_MODE] = sortMode.name
        }
    }

    suspend fun updateViewMode(viewMode: LibraryViewMode) {
        context.readerPrefsDataStore.edit { prefs ->
            prefs[Keys.VIEW_MODE] = viewMode.name
        }
    }

    suspend fun updateReadingLayout(layout: BookReadingLayout) {
        context.readerPrefsDataStore.edit { prefs ->
            prefs[Keys.READING_LAYOUT] = layout.name
        }
    }

    suspend fun updateCropPdfMargins(crop: Boolean) {
        context.readerPrefsDataStore.edit { prefs ->
            prefs[Keys.CROP_PDF_MARGINS] = crop
        }
    }
}
