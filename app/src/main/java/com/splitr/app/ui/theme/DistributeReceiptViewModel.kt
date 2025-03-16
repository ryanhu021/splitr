package com.splitr.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.ReceiptWithItemsAndUsers
import com.splitr.app.data.User
import com.splitr.app.data.UserItemCrossRef
import com.splitr.app.data.UserReceiptCrossRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class DistributeReceiptViewModel(
    private val receiptDao: ReceiptDao,
    private val receiptId: Int
) : ViewModel() {

    private val _receiptWithItemsAndUsers = MutableStateFlow<ReceiptWithItemsAndUsers?>(null)
    val receiptWithItemsAndUsers: MutableStateFlow<ReceiptWithItemsAndUsers?> =
        _receiptWithItemsAndUsers

    private val _receiptContributors = MutableStateFlow<List<User>>(emptyList())
    val receiptContributors: MutableStateFlow<List<User>> = _receiptContributors

    init {
        loadReceipt(receiptId)
    }

    fun loadReceipt(receiptId: Int) {
        viewModelScope.launch {
            val receipt = receiptDao.getReceiptWithUsersById(receiptId)
            _receiptWithItemsAndUsers.value = receipt

            _receiptContributors.value = receiptDao.getUsersForReceiptById(receiptId)
        }
    }

    fun deleteCollaborator(user: User) {
        viewModelScope.launch {
            _receiptWithItemsAndUsers.value?.itemsWithUsers?.forEach { itemWithUsers ->
                itemWithUsers.users.forEach { itemUser ->
                    if (itemUser.id == user.id) {
                        receiptDao.deleteUserItemCrossRef(
                            UserItemCrossRef(
                                user.id,
                                itemWithUsers.item.id
                            )
                        )
                    }
                }
            }

            receiptDao.deleteUserReceiptCrossRef(UserReceiptCrossRef(user.id, receiptId))
            _receiptContributors.value = receiptDao.getUsersForReceiptById(receiptId)
            _receiptWithItemsAndUsers.value = receiptDao.getReceiptWithUsersById(receiptId)
        }
    }

    fun assignCollaboratorsOnItem(itemId: Int?, userId: Int) {
        if (itemId != null) {
            viewModelScope.launch {
                receiptDao.insertUserItemCrossRef(UserItemCrossRef(userId, itemId))
                _receiptWithItemsAndUsers.value = receiptDao.getReceiptWithUsersById(receiptId)
            }
        }
    }

    fun removeCollaboratorsOnItem(itemId: Int, userId: Int) {
        viewModelScope.launch {
            receiptDao.deleteUserItemCrossRef(UserItemCrossRef(userId, itemId))
            _receiptWithItemsAndUsers.value = receiptDao.getReceiptWithUsersById(receiptId)
        }
    }

}
