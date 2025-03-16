package com.splitr.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitr.app.data.ReceiptDao
import com.splitr.app.data.User
import com.splitr.app.data.UserReceiptCrossRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CollaboratorsViewModel(
    private val receiptDao: ReceiptDao,
    private val receiptId: Int?
) : ViewModel() {
    private val _collaborators = MutableStateFlow<List<User>>(emptyList())
    val collaborators: MutableStateFlow<List<User>> = _collaborators

    private val _selectedCollaborators = MutableStateFlow<List<User>>(emptyList())
    val selectedCollaborators: MutableStateFlow<List<User>> = _selectedCollaborators

    init {
        loadCollaborators()

        receiptId?.let { id ->
            loadSelectedCollaborators(id)
        }
    }

    private fun loadCollaborators() {
        viewModelScope.launch {
            _collaborators.value = receiptDao.getAllUsers()
        }
    }

    private fun loadSelectedCollaborators(receiptId: Int) {
        viewModelScope.launch {
            _selectedCollaborators.value = receiptDao.getUsersForReceiptById(receiptId)
        }
        _selectedCollaborators.value.forEach { user -> _collaborators.value - user }
    }

    fun toggleSelectedCollaboratorSelection(user: User, receiptId: Int?) {
        if (receiptId != null) {
            val currentSelected = _selectedCollaborators.value
            viewModelScope.launch {
                _selectedCollaborators.value = if (user in currentSelected) {
                    receiptDao.deleteUserReceiptCrossRef(UserReceiptCrossRef(user.id, receiptId))
                    _selectedCollaborators.value - user

                } else {
                    receiptDao.insertUserReceiptCrossRef(UserReceiptCrossRef(user.id, receiptId))
                    _selectedCollaborators.value + user
                }
            }
            Log.e("Contributors", _selectedCollaborators.value.toString())

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
            if (_selectedCollaborators.value.contains(user))
                _selectedCollaborators.value -= user
        }
    }

    fun toggleCollaboratorSelection(user: User) {

    }

//    fun addCollaboratorToReceipt(user: User) {
//        viewModelScope.launch {
//            receiptDao.insertUserItemCrossRef()
//        }
//    }
}
