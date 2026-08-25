package com.shunshun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shunshun.app.data.AppDatabase
import com.shunshun.app.data.PoopDao
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.navigation.ShunShunApp
import com.shunshun.app.ui.ShunShunTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShunShunTheme { ShunShunApp() } }
    }
}

class PoopViewModel(private val dao: PoopDao) : ViewModel() {
    val records: StateFlow<List<PoopRecord>> = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(record: PoopRecord) = viewModelScope.launch(Dispatchers.IO) { dao.insert(record) }
    fun addAll(records: List<PoopRecord>) = viewModelScope.launch(Dispatchers.IO) { dao.insertAll(records) }
    fun delete(record: PoopRecord) = viewModelScope.launch(Dispatchers.IO) { dao.delete(record) }
    fun clear() = viewModelScope.launch(Dispatchers.IO) { dao.clearAll() }
}

class PoopViewModelFactory(private val dao: PoopDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PoopViewModel(dao) as T
}
