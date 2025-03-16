package com.splitr.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.ReceiptWithItemsAndUsers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DistributeReceiptViewModel(
    private val receiptDao: ReceiptDao,
    private val receiptId: Int?
) : ViewModel() {

    private val _receiptWithItemsAndUsers = MutableStateFlow<ReceiptWithItemsAndUsers?>(null)
    val receiptWithItemsAndUsers: StateFlow<ReceiptWithItemsAndUsers?> =
        _receiptWithItemsAndUsers.asStateFlow()

    init {
        receiptId?.let {
            loadReceipt(it)
        }
    }

    private fun loadReceipt(receiptId: Int) {
        viewModelScope.launch {
            val receipt = receiptDao.getReceiptWithUsersById(receiptId)
            _receiptWithItemsAndUsers.value = receipt
        }
    }

    fun assignCollaborators(receiptId: Int) {
        //TODO:
    }

}
