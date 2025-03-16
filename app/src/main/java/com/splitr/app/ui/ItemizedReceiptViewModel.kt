package com.splitr.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.Item
import com.splitr.app.data.Receipt
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
            val receipt = receiptDao.getReceiptWithItemsById(receiptId)
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

    fun updateReceiptName(name: String) {
        viewModelScope.launch {
            _receiptWithItems.value?.receipt?.let { receipt ->
                receiptDao.updateReceipt(receipt.copy(name = name))
            }
        }
    }

    fun updateReceiptDate(date: String) {
        viewModelScope.launch {
            _receiptWithItems.value?.receipt?.let { receipt ->
                receiptDao.updateReceipt(receipt.copy(date = date))
            }
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            receiptDao.deleteReceipt(receipt)
        }
    }

}