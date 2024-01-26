package com.example.efarm.core.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.eFarm.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.TimeZone

class TextFormater {
    companion object{
        fun formatLikeCounts(likes: Int): String {
            return if (likes <= 999) {
                likes.toString()
            } else {
                var x = likes
                x /= 100
                var z=x.toDouble()
                z/=10
                z.toString()+" k"
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun timestampToLocalTime(timestamp: Long): String {
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                TimeZone.getDefault().toZoneId()
            ).toString()

        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun stringToDate(data: String): LocalDate {
            val x = data.split("-")
            return LocalDate.of(
                x[0].toInt(),
                x[1].toInt(),
                x[2].toInt()
            )
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun toPostTime(timestamp: Long, context: Context): String {
            var date = timestampToLocalTime(timestamp)

            val posted: String
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DAY_OF_MONTH)
            val x = date.split("T")
            val y = stringToDate(x[0])

            val todayDate = LocalDate.of(year, month, day)
            val daysBetween = ChronoUnit.DAYS.between(y, todayDate)

            val time = x[1].substring(0, 5)
            posted = if (x[0] == todayDate.toString()) {
                context.resources.getString(
                    R.string.posted,
                    "${context.resources.getString(R.string.today)}",
                    time
                )
            } else {
                when {
                    daysBetween.toInt() > 7 -> {
                        val r = x[0].split("-")
                        val date =
                            context.resources.getString(R.string.date_format, r[2], r[1], r[0])
                        context.resources.getString(R.string.posted, date, time)
                    }
                    daysBetween.toInt() == 1 -> {
                        context.resources.getString(
                            R.string.posted,
                            "${context.resources.getString(R.string.yesterday)}",
                            time
                        )
                    }
                    else -> {
                        context.resources.getString(
                            R.string.posted_days_ago,
                            "${ChronoUnit.DAYS.between(y, todayDate)}", time
                        )
                    }
                }
            }
            return posted
        }
    }
}