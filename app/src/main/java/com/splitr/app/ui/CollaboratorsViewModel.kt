package com.splitr.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CollaboratorsViewModel(
    private val receiptDao: ReceiptDao
) : ViewModel() {
    private val _collaborators = MutableStateFlow<List<User>>(emptyList())
    val collaborators: MutableStateFlow<List<User>> = _collaborators

    init {
        loadCollaborators()
    }

    private fun loadCollaborators() {
        viewModelScope.launch {
            _collaborators.value = receiptDao.getAllUsers()
        }
    }

    fun addCollaborator(user: User) {
        viewModelScope.launch {
            receiptDao.insertUser(user)
            _collaborators.value += user
        }
    }

    fun deleteCollaborator(user: User) {
        viewModelScope.launch {
            receiptDao.deleteUser(user)
            _collaborators.value -= user
        }
    }
}