package com.android.todoapp

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.*

/**
 * A fragment that acts as a popup window for picking a time.
 */
// HINT: Use the DatePickerFragment as inspiration for completing this one.
class TimePickerFragment : DialogFragment(), OnTimeSetListener {
    /**
     * The arguments passed to this dialog:
     *   - time: optional (nullable) Date to use for the initial time shown;
     *     if null, the current time (i.e. now) is used
     *   - isStartTime: boolean if this is being used as the start time or end
     *     time; this is only used during setting the fragment result and does
     *     not directly effect the behavior od the dialog
     */
    private val args: TimePickerFragmentArgs by navArgs()

    /**
     * When the dialog is created, we need to create the appropriate picker (in
     * this case a TimePickerDialog) and set its initial information (in this
     * case the `time` argument or right now if null).
     * @param savedInstanceState not used
     * @return the Dialog to be shown
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()
        cal.time = args.time
        return TimePickerDialog(
            requireContext(), this,
            cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), false
        )
    }

    /**
     * When the TimePickerDialog is confirmed, this method sends a result back
     * to the fragment with the key REQUEST_KEY_DATE containing a bundle that
     * has the key BUNDLE_KEY_DATE associated with a date.
     * @param view the TimePickerDialog that is calling this method
     * @param hourOfDay the hour picked
     * @param minute the minute picked
     */
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        setFragmentResult(
            REQUEST_KEY_TIME,
            bundleOf(BUNDLE_KEY_TIME to createTime(hourOfDay, minute))
        )
    }

    /**
     * Create a Date object for the given hour and minute. The time is put on an
     * arbitrary day.
     * @param hour the hour (0-23)
     * @param minute the minute (0-59)
     * @return a Date object for the given time on an arbitrary day
     */
    private fun createTime(hour: Int, minute: Int): Date {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = hour
        cal[Calendar.MINUTE] = minute
        return cal.time
    }

    companion object {
        /** The key used to send results back from fragment requests for start times */
        const val REQUEST_KEY_TIME =
            "com.android.TodoApp.TimePickerFragment.TIME"

        /** The key used for the selected time in the result bundle */
        const val BUNDLE_KEY_TIME = "TIME"
    }
}