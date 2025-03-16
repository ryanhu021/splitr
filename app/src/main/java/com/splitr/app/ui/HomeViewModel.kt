package com.splitr.app.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.Item
import com.splitr.app.data.Receipt
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.ReceiptWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val receiptDao: ReceiptDao,
) : ViewModel() {
    private val _receiptList = MutableStateFlow<List<ReceiptWithItems>>(emptyList())
    var receiptList = _receiptList.asStateFlow()

    init {
        loadReceipts()
        addTestReceipts()
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            _receiptList.value = receiptDao.getAllReceipts()
        }
    }

    fun addReceipt(receipt: Receipt, items: List<Item>) {
        viewModelScope.launch {
            val receiptId = receiptDao.insertReceipt(receipt).toInt()
            items.forEach { it.copy(receiptId = receiptId) }
            receiptDao.insertItems(items)
            loadReceipts()
        }
    }

    private fun addTestReceipts() {
        viewModelScope.launch {
            if (receiptDao.getAllReceipts().isEmpty()) { // Avoid duplicate inserts
                // test receipt
                val receipt = Receipt(name = "Costco", date = "2025-02-18", totalAmount = 45.67)
                val receiptId = receiptDao.insertReceipt(receipt).toInt()

                val items = listOf(
                    Item(receiptId = receiptId, name = "Pizza", price = 12.99, quantity = 1),
                    Item(receiptId = receiptId, name = "Soda", price = 2.50, quantity = 2),
                    Item(receiptId = receiptId, name = "Burger", price = 8.99, quantity = 1)
                )
                receiptDao.insertItems(items)

                loadReceipts() // Refresh UI
            }
        }
    }
}