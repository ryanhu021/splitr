package com.splitr.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitr.app.data.User

@Composable
fun CollaboratorsScreen(
    receiptId: Int?,
    onBack: () -> Unit,
    viewModel: CollaboratorsViewModel = viewModel()
) {
    val collaborators by viewModel.collaborators.collectAsState()
    val selectedCollaborators by viewModel.selectedCollaborators.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newCollaboratorName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text("Manage Collaborators", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))
        if (receiptId != null) {
            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Back", fontSize = 24.sp)
            }
            if (selectedCollaborators.isNotEmpty()) {
                Text("Selected Collaborators", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(selectedCollaborators) { user ->
                        CollaboratorItem(
                            user = user,
                            onClick = {
                                viewModel.toggleSelectedCollaboratorSelection(
                                    user,
                                    receiptId
                                )
                            },
                            onDelete = { viewModel.deleteCollaborator(user) }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            Text("Available Collaborators", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Collaborators List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(collaborators.filter { it !in selectedCollaborators }) { user ->
                CollaboratorItem(
                    user = user,
                    onClick = { viewModel.toggleSelectedCollaboratorSelection(user, receiptId) },
                    onDelete = { viewModel.deleteCollaborator(user) }
                )
            }
        }

        // "+" Button for Adding Collaborators
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
            ) {
                Text("+", fontSize = 24.sp)
            }
        }

        // Add Collaborator Dialog
        if (showAddDialog) {
            AddCollaboratorDialog(
                newCollaboratorName = newCollaboratorName,
                onNameChange = { newCollaboratorName = it },
                onAddClick = {
                    if (newCollaboratorName.isNotBlank()) {
                        viewModel.addCollaborator(User(name = newCollaboratorName.trim()))
                        newCollaboratorName = "" // Clear input field
                        showAddDialog = false  // Close the dialog
                    }
                },
                onDismiss = {
                    showAddDialog = false
                    newCollaboratorName = ""
                }
            )
        }
    }
}

@Composable
fun AddCollaboratorDialog(
    newCollaboratorName: String,
    onNameChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Collaborator") },
        text = {
            TextField(
                value = newCollaboratorName,
                onValueChange = onNameChange,
                label = { Text("Collaborator Name") }
            )
        },
        confirmButton = {
            Button(onClick = onAddClick) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

