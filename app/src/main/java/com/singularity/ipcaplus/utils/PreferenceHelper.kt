package com.singularity.ipcaplus.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferenceHelper {

    val USER_ID = "USER_ID"
    val USER_NAME = "USER_NAME"
    val USER_EMAIL = "USER_EMAIL"
    val USER_PASSWORD = "PASSWORD"
    val USER_ROLE = "USER_ROLE"

    fun defaultPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    var SharedPreferences.userId
        get() = getString(USER_ID, "")
        set(value) {
            UserLoggedIn.id = value
            editMe {
                it.putString(USER_ID, value)
            }
        }

    var SharedPreferences.email
        get() = getString(USER_EMAIL, "")
        set(value) {
            UserLoggedIn.email = value
            editMe {
                it.putString(USER_EMAIL, value)
            }
        }

    var SharedPreferences.password
        get() = getString(USER_PASSWORD, "")
        set(value) {
            UserLoggedIn.password = value
            editMe {
                it.putString(USER_PASSWORD, value)
            }
        }

    var SharedPreferences.name
        get() = getString(USER_NAME, "")
        set(value) {
            UserLoggedIn.name = value
            editMe {
                it.putString(USER_NAME, value)
            }
        }

    var SharedPreferences.role
        get() = getString(USER_ROLE, "")
        set(value) {
            UserLoggedIn.role = value
            editMe {
                it.putString(USER_ROLE, value)
            }
        }

    var SharedPreferences.clearValues
        get() = { }
        set(value) {
            editMe {
                it.clear()
            }
        }
}