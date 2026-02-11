package com.example.fitness_plan.domain.calculator

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class WorkoutDateCalculatorTest {

    private val calculator = WorkoutDateCalculator()

    @Test
    fun `validateStartDate should reject dates in the past`() {
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        val result = calculator.validateStartDate(yesterday)
        assertThat(result.isValid).isFalse()
        assertThat(result.errorMessage).isEqualTo("Нельзя выбрать дату в прошлом")
    }

    @Test
    fun `validateStartDate should accept today`() {
        val today = System.currentTimeMillis()
        val result = calculator.validateStartDate(today)
        assertThat(result.isValid).isTrue()
        assertThat(result.errorMessage).isNull()
    }

    @Test
    fun `validateStartDate should accept future dates in current year`() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.DECEMBER)
        calendar.set(Calendar.DAY_OF_MONTH, 31)
        val futureDate = calendar.timeInMillis
        val result = calculator.validateStartDate(futureDate)
        assertThat(result.isValid).isTrue()
        assertThat(result.errorMessage).isNull()
    }

    @Test
    fun `generateDates for 1 per week should start with selected date and then Wednesday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 20, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "1 раз в неделю", 4)

        assertThat(dates).hasSize(4)
        
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = dates[0]
        assertThat(cal1.get(Calendar.DAY_OF_MONTH)).isEqualTo(20)
        assertThat(cal1.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = dates[1]
        assertThat(cal2.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal3 = Calendar.getInstance()
        cal3.timeInMillis = dates[2]
        assertThat(cal3.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal4 = Calendar.getInstance()
        cal4.timeInMillis = dates[3]
        assertThat(cal4.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)
    }

    @Test
    fun `generateDates for 1 per week starting on Monday should switch to Wednesday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 13, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "1 раз в неделю", 3)

        assertThat(dates).hasSize(3)

        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = dates[0]
        assertThat(cal1.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = dates[1]
        assertThat(cal2.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal3 = Calendar.getInstance()
        cal3.timeInMillis = dates[2]
        assertThat(cal3.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)
    }

    @Test
    fun `generateDates for 3 per week should start on Monday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 13, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "3 раза в неделю", 6)

        assertThat(dates).hasSize(6)

        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = dates[0]
        assertThat(cal1.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = dates[1]
        assertThat(cal2.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal3 = Calendar.getInstance()
        cal3.timeInMillis = dates[2]
        assertThat(cal3.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.FRIDAY)

        val cal4 = Calendar.getInstance()
        cal4.timeInMillis = dates[3]
        assertThat(cal4.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)
    }

    @Test
    fun `generateDates for 3 per week starting on Tuesday should start on Wednesday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "3 раза в неделю", 3)

        assertThat(dates).hasSize(3)

        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = dates[0]
        assertThat(cal1.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = dates[1]
        assertThat(cal2.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.FRIDAY)

        val cal3 = Calendar.getInstance()
        cal3.timeInMillis = dates[2]
        assertThat(cal3.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)
    }

    @Test
    fun `generateDates for 3 per week starting on Wednesday should start on Wednesday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "3 раза в неделю", 3)

        assertThat(dates).hasSize(3)

        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = dates[0]
        assertThat(cal1.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = dates[1]
        assertThat(cal2.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.FRIDAY)

        val cal3 = Calendar.getInstance()
        cal3.timeInMillis = dates[2]
        assertThat(cal3.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)
    }

    @Test
    fun `generateDates for 5 per week should start on Monday`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "5 раз в неделю", 10)

        assertThat(dates).hasSize(10)

        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = dates[0]
        assertThat(cal1.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = dates[1]
        assertThat(cal2.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.TUESDAY)

        val cal3 = Calendar.getInstance()
        cal3.timeInMillis = dates[2]
        assertThat(cal3.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.WEDNESDAY)

        val cal4 = Calendar.getInstance()
        cal4.timeInMillis = dates[3]
        assertThat(cal4.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.THURSDAY)

        val cal5 = Calendar.getInstance()
        cal5.timeInMillis = dates[4]
        assertThat(cal5.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.FRIDAY)

        val cal6 = Calendar.getInstance()
        cal6.timeInMillis = dates[5]
        assertThat(cal6.get(Calendar.DAY_OF_WEEK)).isEqualTo(Calendar.MONDAY)
    }

    @Test
    fun `generateDates for 5 per week should skip weekend`() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val dates = calculator.generateDates(startDate, "5 раз в неделю", 7)

        assertThat(dates).hasSize(7)

        for (date in dates) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            assertThat(dayOfWeek).isNotEqualTo(Calendar.SATURDAY)
            assertThat(dayOfWeek).isNotEqualTo(Calendar.SUNDAY)
        }
    }
}
