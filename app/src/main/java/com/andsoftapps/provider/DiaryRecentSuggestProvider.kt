package com.andsoftapps.provider

import android.content.SearchRecentSuggestionsProvider

class DiaryRecentSuggestProvider : SearchRecentSuggestionsProvider() {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.andsoftapps.provider.DiaryRecentSuggestProvider"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }

}