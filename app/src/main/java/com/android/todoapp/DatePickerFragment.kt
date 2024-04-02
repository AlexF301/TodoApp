package com.android.todoapp

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.*

/**
 * A fragment that acts as a popup window for picking a date.
 */
// NOTE: This class is complete and nearly the same as what is in chapter 14.
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    /**
     * The arguments passed to this dialog:
     *   - date: optional (nullable) Date to use for the initial date shown;
     *     if null, the current date (i.e. now) is used
     */
    private val args: DatePickerFragmentArgs by navArgs()

    /**
     * When the dialog is created, we need to create the appropriate picker (in
     * this case a DatePickerDialog) and set its initial information (in this
     * case the `date` argument or right now if null).
     * @param savedInstanceState not used
     * @return the Dialog to be shown
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = Calendar.getInstance()
        cal.time = args.date
        return DatePickerDialog(
            requireContext(), this,
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * When the DatePickerDialog is confirmed, this method sends a result back
     * to the fragment with the key REQUEST_KEY_DATE containing a bundle that
     * has the key BUNDLE_KEY_DATE associated with a date.
     * @param view the DatePicker that is calling this method
     * @param year the year picked
     * @param month the month picked
     * @param dayOfMonth the day of that month picked
     */
    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        setFragmentResult(
            REQUEST_KEY_DATE,
            bundleOf(BUNDLE_KEY_DATE to createDate(year, month, dayOfMonth))
        )
    }

    /**
     * Creates a Date object for the given year, month, and day. The time is at
     * the start of that day (i.e. midnight).
     * @param year the year (e.g. 2021)
     * @param month the month (1-12)
     * @param dayOfMonth the day of that month (1-31)
     * @return a Date object for midnight at the start of that day
     */
    private fun createDate(year: Int, month: Int, dayOfMonth: Int): Date =
        GregorianCalendar(year, month, dayOfMonth).time

    companion object {
        /** The key used to send results back from fragment requests */
        const val REQUEST_KEY_DATE = "com.android.TodoApp.DatePickerFragment.DATE"

        /** The key used for the selected date in the result bundle */
        const val BUNDLE_KEY_DATE = "DATE"
    }
}