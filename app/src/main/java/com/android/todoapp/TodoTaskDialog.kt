package com.android.todoapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory
import com.amazonaws.regions.Regions
import com.android.todoapp.databinding.TodoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class TodoTaskDialog : BottomSheetDialogFragment() {

    private var _binding: TodoBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    /**
     *
     */
    private val todoTaskViewModel: TodoAppViewModel by viewModels()

    /**
     *
     */
    private val args: TodoTaskDialogArgs by navArgs()

    /**
     * client to call REST API Methods
     */
    private var client: TodoappClient? = null

    /**
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TodoBinding.inflate(inflater, container, false)

        // Sets up the client
        client = ApiClientFactory().credentialsProvider(null).build(TodoappClient::class.java)

        binding.todoNameEdit.doOnTextChanged { text, _, _, _ ->
            todoTaskViewModel.todoTaskName = text.toString()
        }

        binding.todoDescriptionEdit.doOnTextChanged { text, _, _, _ ->
            todoTaskViewModel.todoTaskDescription = text.toString()
        }
        return binding.root
    }

    /**
     * update UI with corresponding TodoTask items
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // args.createTodo is false when user just wants to update a TodoTask, thus displaying
        // the values related to the TodoTask selected
        if (!args.createTodo) {
            binding.todoNameEdit.setText(args.todoTaskName)
            binding.todoDescriptionEdit.setText(args.todoTaskDescription)
            binding.addTaskButton.text = getString(R.string.update_task)
        }

        timeSelector()
        dateSelector()

        binding.addTaskButton.setOnClickListener {
            lifecycleScope.executeAsyncTask(onPreExecute = {},
                doInBackground = {
                    val todoTask: TodoTask?

                    if (args.createTodo) {
                        // creates TodoTask item with todoId being a generated random UUID
                        todoTask = TodoTask(
                            todoName = todoTaskViewModel.todoTaskName,
                            todoDescription = todoTaskViewModel.todoTaskDescription,
                            dueDate = todoTaskViewModel.dueDateTime.formatDateString()
                        )

                    } else {
                        // if not create todoo, then we want to update one selected. provide its UUID
                        // to update it - dynamodb will realize this TodoTask is already in the database
                        // based off the UUID
                        val id = UUID.fromString(args.id)

                        todoTask = TodoTask(
                            todoId = id,
                            todoName = todoTaskViewModel.todoTaskName,
                            todoDescription = todoTaskViewModel.todoTaskDescription,
                            dueDate = todoTaskViewModel.dueDateTime.formatDateString()
                        )
                    }

                    val jsonString = Json.encodeToString(todoTask)
                    println(jsonString) // {"id":"7229496a-21b4-4167-b7a2-6d7c1d3f5f01","todoName":"Default","taskDescription":"raaahh"}
                    client?.postTodo(todoTask.todoId, jsonString, args.idToken)
                    "Result"
                }, onPostExecute = {
                    // When TodoTask is either added to database or updated, return to list fragment
                    // and display a toast notifying user of their action
                    findNavController().navigate(
                        TodoTaskDialogDirections.returnToListFragment()
                    )

                    val message = if (args.createTodo)
                        getString(R.string.added_todo_task)
                    else getString(R.string.updated_task)

                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                })
        }
    }

    /**
     * Gets the date the user selected from the DatePickerFragment
     */
    private fun dateSelector() {
        binding.dateView.text = todoTaskViewModel.dueDateTime.toDateString()
        // launch DatePickerFragment
        binding.dateView.setOnClickListener {
            val date = Date()
            findNavController().navigate(
                TodoTaskDialogDirections.selectDate(date)
            )
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE) { _, bundle ->
            val date = bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            //
            todoTaskViewModel.dueDateTime = todoTaskViewModel.dueDateTime.combineWithDate(date)
            // update UI to reflect the new date the user selected
            binding.dateView.text = date.toDateString()
        }
    }

    /**
     * Gets the time the user selected from the TimePickerFragment
     */
    private fun timeSelector() {
        // launch TimePickerFragment
        binding.timeView.setOnClickListener {
            val date = Date()
            findNavController().navigate(
                TodoTaskDialogDirections.openTimePicker(date)
            )
        }

        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_TIME) { _, bundle ->
            val time = bundle.getSerializable(TimePickerFragment.BUNDLE_KEY_TIME) as Date
            // combines the date with the time provided to reflect user selection of both attributes
            // For example is user selected 5:00PM and December 3, string is something like December 3 5:00 PM
            todoTaskViewModel.dueDateTime = todoTaskViewModel.dueDateTime.combineWithTime(time)
            // update UI to reflect the new time the user selected
            binding.timeView.text = time.toTimeString()
        }
    }

    /**
     * Need to call onCreate in order to make a callback for whenever back button on device is pressed
     * OnBackPress cannot be by itself in modal bottom sheet Fragment.
     * dismisses the fragment on back press key
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dismiss()
            }
        })
    }

    /**o
     * layout configuration for the bottom sheet fragment to display correctly
     */
    override fun onStart() {
        super.onStart()

        val mBottomBehavior = BottomSheetBehavior.from(requireView().parent as View)
        mBottomBehavior.maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
        mBottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * When fragment is dismissed, flags get cleared along with it
     */
    override fun dismiss() {
        super.dismiss()
        dialog?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

}