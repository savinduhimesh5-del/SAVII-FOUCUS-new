package com.savii.focus.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.savii.focus.data.FocusDatabase
import com.savii.focus.data.FocusRepository
import com.savii.focus.model.FocusSession
import com.savii.focus.model.FocusTask
import com.savii.focus.model.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppState(
    val tasks: List<FocusTask> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val sessions: List<FocusSession> = emptyList(),
    val loading: Boolean = true
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusRepository(FocusDatabase(application))
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                AppState(repository.tasks(), repository.subjects(), repository.sessions(), false)
            }
            _state.value = data
        }
    }

    fun addTask(title: String, due: String) = change { repository.addTask(title, due) }
    fun toggleTask(task: FocusTask) = change { repository.toggleTask(task.id, !task.done) }
    fun deleteTask(task: FocusTask) = change { repository.deleteTask(task.id) }
    fun addSubject(name: String, detail: String) = change { repository.addSubject(name, detail) }
    fun addSession(subject: String, minutes: Int, completed: Boolean = true) = change { repository.addSession(subject, minutes, completed) }

    private fun change(action: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { action() }
            refresh()
        }
    }
}
