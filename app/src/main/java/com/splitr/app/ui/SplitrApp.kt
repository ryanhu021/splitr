package com.splitr.app.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitr.app.data.Receipt
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.splitr.app.data.AppDatabase
import com.splitr.app.data.Item
import com.splitr.app.data.ReceiptWithItems
import com.splitr.app.ui.theme.ItemizedReceiptViewModel
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object Home

    @Serializable
    data class ItemizedReceipt(
        val receiptId: Int
    )
}

@Composable
fun SplitrApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val receiptDao = remember { database.receiptDao() }


    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable<Routes.Home> {
            HomeScreen(
                onEditReceipt = { receiptId ->
                    navController.navigate(Routes.ItemizedReceipt(receiptId))
                }
            )
        }
        composable<Routes.ItemizedReceipt> { backstackEntry ->
            val details: Routes.ItemizedReceipt = backstackEntry.toRoute()
            val viewModel: ItemizedReceiptViewModel = viewModel {
                ItemizedReceiptViewModel(receiptDao, details.receiptId)
            }
            val receiptWithItems by viewModel.receiptWithItems.collectAsState()

            receiptWithItems?.let {
                ItemizedReceiptScreen(
                    receiptWithItems = it,
                    onDoneClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onEditReceipt: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val receipts by viewModel.receiptList.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Splitr", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* Navigate to scanner screen */ }
        ) {
            Text("Scan New Receipt")
        }

        LazyColumn {
            items(receipts) { receiptWithItems ->
                ReceiptItem(receiptWithItems.receipt, onEditReceipt)
            }
        }
    }
}

@Composable
fun ItemizedReceiptScreen(
    receiptWithItems: ReceiptWithItems,
    onDoneClick: () -> Unit,
    viewModel: ItemizedReceiptViewModel = viewModel()
) {
    var editableItems by remember { mutableStateOf(receiptWithItems?.items ?: emptyList()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Receipt Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Date: ${receiptWithItems.receipt.date}")

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn {
                    items(editableItems) { item ->
                        ItemRow(item) { updatedItem ->
                            editableItems = editableItems.map {
                                if (it.id == updatedItem.id) updatedItem else it
                            }
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("${'$'}${receiptWithItems.receipt.totalAmount}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.updateItems(editableItems)
                onDoneClick()
            }
        ) {
            Text("Done")
        }
    }
}

@Composable
fun ReceiptItem(
    receipt: Receipt,
    onEditReceipt: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(receipt.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(receipt.date)
                Text("Total: ${'$'}${receipt.totalAmount}")
            }
            IconButton(onClick = { onEditReceipt(receipt.id) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Receipt", tint = Color.Gray)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
            TextField(
                value = item.quantity.toString(),
                onValueChange = {
                    onItemChange(item.copy(quantity = it.toIntOrNull() ?: 0))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Quantity") },
                textStyle = TextStyle.Default.copy(fontSize = 14.sp)
            )
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