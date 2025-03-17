package com.splitr.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.splitr.app.data.ItemWithUsers
import com.splitr.app.data.ReceiptWithItemsAndUsers
import com.splitr.app.data.User

@Composable
fun DistributeReceiptScreen(
    receiptWithItemsAndUsers: ReceiptWithItemsAndUsers,
    onDone: () -> Unit,
    onViewBreakdown: () -> Unit,
    onAddContributors: () -> Unit,
    viewModel: DistributeReceiptViewModel = viewModel()
) {
    // State to hold the collaborators selected in this screen
    val receiptContributors = viewModel.receiptContributors.collectAsState()
    var selectedItemId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(receiptWithItemsAndUsers.receipt.id) {
        viewModel.loadReceipt(receiptWithItemsAndUsers.receipt.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Distribute Receipt",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Show basic receipt information
        Text("Name: ${receiptWithItemsAndUsers.receipt.name}")
        Text("Date: ${receiptWithItemsAndUsers.receipt.date}")
        Text("Total Amount: $${"%.2f".format(receiptWithItemsAndUsers.receipt.totalAmount)}")
        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of items for context
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(receiptWithItemsAndUsers.itemsWithUsers) { itemWithUsers ->
                    ReceiptItemWithContributors(
                        itemWithUsers = itemWithUsers,
                        onItemClick = { selectedItemId = itemWithUsers.item.id },
                        onContributorClick = { user ->
                            viewModel.removeCollaboratorsOnItem(itemWithUsers.item.id, user.id)
                        },
                        isSelected = selectedItemId == itemWithUsers.item.id // Pass the selected state
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section for collaborator selection.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Selected Collaborators:",
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    onAddContributors()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Contributors",
                    tint = Color.White,
                )
            }
        }
        if (receiptContributors.value.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1F)
            ) {
                LazyColumn {
                    items(receiptContributors.value) { collaborator ->
                        CollaboratorItem(
                            collaborator,
                            onClick = {
                                viewModel.assignCollaboratorsOnItem(
                                    selectedItemId,
                                    collaborator.id
                                )
                            },
                            onDelete = { viewModel.deleteCollaborator(collaborator) }
                        )
                    }
                }
            }
        } else {
            Text("No collaborators selected.", modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Button(
                    onClick = {
                        onViewBreakdown()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Breakdown")
                }
                Button(
                    onClick = {
                        onDone()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}


@Composable
fun ItemCollaboratorIcon(user: User, onClick: () -> Unit) {
    val isLebron = user.name.equals("Lebron", ignoreCase = true)
    val imageUrl = "https://cdn.nba.com/headshots/nba/latest/1040x760/2544.png"

    Card(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .border(width = 2.dp, color = Color.Gray, shape = CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLebron) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Lebron's profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString() ?: "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ReceiptItemWithContributors(
    itemWithUsers: ItemWithUsers,
    onItemClick: () -> Unit,
    onContributorClick: (User) -> Unit,
    isSelected: Boolean // Pass selected state here
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(4.dp)
                .then(
                    if (isSelected) Modifier.background(Color.LightGray.copy(alpha = 0.3f)) // Apply background when selected
                    else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item name on the left
            Text(
                text = if (itemWithUsers.item.name.length > 20) {
                    itemWithUsers.item.name.take(20) + "..."
                } else {
                    itemWithUsers.item.name
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Row for contributor icons on the right, spaced out as desired
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                items(itemWithUsers.users) { user ->
                    ItemCollaboratorIcon(
                        user = user,
                        onClick = { onContributorClick(user) }
                    )
                }
            }
        }
    }
}
