package com.splitr.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.ReceiptWithItemsAndUsers
import com.splitr.app.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DistributeReceiptViewModel(
    private val receiptDao: ReceiptDao,
    private val receiptId: Int?
) : ViewModel() {

    private val _receiptWithItemsAndUsers = MutableStateFlow<ReceiptWithItemsAndUsers?>(null)
    val receiptWithItemsAndUsers: MutableStateFlow<ReceiptWithItemsAndUsers?> =
        _receiptWithItemsAndUsers

    private val _receiptContributors = MutableStateFlow<List<User>>(emptyList())
    val receiptContributors: MutableStateFlow<List<User>> = _receiptContributors

    init {
        receiptId?.let {
            loadReceipt(it)
        }
    }

    fun loadReceipt(receiptId: Int) {
        viewModelScope.launch {
            val receipt = receiptDao.getReceiptWithUsersById(receiptId)
            _receiptWithItemsAndUsers.value = receipt

            _receiptContributors.value = receiptDao.getUsersForReceiptById(receiptId)
        }
    }

    fun assignCollaborators(receiptId: Int) {
        //TODO:
    }

}
