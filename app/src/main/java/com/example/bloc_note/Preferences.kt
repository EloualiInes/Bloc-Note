package com.example.bloc_note

import android.content.Context
import androidx.preference.PreferenceManager

class Preferences(context: Context) {
    companion object{
        private const val modeSombreStatus = "DARK_STATUS"
    }
    private val preference = PreferenceManager.getDefaultSharedPreferences(context)
    var modeSombre = preference.getInt(modeSombreStatus, 0)
        set(value) = preference.edit().putInt(modeSombreStatus,value).apply()

}