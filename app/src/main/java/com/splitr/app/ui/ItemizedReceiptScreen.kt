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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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

        AutoClosingTextField(
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

        AutoClosingTextField(
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
                LazyColumn(modifier = Modifier.weight(1F)) {
                    items(editableItems) { item ->
                        ItemRow(item,
                            onItemChange = { updatedItem ->
                                editableItems = editableItems.map {
                                    if (it.id == updatedItem.id) updatedItem else it
                                }
                                viewModel.updateItems(editableItems)
                            },
                            onDelete = {
                                editableItems = editableItems.filter { it.id != item.id }
                                viewModel.updateItems(editableItems)
                            }
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val newItem = Item(
                            receiptId = receiptWithItems.receipt.id,
                            name = "",
                            price = 0.0,
                            quantity = 1
                        )
                        editableItems = editableItems + newItem
                        viewModel.updateItems(editableItems)
                    }
                ) {
                    Text("Add Item")
                }
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
                        "${'$'}${"%.2f".format(receiptWithItems.receipt.totalAmount)}",
                        fontWeight = FontWeight.Bold
                    )
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
    onItemChange: (Item) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            AutoClosingTextField(
                value = item.name,
                onValueChange = { onItemChange(item.copy(name = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Item Name") },
                textStyle = TextStyle.Default.copy(fontWeight = FontWeight.Medium)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        AutoClosingTextField(
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
            ),
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
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp),
            onClick = onDelete
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Item",
                tint = Color.Red
            )
        }
    }
}
