package com.android.todoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.kotlin.core.Amplify
import com.android.todoapp.databinding.FragmentTodoListBinding
import com.android.todoapp.databinding.TodoRecyclerviewItemBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull.content
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


class TodoListFragment : Fragment() {
    /** Binding for the views of the fragment (nullable version) */
    private var _binding: FragmentTodoListBinding? = null

    /** Binding for the views of the fragment (non-nullable accessor) */
    private val binding: FragmentTodoListBinding
        get() = checkNotNull(_binding) { getString(R.string.binding_failed) }

    private var adapter: TodoTaskAdapter? = null

    /**
     * client to call REST API Methods
     */
    private var apiClient: TodoappClient? = null

    /**
     * The identification token accessed from Cognito User Pool in cooperation with Google
     * Sign in to be authorized to access the HTTP methods in AWS APi Gateway REST API.
     * Methods have a authenticator wall so no third party is able to access
     */
    private var idToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodoListBinding.inflate(inflater)
        // Inflate the layout for this fragment

        return binding.root
    }

    /**
     * Sets up the client and gets all the tasks associated to a user
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            apiClient = setUpClient()
            getTodoTasks()
        }

        // launch TodoTaskDialog when user wants to create a new TodoTask
        binding.navigationAddTaskButton.setOnClickListener {
            findNavController().navigate(
                TodoListFragmentDirections.createTodo("", "", true, "", idToken)
            )
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sign_out -> {
                    lifecycleScope.launch { signOutUser() }
                    // Handle search icon press
                    true
                }
                else -> false
            }
        }
    }

    private suspend fun signOutUser() {
        when (val signOutResult = Amplify.Auth.signOut()) {
            is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                // Sign Out completed fully and without errors.
                Log.i("AuthQuickStart", "Signed out successfully")
                startActivity(Intent(requireActivity(), SignInActivity::class.java))
                activity?.finish()
            }
            is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                // Sign Out completed with some errors. User is signed out of the device.
                signOutResult.hostedUIError?.let {
                    Log.e("AuthQuickStart", "HostedUI Error", it.exception)
                    // Optional: Re-launch it.url in a Custom tab to clear Cognito web session.

                }
                signOutResult.globalSignOutError?.let {
                    Log.e("AuthQuickStart", "GlobalSignOut Error", it.exception)
                    // Optional: Use escape hatch to retry revocation of it.accessToken.
                }
                signOutResult.revokeTokenError?.let {
                    Log.e("AuthQuickStart", "RevokeToken Error", it.exception)
                    // Optional: Use escape hatch to retry revocation of it.refreshToken.
                }
            }
            is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                // Sign Out failed with an exception, leaving the user signed in.
                Log.e("AuthQuickStart", "Sign out Failed", signOutResult.exception)
            }
        }
    }

    /**
     * Sets up the TodoAppClient to be able to access Rest Api methods
     * @return valid client to access REST API methods
     */
    private suspend fun setUpClient(): TodoappClient {
        // check the current auth session.
        try {
            // currently signed in user information
            val session = Amplify.Auth.fetchAuthSession() as AWSCognitoAuthSession
            val id = session.identityIdResult
            if (id.type == AuthSessionResult.Type.SUCCESS) {
                // Acquire the IdToken to be used for a signed in user to get authorized
                idToken = session.userPoolTokensResult.value?.idToken
                Log.i("AuthQuickStart", "IdentityId: ${id.value}")
            } else if (id.type == AuthSessionResult.Type.FAILURE)
                Log.i("AuthQuickStart", "IdentityId not present: ${id.error}")
        } catch (error: AuthException) {
            Log.e("AuthQuickStart", "Failed to fetch session", error)
        }

        return ApiClientFactory().credentialsProvider(null).build(TodoappClient::class.java)
    }

    /**
     *
     * Gets all the TodoTasks for a user by calling the getAllTodos() from the client.
     */
    private fun getTodoTasks() {
        lifecycleScope.executeAsyncTask(onPreExecute = {
            // ... runs in Main Thread
        }, doInBackground = {
            // ... runs in Worker(Background) Thread as solution to NetworkOnMainThread Exception
            val result = apiClient?.getAllTodos(idToken)

            // [{"todoId":"c58c9b1b-0c34-46fd-ab44-072a7fb5d0f7","todoName":"run it again ","todoDescription":"data","dueDate":"12/03/2022 03:32:37"}
            val jsonString: String = Json.encodeToString(result)

            val outputList =
                Json { ignoreUnknownKeys = true }.decodeFromString<List<TodoTask>>(jsonString)

            println(outputList)
            // Pass the list to the recyclerview adapter to display as an individual TodoTask on the UI
            lifecycleScope.launch {
                adapter = TodoTaskAdapter(outputList)
                binding.recyclerView.adapter = adapter
            }
            "Result" // send data to "onPostExecute"
        }, onPostExecute = {
            // runs in Main Thread
            // ... here "it" is the data returned from "doInBackground"
        })
    }

    /**
     * The ViewHolder for the items in the recycler view. This uses the layout
     * given in todo.xml.
     */
    private inner class TodoListItemHolder(val binding: TodoRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Update this view holder to display the given todotask item.
         */
        fun bind(todoTask: TodoTask) {
            binding.todoName.text = todoTask.todoName
            binding.todoDescription.text = todoTask.todoDescription
            binding.dueDateTime.text = todoTask.dueDate
            // can click on an item in the recyclerview to be able to update
            binding.root.setOnClickListener {
                findNavController().navigate(
                    TodoListFragmentDirections.createTodo(
                        todoTask.todoName, todoTask.todoDescription, false,
                        todoTask.todoId.toString(), idToken
                    )
                )
            }
            // remove the TodoTask when user clicks on checkbox
            binding.todoCheck.setOnClickListener {
                lifecycleScope.executeAsyncTask(onPreExecute = {},
                    doInBackground = {
                        apiClient?.deleteTodo(todoTask.todoId, idToken)
                        // Calling this method again is not optimal
                        getTodoTasks()
                        "Result"
                    }, onPostExecute = {
                        // should update the UI with the removed position. however couldn't figure out
                        // a clean solution. in doInBackground, acquires all the todos again
                    })
            }
        }
    }

    /**
     *
     */
    private inner class TodoTaskAdapter(val tasks: List<TodoTask>) :
        RecyclerView.Adapter<TodoListItemHolder>() {
        /** Creates a ViewHolder by inflating the todo_task.xml layout */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListItemHolder =
            TodoListItemHolder(
                TodoRecyclerviewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        /** Binds an existing ViewHolder to the event at the position in the events list */
        override fun onBindViewHolder(holder: TodoListItemHolder, position: Int) =
            holder.bind(tasks[position])

        /** return size of the list of events */
        override fun getItemCount(): Int = tasks.size
    }
}