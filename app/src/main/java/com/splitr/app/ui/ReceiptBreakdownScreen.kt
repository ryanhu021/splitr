package com.splitr.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Receipt Breakdown",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (collaborators.isEmpty()) {
            Text("No contributors found.", modifier = Modifier.padding(8.dp))
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = user.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Amount Owed: $${String.format("%.2f", amount)}")
        }
    }
}