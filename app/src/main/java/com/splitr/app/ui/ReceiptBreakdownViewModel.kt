package com.splitr.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.ReceiptWithItemsAndUsers
import com.splitr.app.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReceiptBreakdownViewModel(
    private val receiptDao: ReceiptDao,
    private val receiptId: Int
) : ViewModel() {

    private val _receiptWithAmounts = MutableStateFlow<Map<User, Double>>(emptyMap())
    val receiptWithAmounts: MutableStateFlow<Map<User, Double>> = _receiptWithAmounts

    init {
        fetchReceiptBreakdown()
    }

    private fun fetchReceiptBreakdown() {
        viewModelScope.launch {
            val receiptWithItemsAndUsers = receiptDao.getReceiptWithItemsAndUsersById(receiptId)
            _receiptWithAmounts.value = calculateAmounts(receiptWithItemsAndUsers)
        }
    }

    private fun calculateAmounts(receipt: ReceiptWithItemsAndUsers): Map<User, Double> {
        val userAmounts = mutableMapOf<User, Double>()

        for (itemWithUsers in receipt.itemsWithUsers) {
            val pricePerPerson = itemWithUsers.item.price / itemWithUsers.users.size
            for (user in itemWithUsers.users) {
                userAmounts[user] = userAmounts.getOrDefault(user, 0.0) + pricePerPerson
            }
        }

        return userAmounts
    }
}
