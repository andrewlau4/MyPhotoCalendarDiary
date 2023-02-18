package com.andsoftapps.fragment

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.andsoftapps.R
import com.andsoftapps.compose.ComposeDiaryCalendarLocalProviders
import com.andsoftapps.compose.DiaryCalendarScreen
import com.andsoftapps.provider.DiaryRecentSuggestProvider
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import dagger.hilt.android.AndroidEntryPoint


private val START_SEARCH_CHAR_THRESHOLD = 2

@AndroidEntryPoint
class DiaryCalendarFragment : Fragment() {

    private val diaryCalendarViewModel: DiaryCalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val menuHost = requireActivity()
        val menu = DiaryMenu()
        menuHost.addMenuProvider(menu, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return inflater.inflate(R.layout.fragment_diary_calendar, container, false)
            .apply {
                findViewById<ComposeView>(R.id.compose_view).apply {
                    //https://developer.android.com/jetpack/compose/interop/interop-apis
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                    setContent {
                        ComposeDiaryCalendarLocalProviders(
                            viewModel = diaryCalendarViewModel,
                            activity = activity as AppCompatActivity
                        ) {
                            DiaryCalendarScreen(viewModel = diaryCalendarViewModel)
                        }
                    }
                }
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DiaryCalendarFragment()
    }

    inner class DiaryMenu : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.fragment_diary_menu, menu)

            val searchItem = menu.findItem(R.id.menu_item_search)

            val searchView = searchItem.actionView as? SearchView

            searchView?.queryHint = "enter search"

            val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

            activity?.getComponentName()?.let {
                searchView?.setSearchableInfo(searchManager.getSearchableInfo(it))
            }

            val searchAutoCompleteTextView =
                searchView?.findViewById(
                    androidx.appcompat.R.id.search_src_text
                ) as AutoCompleteTextView

            searchAutoCompleteTextView.setThreshold(START_SEARCH_CHAR_THRESHOLD)

            searchAutoCompleteTextView.apply {
                setHintTextColor(resources.getColor(R.color.purple_700, null))
                setTextColor(resources.getColor(R.color.black, null))
                setBackgroundColor(resources.getColor(R.color.white, null))
            }

            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView?.clearFocus()
                    SearchRecentSuggestions(
                        this@DiaryCalendarFragment.activity,
                        DiaryRecentSuggestProvider.AUTHORITY,
                        DiaryRecentSuggestProvider.MODE
                        ).saveRecentQuery(query, null)
                    diaryCalendarViewModel.setQuery(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    diaryCalendarViewModel.setQuery(newText)
                    return false
                }
            })
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.menu_item_search -> true
                else -> false
            }
        }
    }
}