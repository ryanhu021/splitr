package com.splitr.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.splitr.app.data.Receipt

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val receipts by viewModel.receiptList.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Splitr", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Button(onClick = { /* Navigate to scanner screen */ }) {
            Text("Scan New Receipt")
        }

        LazyColumn {
            items(receipts) { receiptWithItems ->
                ReceiptItem(receiptWithItems.receipt)
            }
        }
    }
}

@Composable
fun ReceiptItem(receipt: Receipt) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Date: ${receipt.date}", fontWeight = FontWeight.Bold)
            Text("Total: $${receipt.totalAmount}")
        }
    }
}
