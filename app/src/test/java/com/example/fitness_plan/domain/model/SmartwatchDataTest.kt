package com.example.fitness_plan.domain.model

import org.junit.Assert.*
import org.junit.Test

class SmartwatchDataTest {

    @Test
    fun `empty creates SmartwatchData with null values`() {
        val data = SmartwatchData.empty()
        
        assertNull(data.heartRate)
        assertNull(data.heartRateMin)
        assertNull(data.heartRateMax)
        assertNull(data.heartRateAvg)
        assertNull(data.caloriesBurned)
        assertNull(data.steps)
        assertNull(data.distance)
        assertEquals(0L, data.activeDuration)
        assertNull(data.heartRateZone)
    }

    @Test
    fun `empty uses provided timestamp`() {
        val timestamp = 1234567890L
        val data = SmartwatchData.empty(timestamp)
        
        assertEquals(timestamp, data.timestamp)
    }

    @Test
    fun `isActive returns true when heartRate is not null`() {
        val data = SmartwatchData(
            timestamp = System.currentTimeMillis(),
            heartRate = 120,
            heartRateMin = null,
            heartRateMax = null,
            heartRateAvg = null,
            caloriesBurned = null,
            steps = null,
            distance = null,
            activeDuration = 60000L,
            heartRateZone = HeartRateZone.CARDIO
        )
        
        assertTrue(data.isActive)
    }

    @Test
    fun `isActive returns true when caloriesBurned is not null`() {
        val data = SmartwatchData(
            timestamp = System.currentTimeMillis(),
            heartRate = null,
            heartRateMin = null,
            heartRateMax = null,
            heartRateAvg = null,
            caloriesBurned = 150.0,
            steps = null,
            distance = null,
            activeDuration = 60000L,
            heartRateZone = null
        )
        
        assertTrue(data.isActive)
    }

    @Test
    fun `isActive returns false when all biometrics are null`() {
        val data = SmartwatchData.empty()
        
        assertFalse(data.isActive)
    }

    @Test
    fun `formatDuration formats zero correctly`() {
        val data = SmartwatchData.empty().copy(activeDuration = 0L)
        
        assertEquals("0:00", data.formatDuration())
    }

    @Test
    fun `formatDuration formats minutes and seconds correctly`() {
        val data = SmartwatchData.empty().copy(activeDuration = 125000L)
        
        assertEquals("2:05", data.formatDuration())
    }

    @Test
    fun `formatDuration formats only seconds correctly`() {
        val data = SmartwatchData.empty().copy(activeDuration = 45000L)
        
        assertEquals("0:45", data.formatDuration())
    }

    @Test
    fun `formatDuration formats large durations correctly`() {
        val data = SmartwatchData.empty().copy(activeDuration = 3723000L)
        
        assertEquals("62:03", data.formatDuration())
    }
}

class HeartRateZoneTest {

    @Test
    fun `fromHeartRate returns null when heartRate is null`() {
        val zone = HeartRateZone.fromHeartRate(null, 30)
        
        assertNull(zone)
    }

    @Test
    fun `fromHeartRate returns null when age is zero or negative`() {
        assertNull(HeartRateZone.fromHeartRate(100, 0))
        assertNull(HeartRateZone.fromHeartRate(100, -5))
    }

    @Test
    fun `fromHeartRate returns REST for low heart rate`() {
        val maxHr = 220 - 30
        val lowHr = (maxHr * 0.40).toInt()
        
        assertEquals(HeartRateZone.REST, HeartRateZone.fromHeartRate(lowHr, 30))
    }

    @Test
    fun `fromHeartRate returns FAT_BURN for fat burn zone`() {
        val age = 30
        val maxHr = 220 - age
        val fatBurnHr = (maxHr * 0.55).toInt()
        
        assertEquals(HeartRateZone.FAT_BURN, HeartRateZone.fromHeartRate(fatBurnHr, age))
    }

    @Test
    fun `fromHeartRate returns CARDIO for cardio zone`() {
        val age = 30
        val maxHr = 220 - age
        val cardioHr = (maxHr * 0.65).toInt()
        
        assertEquals(HeartRateZone.CARDIO, HeartRateZone.fromHeartRate(cardioHr, age))
    }

    @Test
    fun `fromHeartRate returns HARD for hard zone`() {
        val age = 30
        val maxHr = 220 - age
        val hardHr = (maxHr * 0.75).toInt()
        
        assertEquals(HeartRateZone.HARD, HeartRateZone.fromHeartRate(hardHr, age))
    }

    @Test
    fun `fromHeartRate returns MAXIMUM for maximum zone`() {
        val age = 30
        val maxHr = 220 - age
        val maximumHr = (maxHr * 0.85).toInt()
        
        assertEquals(HeartRateZone.MAXIMUM, HeartRateZone.fromHeartRate(maximumHr, age))
    }

    @Test
    fun `fromHeartRate returns EXTREME for extreme zone`() {
        val age = 30
        val maxHr = 220 - age
        val extremeHr = (maxHr * 0.95).toInt()
        
        assertEquals(HeartRateZone.EXTREME, HeartRateZone.fromHeartRate(extremeHr, age))
    }

    @Test
    fun `fromHeartRate returns EXTREME for very high heart rate`() {
        val age = 30
        val veryHighHr = 210
        
        assertEquals(HeartRateZone.EXTREME, HeartRateZone.fromHeartRate(veryHighHr, age))
    }

    @Test
    fun `displayName is correct for all zones`() {
        assertEquals("Отдых", HeartRateZone.REST.displayName)
        assertEquals("Жиросжигание", HeartRateZone.FAT_BURN.displayName)
        assertEquals("Кардио", HeartRateZone.CARDIO.displayName)
        assertEquals("Интенсивная", HeartRateZone.HARD.displayName)
        assertEquals("Максимальная", HeartRateZone.MAXIMUM.displayName)
        assertEquals("Экстремальная", HeartRateZone.EXTREME.displayName)
    }

    @Test
    fun `percentage ranges are correct`() {
        assertTrue(HeartRateZone.REST.maxPercentage <= HeartRateZone.FAT_BURN.minPercentage)
        assertTrue(HeartRateZone.FAT_BURN.maxPercentage <= HeartRateZone.CARDIO.minPercentage)
        assertTrue(HeartRateZone.CARDIO.maxPercentage <= HeartRateZone.HARD.minPercentage)
        assertTrue(HeartRateZone.HARD.maxPercentage <= HeartRateZone.MAXIMUM.minPercentage)
        assertTrue(HeartRateZone.MAXIMUM.maxPercentage <= HeartRateZone.EXTREME.minPercentage)
    }
}

class SmartwatchSessionSummaryTest {

    @Test
    fun `durationFormatted formats duration correctly`() {
        val summary = SmartwatchSessionSummary(
            startTime = 0L,
            endTime = 125000L,
            avgHeartRate = 130,
            minHeartRate = 100,
            maxHeartRate = 160,
            totalCalories = 250.0,
            totalSteps = null,
            totalDistance = null,
            totalDuration = 125000L,
            dominantHeartRateZone = HeartRateZone.CARDIO,
            heartRateZoneDistribution = emptyMap()
        )
        
        assertEquals("2:05", summary.durationFormatted)
    }

    @Test
    fun `durationFormatted handles zero duration`() {
        val summary = SmartwatchSessionSummary(
            startTime = 0L,
            endTime = 0L,
            avgHeartRate = null,
            minHeartRate = null,
            maxHeartRate = null,
            totalCalories = null,
            totalSteps = null,
            totalDistance = null,
            totalDuration = 0L,
            dominantHeartRateZone = null,
            heartRateZoneDistribution = emptyMap()
        )
        
        assertEquals("0:00", summary.durationFormatted)
    }
}

class ExerciseStatsBiometricsTest {

    @Test
    fun `hasBiometrics returns false when no biometric data`() {
        val stats = ExerciseStats(
            exerciseName = "Test",
            date = System.currentTimeMillis(),
            weight = 50.0,
            reps = 10,
            setNumber = 1,
            sets = 3
        )
        
        assertFalse(stats.hasBiometrics)
    }

    @Test
    fun `hasBiometrics returns true when avgHeartRate is set`() {
        val stats = ExerciseStats(
            exerciseName = "Test",
            date = System.currentTimeMillis(),
            weight = 50.0,
            reps = 10,
            setNumber = 1,
            sets = 3,
            avgHeartRate = 130
        )
        
        assertTrue(stats.hasBiometrics)
    }

    @Test
    fun `hasBiometrics returns true when caloriesBurned is set`() {
        val stats = ExerciseStats(
            exerciseName = "Test",
            date = System.currentTimeMillis(),
            weight = 50.0,
            reps = 10,
            setNumber = 1,
            sets = 3,
            caloriesBurned = 150.0
        )
        
        assertTrue(stats.hasBiometrics)
    }

    @Test
    fun `hasBiometrics returns true when both biometrics are set`() {
        val stats = ExerciseStats(
            exerciseName = "Test",
            date = System.currentTimeMillis(),
            weight = 50.0,
            reps = 10,
            setNumber = 1,
            sets = 3,
            avgHeartRate = 130,
            caloriesBurned = 150.0
        )
        
        assertTrue(stats.hasBiometrics)
    }
}
