package com.example.fitness_plan.domain.calculator

import java.util.Calendar

class WorkoutDateCalculator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateStartDate(startDate: Long): ValidationResult {
        val now = System.currentTimeMillis()
        val today = getStartOfDay(now)
        
        val selectedDate = getStartOfDay(startDate)
        
        if (selectedDate < today) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Нельзя выбрать дату в прошлом"
            )
        }
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        
        if (calendar.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Нельзя выбрать дату в следующем году"
            )
        }
        
        return ValidationResult(isValid = true)
    }

    fun getMinStartDate(): Long {
        return getStartOfDay(System.currentTimeMillis())
    }

    fun getMaxStartDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun generateDates(startDate: Long, frequency: String, totalCount: Int = 10): List<Long> {
        return when (frequency) {
            "1 раз в неделю" -> generateOncePerWeekDates(startDate, totalCount)
            "3 раза в неделю" -> generateThreeTimesPerWeekDates(startDate, totalCount)
            "5 раз в неделю" -> generateFiveTimesPerWeekDates(startDate, totalCount)
            else -> generateOncePerWeekDates(startDate, totalCount)
        }
    }

    private fun generateOncePerWeekDates(startDate: Long, totalCount: Int): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getStartOfDay(startDate)
        
        dates.add(calendar.timeInMillis)
        
        while (dates.size < totalCount) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek != Calendar.WEDNESDAY) {
                while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
                    calendar.add(Calendar.DAY_OF_YEAR, if (dayOfWeek < Calendar.WEDNESDAY) 1 else -1)
                }
            }
            dates.add(calendar.timeInMillis)
        }
        
        return dates
    }

    private fun generateThreeTimesPerWeekDates(startDate: Long, totalCount: Int): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getStartOfDay(startDate)
        
        val firstDate = findNextMondayWednesdayOrFriday(calendar)
        calendar.timeInMillis = firstDate
        
        while (dates.size < totalCount) {
            dates.add(calendar.timeInMillis)
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            when (dayOfWeek) {
                Calendar.MONDAY -> calendar.add(Calendar.DAY_OF_YEAR, 2)
                Calendar.WEDNESDAY -> calendar.add(Calendar.DAY_OF_YEAR, 2)
                Calendar.FRIDAY -> calendar.add(Calendar.DAY_OF_YEAR, 3)
                else -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return dates
    }

    private fun generateFiveTimesPerWeekDates(startDate: Long, totalCount: Int): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getStartOfDay(startDate)
        
        val firstDate = findNextMonday(calendar)
        calendar.timeInMillis = firstDate
        
        while (dates.size < totalCount) {
            dates.add(calendar.timeInMillis)
            
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.FRIDAY) {
                calendar.add(Calendar.DAY_OF_YEAR, 3)
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return dates
    }

    private fun findNextMonday(calendar: Calendar): Long {
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun findNextMondayWednesdayOrFriday(calendar: Calendar): Long {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        when (dayOfWeek) {
            Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY -> {
                return calendar.timeInMillis
            }
            Calendar.TUESDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            Calendar.THURSDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            Calendar.SATURDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 2)
            }
            Calendar.SUNDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        return calendar.timeInMillis
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
