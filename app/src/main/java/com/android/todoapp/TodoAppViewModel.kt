package com.android.todoapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TodoAppViewModel : ViewModel() {
    var todoTaskName: String = "Default"
    var todoTaskDescription: String = "Todo Task"
    var dueDateTime = Date()

    fun someFun() {
        viewModelScope.executeAsyncTask(onPreExecute = {
            // ... runs in Main Thread
        }, doInBackground = {
            // ... runs in Worker(Background) Thread
            "Result" // send data to "onPostExecute"
        }, onPostExecute = {
            // runs in Main Thread
            // ... here "it" is the data returned from "doInBackground"
        })
    }

//    fun updateTodoTask(onUpdate: (TodoTask) -> TodoTask){
//        _todoTask.update { it?.let {onUpdate(it)} }
//    }
}

fun CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit,
    doInBackground: () -> String,
    onPostExecute: (String) -> Unit
) = launch {
    onPreExecute() // runs in Main Thread
    val result = withContext(Dispatchers.IO) {
        doInBackground() // runs in background thread without blocking the Main Thread
    }
    onPostExecute(result) // runs in Main Thread
}
