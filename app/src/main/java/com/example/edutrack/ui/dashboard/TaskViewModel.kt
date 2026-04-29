package com.example.edutrack.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.edutrack.ui.dashboard.EduTrackDatabase
import com.example.edutrack.ui.dashboard.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = EduTrackDatabase.getDatabase(application).taskDao()
    val allTasks: LiveData<List<Task>> = dao.getAllTasks()

    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        dao.insert(task)
    }
    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        dao.update(task)
    }
    fun delete(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        dao.delete(task)
    }
}