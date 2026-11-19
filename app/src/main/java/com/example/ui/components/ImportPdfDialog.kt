package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CivilSubject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportPdfMetadataDialog(
    initialTitle: String,
    isImporting: Boolean,
    onDismiss: () -> Unit,
    onConfirmImport: (title: String, author: String, isbn: String, subject: CivilSubject) -> Unit
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var author by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var selectedSubject by remember(initialTitle) {
        mutableStateOf(CivilSubject.inferFromText(initialTitle))
    }

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        title = {
            Text(
                text = "Import PDF Textbook",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Assign a course subject and catalog details so CivLib shelves this PDF alongside your open-access library.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    singleLine = true,
                    enabled = !isImporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_pdf_title_field")
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author (optional)") },
                    placeholder = { Text("e.g., R. C. Hibbeler") },
                    singleLine = true,
                    enabled = !isImporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_pdf_author_field")
                )

                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN (optional)") },
                    placeholder = { Text("e.g., 9780134610672") },
                    singleLine = true,
                    enabled = !isImporting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_pdf_isbn_field")
                )

                Text(
                    text = "COURSE SUBJECT SHELF",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CivilSubject.entries.forEach { subject ->
                        FilterChip(
                            selected = selectedSubject == subject,
                            onClick = { if (!isImporting) selectedSubject = subject },
                            label = { Text("${subject.courseCode} ${subject.shortName}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmImport(title, author, isbn, selectedSubject) },
                enabled = !isImporting && title.isNotBlank(),
                modifier = Modifier.testTag("confirm_import_pdf_button")
            ) {
                if (isImporting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importing…")
                    }
                } else {
                    Text("Open as Book")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isImporting
            ) {
                Text("Cancel")
            }
        }
    )
}
