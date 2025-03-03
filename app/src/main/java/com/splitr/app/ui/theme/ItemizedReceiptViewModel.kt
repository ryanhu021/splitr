package com.splitr.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.Item
import kotlinx.coroutines.launch

import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.ReceiptWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ItemizedReceiptViewModel(
    private val receiptDao: ReceiptDao,
    private val receiptId: Int?
) : ViewModel() {
    private val _receiptWithItems = MutableStateFlow<ReceiptWithItems?>(null)
    val receiptWithItems: StateFlow<ReceiptWithItems?> = _receiptWithItems

    init {
        receiptId?.let {
            loadReceipt(it)
        }
    }

    private fun loadReceipt(receiptId: Int) {
        viewModelScope.launch {
            val receipt = receiptDao.getReceiptById(receiptId)
            _receiptWithItems.value = receipt
        }
    }

    fun updateItems(items: List<Item>) {
        viewModelScope.launch {
            items.forEach { item ->
                receiptDao.updateItem(item)
            }
        }
    }

    fun setNewReceipt(receipt: ReceiptWithItems) {
        _receiptWithItems.value = receipt
    }

}