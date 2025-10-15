package com.example.roomdatabaseapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.roomdatabaseapp.data.AppDatabase
import com.example.roomdatabaseapp.data.User
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    val users: LiveData<List<User>> = userDao.getAllUsers().asLiveData()

    fun insertUser(name: String, age: Int) {
        viewModelScope.launch {
            userDao.insert(User(name = name, age = age))
        }
    }

    fun updateUser(id: Int, name: String?, age: Int?) {
        viewModelScope.launch {
            val finalName = name ?: return@launch
            val finalAge = age ?: return@launch
            userDao.update(User(id = id, name = finalName, age = finalAge))
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.delete(user)
        }
    }
}