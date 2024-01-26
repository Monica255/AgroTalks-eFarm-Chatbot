package com.example.efarm.core.util

import android.content.Context
import com.example.eFarm.R
import com.example.efarm.core.domain.model.Mdate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



object DateConverter {
    fun convertStringToData(data:String,withHour:Boolean=true): Mdate {
        val year=data.substring(0,4).toInt()
        val month=data.substring(4,6).replaceFirst("^0+(?!$)", "").toInt()
        val day=data.substring(6,8).replaceFirst("^0+(?!$)", "").toInt()
        val hour=data.substring(8,10).replaceFirst("^0+(?!$)", "").toInt()


        return Mdate(year,month,day,if (withHour)hour else null)
    }
    fun convertMillisToString(timeMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun convertMillisToDate(timeMillis: Long,ctx: Context):String{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        val sdf = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault())

        val list=sdf.format(calendar.time).split(",")
        val output=StringBuilder().append(when(list[0]){
            "Monday"->"Senin"
            "Tuesday"->"Selasa"
            "Wednesday"->"Rabu"
            "Thursday"->"Kamis"
            "Friday"->"Jumat"
            "Saturday"->"Sabtu"
            "Sunday"->"Minggu"
            else->list[0]
        }).append(", ").append(list[1].trim())


        return output.toString()
    }

    fun getCurrentTimestamp():Long=System.currentTimeMillis()

}