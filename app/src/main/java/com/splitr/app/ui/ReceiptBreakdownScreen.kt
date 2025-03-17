package com.splitr.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitr.app.data.Receipt
import com.splitr.app.data.ReceiptWithItemsAndUsers
import com.splitr.app.data.User

@Composable
fun ReceiptBreakdownScreen(
    receiptWithItemsAndUsers: ReceiptWithItemsAndUsers,
    onDone: () -> Unit,
    viewModel: ReceiptBreakdownViewModel = viewModel(),
) {
    val collaborators by viewModel.receiptWithAmounts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Receipt Breakdown",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Show basic receipt information
        Text("Name: ${receiptWithItemsAndUsers.receipt.name}")
        Text("Date: ${receiptWithItemsAndUsers.receipt.date}")
        Text("Total Amount: $${"%.2f".format(receiptWithItemsAndUsers.receipt.totalAmount)}")
        Spacer(modifier = Modifier.height(8.dp))

        // Display the list of collaborators and their amounts
        if (collaborators.isEmpty()) {
            Text("No collaborators found.", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn {
                items(collaborators.toList()) { (user, amount) ->
                    ContributorCard(user, amount)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onDone() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
fun ContributorCard(
    user: User,
    amount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemCollaboratorIcon(user) {}
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.name} Owes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}