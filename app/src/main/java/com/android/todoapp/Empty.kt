package com.android.todoapp

import com.google.gson.annotations.SerializedName

@kotlinx.serialization.Serializable
class Empty {
    @SerializedName("todoId")
    var todoId: String? = null
    @SerializedName("todoName")
    var todoName : String? = null
    @SerializedName("todoDescription")
    var todoDescription : String? = null
    @SerializedName("dueDate")
    var dueDate : String? = null
}