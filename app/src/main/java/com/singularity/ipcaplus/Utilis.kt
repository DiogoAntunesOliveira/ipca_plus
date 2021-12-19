package com.singularity.ipcaplus

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.util.regex.Pattern

object  Utilis {

    /*
        This function return the current month id
        @month = month id
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentMonthId(): Int {

        // Variables
        val c = LocalDate.now()

        val strArray = Pattern.compile("-").split(c.toString())

        return strArray[1].toInt()
    }

    /*
        This function return the month value in string by an id
        @month = month id
     */
    fun getMonthById(month: Int): String {
        return when (month) {
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> "None"
        }
    }
}