package com.example.fitness_plan.domain.calculator

import com.example.fitness_plan.domain.model.ExerciseStats
import kotlin.math.abs

enum class ExerciseType {
    BASE,
    ISOLATION,
    COMPOUND,
    CARDIO
}

data class WeightRecommendation(
    val recommendedWeight: Float,
    val recommendedReps: List<Int>,
    val description: String
)

class WeightCalculator {
    
    companion object {
        private val STANDARD_DUMBBELL_WEIGHTS = listOf(
            1.25f, 2.5f, 3.75f, 5f, 6.25f, 7.5f, 8.75f, 10f, 11.25f, 12.5f, 13.75f, 15f,
            17.5f, 20f, 22.5f, 25f, 27.5f, 30f, 32.5f, 35f, 37.5f, 40f, 45f, 50f, 55f, 60f
        )
        
        private val STANDARD_BARBELL_WEIGHTS = listOf(
            2.5f, 5f, 7.5f, 10f, 15f, 20f, 25f, 30f, 35f, 40f, 45f, 50f, 60f, 70f, 80f, 90f, 100f
        )
    }
    
    fun calculateBaseWeight(
        bodyWeight: Float,
        level: String,
        goal: String,
        gender: String,
        exerciseType: ExerciseType
    ): Float {
        if (bodyWeight <= 0) return 10f
        
        val basePercentage = getBasePercentage(level, gender, exerciseType)
        val levelModifier = getLevelModifier(level)
        val goalModifier = getGoalModifier(goal)
        val genderModifier = getGenderModifier(gender)
        
        val calculatedWeight = bodyWeight * basePercentage * levelModifier * goalModifier * genderModifier
        
        return roundToStandardWeight(calculatedWeight.coerceAtLeast(1f))
    }
    
    fun calculateAdaptiveWeight(
        exerciseName: String,
        history: List<ExerciseStats>,
        baseReps: List<Int>
    ): Float? {
        if (history.size < 2) return null
        
        val lastTwo = history.takeLast(2)
        val avgReps = lastTwo.map { it.reps }.average()
        val avgWeight = lastTwo.map { it.weight }.average()
        val targetReps = baseReps.average()
        
        if (avgReps >= targetReps + 2) {
            val newWeight = roundToStandardWeight(avgWeight.toFloat() + 1.25f)
            return newWeight
        }
        
        return null
    }
    
    fun roundToStandardWeight(weight: Float): Float {
        val allStandardWeights = (STANDARD_DUMBBELL_WEIGHTS + STANDARD_BARBELL_WEIGHTS).sorted()
        
        var closest = allStandardWeights[0]
        var minDiff = abs(weight - closest)
        
        for (w in allStandardWeights) {
            val diff = abs(weight - w)
            if (diff < minDiff) {
                minDiff = diff
                closest = w
            }
        }
        
        return closest
    }
    
    fun getRecommendedReps(level: String): List<Int> {
        return when (level) {
            "Новичок" -> listOf(12, 13, 14)
            "Любитель" -> listOf(10, 11, 12)
            "Профессионал" -> listOf(6, 8, 10)
            else -> listOf(12, 13, 14)
        }
    }
    
    fun getRecommendedRepsString(level: String): String {
        return getRecommendedReps(level).joinToString(",")
    }
    
    fun determineExerciseType(exerciseName: String): ExerciseType {
        val lowerName = exerciseName.lowercase()
        
        val baseKeywords = listOf(
            "приседание", "жим лёжа", "становая тяга", "тяга штанги"
        )
        
        val isolationKeywords = listOf(
            "бицепс", "трицепс", "икры", "планка", "велосипед", "скручивание",
            "разведения в стороны", "обратные разведения", "концентрированные сгибания"
        )
        
        val cardioKeywords = listOf(
            "кардо", "эллипс", "велотренажёр", "беговая", "гребной", "hiit", 
            "интервал", "бег", "ходьба"
        )
        
        return when {
            cardioKeywords.any { lowerName.contains(it) } -> ExerciseType.CARDIO
            baseKeywords.any { lowerName.contains(it) } -> ExerciseType.BASE
            isolationKeywords.any { lowerName.contains(it) } -> ExerciseType.ISOLATION
            else -> ExerciseType.COMPOUND
        }
    }
    
    fun getWeightRecommendation(
        bodyWeight: Float,
        level: String,
        goal: String,
        gender: String,
        exerciseName: String,
        history: List<ExerciseStats> = emptyList()
    ): WeightRecommendation? {
        val exerciseType = determineExerciseType(exerciseName)
        
        if (exerciseType == ExerciseType.CARDIO) {
            return null
        }
        
        val recommendedReps = getRecommendedReps(level)
        
        val adaptiveWeight = calculateAdaptiveWeight(exerciseName, history, recommendedReps)
        val baseWeight = if (bodyWeight > 0) {
            calculateBaseWeight(bodyWeight, level, goal, gender, exerciseType)
        } else {
            10f
        }
        
        val finalWeight = adaptiveWeight ?: baseWeight
        val description = if (adaptiveWeight != null) {
            "Адаптивный вес (на основе истории)"
        } else {
            "Рекомендуемый вес (на основе вашего уровня)"
        }
        
        return WeightRecommendation(
            recommendedWeight = finalWeight,
            recommendedReps = recommendedReps,
            description = description
        )
    }
    
    private fun getBasePercentage(level: String, gender: String, exerciseType: ExerciseType): Float {
        val isMale = gender == "Мужской"
        
        return when (exerciseType) {
            ExerciseType.BASE -> when {
                level == "Новичок" -> if (isMale) 0.5f else 0.35f
                level == "Любитель" -> if (isMale) 0.6f else 0.45f
                level == "Профессионал" -> if (isMale) 0.7f else 0.55f
                else -> if (isMale) 0.5f else 0.35f
            }
            ExerciseType.ISOLATION -> when {
                level == "Новичок" -> if (isMale) 0.2f else 0.15f
                level == "Любитель" -> if (isMale) 0.3f else 0.22f
                level == "Профессионал" -> if (isMale) 0.4f else 0.3f
                else -> if (isMale) 0.2f else 0.15f
            }
            ExerciseType.COMPOUND -> when {
                level == "Новичок" -> if (isMale) 0.35f else 0.25f
                level == "Любитель" -> if (isMale) 0.45f else 0.35f
                level == "Профессионал" -> if (isMale) 0.55f else 0.45f
                else -> if (isMale) 0.35f else 0.25f
            }
            ExerciseType.CARDIO -> 0f
        }
    }
    
    private fun getLevelModifier(level: String): Float {
        return when (level) {
            "Новичок" -> 1.0f
            "Любитель" -> 1.2f
            "Профессионал" -> 1.4f
            else -> 1.0f
        }
    }
    
    private fun getGoalModifier(goal: String): Float {
        return when (goal) {
            "Похудение" -> 0.8f
            "Наращивание мышечной массы" -> 1.2f
            "Поддержание формы" -> 1.0f
            else -> 1.0f
        }
    }
    
    private fun getGenderModifier(gender: String): Float {
        return when (gender) {
            "Мужской" -> 1.0f
            "Женский" -> 0.75f
            else -> 1.0f
        }
    }
}