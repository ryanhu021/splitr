package com.splitr.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitr.app.data.Receipt

@Composable
fun HomeScreen(
    onEditReceipt: (Int) -> Unit,
    onViewBreakdown: (Int) -> Unit,
    onScanReceipt: () -> Unit,
    onManageCollaborators: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val receipts by viewModel.receiptList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadReceipts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Splitr", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onScanReceipt
        ) {
            Text("Scan New Receipt")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onManageCollaborators
        ) {
            Text("Manage Collaborators")
        }

        LazyColumn {
            items(receipts) { receiptWithItems ->
                ReceiptItem(
                    receiptWithItems.receipt,
                    onEditReceipt,
                    onViewBreakdown)
            }
        }
    }
}

@Composable
fun ReceiptItem(
    receipt: Receipt,
    onEditReceipt: (Int) -> Unit,
    onClick: (Int) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(receipt.id) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(receipt.date)
                Text("Total: ${'$'}${"%.2f".format(receipt.totalAmount)}")
            }
            IconButton(onClick = { onEditReceipt(receipt.id) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Receipt", tint = Color.Gray)
            }
        }
    }
}