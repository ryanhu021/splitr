package com.splitr.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitr.app.data.Item
import com.splitr.app.data.ReceiptWithItems

@Composable
fun ItemizedReceiptScreen(
    receiptWithItems: ReceiptWithItems,
    onDoneClick: () -> Unit,
    onNext: (Int) -> Unit,
    viewModel: ItemizedReceiptViewModel = viewModel()
) {
    var editableItems by remember { mutableStateOf(receiptWithItems.items) }
    var updatedName by remember { mutableStateOf(receiptWithItems.receipt.name) }
    var updatedDate by remember { mutableStateOf(receiptWithItems.receipt.date) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Receipt Details", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        TextField(
            value = updatedName,
            onValueChange = { newName ->
                viewModel.updateReceiptName(newName)
                updatedName = newName
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            label = { Text("Name") },
            textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.width(16.dp))

        TextField(
            value = updatedDate,
            onValueChange = { newDate ->
                viewModel.updateReceiptDate(newDate)
                updatedDate = newDate
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            label = { Text("Date") },
            textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn {
                    items(editableItems) { item ->
                        ItemRow(item) { updatedItem ->
                            editableItems = editableItems.map {
                                if (it.id == updatedItem.id) updatedItem else it
                            }
                            viewModel.updateItems(editableItems)
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Total",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${'$'}${receiptWithItems.receipt.totalAmount}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.deleteReceipt(receiptWithItems.receipt)
                        onDoneClick()
                    }
                ) {
                    Text("Delete Receipt", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onNext(receiptWithItems.receipt.id)
                    }
                ) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
fun ItemRow(
    item: Item,
    onItemChange: (Item) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(2f)) {
            TextField(
                value = item.name,
                onValueChange = { onItemChange(item.copy(name = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Item Name") },
                textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium)
            )
//            TextField(
//                value = item.quantity.toString(),
//                onValueChange = {
//                    onItemChange(item.copy(quantity = it.toIntOrNull() ?: 0))
//                },
//                modifier = Modifier.fillMaxWidth(),
//                label = { Text("Quantity") },
//                textStyle = TextStyle.Default.copy(fontSize = 14.sp)
//            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        TextField(
            value = "${'$'}${item.price}",
            onValueChange = {
                val priceString = it.removePrefix("$")
                priceString.toDoubleOrNull()?.let { price ->
                    onItemChange(item.copy(price = price))
                }
            },
            modifier = Modifier.weight(1f),
            label = { Text("Price") },
            textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Bold)
        )
    }
}