package com.andsoftapps.compose

import android.R
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.ListPopupWindow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.andsoftapps.databinding.LayoutMonthSelectionDropdownBinding
import java.lang.reflect.Field
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

private val TOTAL_YEARS_IN_LIST = 60
private val BEGINNING_YEAR_IN_LIST = 1990

@Composable
fun AndroidViewMonthSelector(month: YearMonth, modifier: Modifier,
                             monthChangeCallback: (YearMonth) -> Unit) {

    val yearList = remember { List(TOTAL_YEARS_IN_LIST) { (it + BEGINNING_YEAR_IN_LIST).toString() } }

    val monthMap = remember { List(12) { it }
        .map { Month.of(it + 1).getDisplayName( TextStyle.FULL , Locale.US ) to it } }.toMap()

    val currentContext = LocalContext.current

    AndroidViewBinding(LayoutMonthSelectionDropdownBinding::inflate, modifier = modifier) {

        val yearAdapter: ArrayAdapter<String> =
            object: ArrayAdapter<String>(currentContext, R.layout.simple_spinner_item, yearList) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    return super.getDropDownView(position, convertView, parent).also { view ->
                        if(position == (month.year - BEGINNING_YEAR_IN_LIST)) {
                            view.setBackgroundColor(
                                Color.rgb(204, 255, 255)
                            )
                        } else {
                            view.setBackgroundColor(
                                Color.rgb(255, 255, 255)
                            )
                        }
                    }
                }
            }
        yearAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        yearSelectionDropdownList.adapter = yearAdapter
        yearSelectionDropdownList.setSelection(month.year - BEGINNING_YEAR_IN_LIST)

        //https://stackoverflow.com/questions/21426038/how-do-i-set-the-maximum-length-for-a-spinners-drop-down-list
        val yearPopupField: Field =
            AppCompatSpinner::class.java.getDeclaredField("mPopup")
        yearPopupField.isAccessible = true
        val popupWindow = yearPopupField.get(yearSelectionDropdownList) as
                ListPopupWindow
        popupWindow.height = 700

        val monthAdapter: ArrayAdapter<String> =
            object: ArrayAdapter<String>(currentContext,
                R.layout.simple_spinner_item,
                monthMap.keys.toList()) {

                // https://stackoverflow.com/questions/12406486/android-spinner-highlight-selected-item
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    return super.getDropDownView(position, convertView, parent).also { view ->
                            if(position == (month.monthValue - 1)) {
                                view.setBackgroundColor(
                                    Color.rgb(204, 255, 255)
                                )
                            } else {
                                view.setBackgroundColor(
                                    Color.rgb(255, 255, 255)
                                )
                            }
                        }
                }
            }
        monthAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        monthSelectionDropdownList.adapter = monthAdapter
        monthSelectionDropdownList.setSelection(month.monthValue - 1)

        yearSelectionDropdownList.onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val newMonth = YearMonth.of(position + BEGINNING_YEAR_IN_LIST, month.month)
                    if (newMonth != month) {
                        monthChangeCallback(newMonth)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        monthSelectionDropdownList.onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val newMonth = YearMonth.of(month.year, position + 1)
                    if (newMonth != month) {
                        monthChangeCallback(newMonth)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

    }
}

