package com.example.fitness_plan.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutPlanRepository @Inject constructor() {

    fun getWorkoutPlanForUser(userProfile: UserProfile): WorkoutPlan {
        return when {
            userProfile.goal == "Похудение" && userProfile.level == "Новичок" && userProfile.frequency == "3 раза в неделю" -> fatLossBeginnerPlan
            else -> fatLossBeginnerPlan
        }
    }

    fun getWorkoutPlanWithDates(basePlan: WorkoutPlan, dates: List<Long>): WorkoutPlan {
        if (dates.isEmpty() || basePlan.weeks.isEmpty()) return basePlan

        val totalDays = basePlan.weeks.sumOf { it.workoutPlan.days.size }
        if (dates.size < totalDays) return basePlan

        var dateIndex = 0
        val updatedWeeks = basePlan.weeks.map { week ->
            week.copy(
                workoutPlan = week.workoutPlan.copy(
                    days = week.workoutPlan.days.map { day ->
                        day.copy(
                            scheduledDate = if (dateIndex < dates.size) dates[dateIndex++] else null
                        )
                    }
                )
            )
        }

        return basePlan.copy(weeks = updatedWeeks)
    }

    // === АЛЬТЕРНАТИВНЫЕ УПРАЖНЕНИЯ ===

    // НОГИ
    private val squatAlternatives = listOf(
        Exercise(name = "Приседания с гантелями", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_dumbbell_squat", muscleGroups = listOf("Ноги"), description = "Возьмите гантели в руки, встаньте прямо. Опуститесь вниз, сгибая колени до параллели с полом, затем вернитесь вверх. Держите спину прямо."),
        Exercise(name = "Жим ногами на тренажёре", sets = 3, reps = "12-15", weight = "Лёгкий-средний", imageRes = "exercise_leg_press", muscleGroups = listOf("Ноги"), description = "Сядьте на тренажёр, поставьте ноги на платформу на ширине плеч. Опустите платформу, сгибая колени, затем выжмите её вверх. Не блокируйте колени полностью."),
        Exercise(name = "Выпады с гантелями", sets = 3, reps = "10 на ногу", weight = "Лёгкий", imageRes = "exercise_lunges", muscleGroups = listOf("Ноги"), description = "Возьмите гантели в руки. Сделайте шаг вперёд одной ногой и опуститесь, пока заднее колено не коснётся пола. Вернитесь в исходное положение.")
    )

    private val legCurlAlternatives = listOf(
        Exercise(name = "Сгибания ног лёжа", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_leg_curl", muscleGroups = listOf("Ноги"), description = "Лягте на живот на скамью. Согните ноги в коленях, подтягивая вес к ягодицам, затем медленно опустите."),
        Exercise(name = "Сгибания ног сидя", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_seated_curl", muscleGroups = listOf("Ноги"), description = "Сядьте на тренажёр, закрепите валик на лодыжках. Согните ноги, подтягивая вес к ягодицам, затем опустите."),
        Exercise(name = "Мостик на одной ноге", sets = 3, reps = "10 на ногу", weight = "Без веса", imageRes = "exercise_single_leg_glute", muscleGroups = listOf("Ноги"), description = "Лягте на спину, одну ногу согните. Поднимите таз вверх, опираясь на пятку согнутой ноги, затем опустите.")
    )

    private val lungesAlternatives = listOf(
        Exercise(name = "Зашагивания на платформу", sets = 3, reps = "10 на ногу", weight = "Лёгкий", imageRes = "exercise_step_up", muscleGroups = listOf("Ноги"), description = "Встаньте перед платформой. Поставьте одну ногу на платформу и зашагайте, затем вернитесь. Работайте в темпе."),
        Exercise(name = "Приседания плие", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_plie_squat", muscleGroups = listOf("Ноги"), description = "Встаньте широко, носки развёрнуты наружу. Опуститесь вниз, сгибая колени, затем вернитесь вверх. Акцент на внутреннюю поверхность бедра."),
        Exercise(name = "Ягодичный мостик", sets = 3, reps = "15-20", weight = "Лёгкий", imageRes = "exercise_glute_bridge", muscleGroups = listOf("Ноги"), description = "Лягте на спину, согните колени. Поднимите таз вверх, сжимая ягодицы, затем опустите. Можно добавить гантель на бёдра.")
    )

    // ГРУДЬ
    private val benchPressAlternatives = listOf(
        Exercise(name = "Жим штанги лежа", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_barbell_bench", muscleGroups = listOf("Грудь"), description = "Лягте на скамью, возьмите штангу. Опустите её к груди, затем выжмите вверх. Локти под углом 45 градусов к телу."),
        Exercise(name = "Жим гантелей на наклонной скамье", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_incline_dumbbell", muscleGroups = listOf("Грудь"), description = "Лягте на наклонную скамью (30-45 градусов). Возьмите гантели и выжмите их вверх, затем опустите к груди. Акцент на верхнюю часть груди."),
        Exercise(name = "Отжимания от пола", sets = 3, reps = "15-20", weight = "Без веса", imageRes = "exercise_pushups", muscleGroups = listOf("Грудь"), description = "Встаньте в упор лёжа, руки на ширине плеч. Опуститесь вниз, касаясь грудью пола, затем выжмитесь вверх. Держите тело прямо.")
    )

    private val pushupsAlternatives = listOf(
        Exercise(name = "Отжимания с колен", sets = 3, reps = "12-15", weight = "Облегчённый", imageRes = "exercise_knee_pushups", muscleGroups = listOf("Грудь"), description = "Встаньте в упор лёжа на коленях. Опуститесь вниз, затем выжмитесь вверх. Легче чем классические отжимания."),
        Exercise(name = "Жим гантелей на горизонтальной скамье", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_dumbbell_press", muscleGroups = listOf("Грудь"), description = "Лягте на скамью, возьмите гантели. Выжмите их вверх, затем опустите к груди. Контролируйте движение."),
        Exercise(name = "Пуловер с гантелью", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_pullover", muscleGroups = listOf("Грудь"), description = "Лягте на скамью, держите гантель над грудью. Опустите её за голову, растягивая грудь, затем верните в исходное положение.")
    )

    // СПИНА
    private val latPulldownAlternatives = listOf(
        Exercise(name = "Подтягивания", sets = 3, reps = "6-10", weight = "Собственный вес", imageRes = "exercise_pullups", muscleGroups = listOf("Спина"), description = "Повисните на турнике хватом чуть шире плеч. Подтянитесь вверх, пока подбородок не окажется над перекладиной, затем опуститесь."),
        Exercise(name = "Тяга гантели в наклоне", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_bent_over_row", muscleGroups = listOf("Спина"), description = "Наклонитесь вперёд (корпус параллелен полу), возьмите гантель. Подтяните её к поясу, сжимая лопатки, затем опустите."),
        Exercise(name = "Тяга штанги в наклоне", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_barbell_row", muscleGroups = listOf("Спина"), description = "Наклонитесь вперёд, возьмите штангу. Подтяните её к поясу, держа спину прямо, затем опустите. Локти направлены назад.")
    )

    private val rowAlternatives = listOf(
        Exercise(name = "Тяга к поясу в тренажёре", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_cable_row", muscleGroups = listOf("Спина"), description = "Сядьте в тренажёр, возьмите рукоятку. Тяните её к поясу, сжимая лопатки, затем отпустите. Держите спину прямо."),
        Exercise(name = "Лодка", sets = 3, reps = "12-15", weight = "Без веса", imageRes = "exercise_superman", muscleGroups = listOf("Спина"), description = "Лягте на живот. Одновременно поднимите руки и ноги вверх, удерживайте 2-3 секунды, затем опустите. Акцент на нижней части спины."),
        Exercise(name = "Тяга гантели одной рукой", sets = 3, reps = "12 на руку", weight = "Лёгкий", imageRes = "exercise_single_arm_row", muscleGroups = listOf("Спина"), description = "Обопритесь коленом и рукой о скамью. Возьмите гантель и подтяните её к поясу, затем опустите. Работайте в темпе.")
    )

    // ПЛЕЧИ
    private val shoulderPressAlternatives = listOf(
        Exercise(name = "Жим штанги стоя", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_barbell_shoulder", muscleGroups = listOf("Плечи"), description = "Возьмите штангу на плечи. Выжмите её вверх над головой, затем опустите на плечи. Не отклоняйтесь назад."),
        Exercise(name = "Разведение гантелей в стороны", sets = 3, reps = "12-15", weight = "Очень лёгкий", imageRes = "exercise_lateral_raise", muscleGroups = listOf("Плечи"), description = "Встаньте прямо, возьмите гантели. Разведите руки в стороны до уровня плеч, затем опустите. Лёгкий изгиб в локтях."),
        Exercise(name = "Армейский жим с гантелями", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_dumbbell_shoulder", muscleGroups = listOf("Плечи"), description = "Возьмите гантели на плечи. Выжмите их вверх над головой, затем опустите на плечи. Контролируйте движение в обе стороны.")
    )

    private val lateralRaiseAlternatives = listOf(
        Exercise(name = "Разведение в наклоне", sets = 3, reps = "12-15", weight = "Очень лёгкий", imageRes = "exercise_rear_delt", muscleGroups = listOf("Плечи"), description = "Наклонитесь вперёд, возьмите гантели. Разведите руки в стороны, сжимая задние дельты, затем опустите."),
        Exercise(name = "Тяга к подбородку", sets = 3, reps = "12-15", weight = "Лёгкий", imageRes = "exercise_upright_row", muscleGroups = listOf("Плечи"), description = "Встаньте прямо, возьмите гантели. Тяните их вверх к подбородку, локти направлены вверх и в стороны, затем опустите."),
        Exercise(name = "Передние подъёмы", sets = 3, reps = "12-15", weight = "Очень лёгкий", imageRes = "exercise_front_raise", muscleGroups = listOf("Плечи"), description = "Встаньте прямо, возьмите гантели. Поднимите одну руку вперёд до уровня плеча, затем опустите. Работайте по очереди или вместе.")
    )

    // ПРЕСС
    private val plankAlternatives = listOf(
        Exercise(name = "Русские скручивания", sets = 3, reps = "15-20", weight = "Без веса", imageRes = "exercise_russian_twist", muscleGroups = listOf("Пресс"), description = "Сядьте на пол, согните колени. Отклонитесь назад и скручивайте корпус в стороны, касаясь руками пола. Держите спину прямо."),
        Exercise(name = "Подъём ног лёжа", sets = 3, reps = "12-15", weight = "Без веса", imageRes = "exercise_leg_raises", muscleGroups = listOf("Пресс"), description = "Лягте на спину, руки под ягодицами. Поднимите ноги вверх до вертикали, затем опустите. Не касайтесь пола."),
        Exercise(name = "Боковая планка", sets = 3, reps = "20-30 сек", weight = "Без веса", imageRes = "exercise_side_plank", muscleGroups = listOf("Пресс"), description = "Лягте на бок, обопритесь на локоть и боковую часть стопы. Поднимите тело вверх, удерживая прямую линию. Переключитесь на другую сторону.")
    )

    private val legRaisesAlternatives = listOf(
        Exercise(name = "Велосипед", sets = 3, reps = "20-30", weight = "Без веса", imageRes = "exercise_bicycle", muscleGroups = listOf("Пресс"), description = "Лягте на спину, руки за головой. Поднимите ноги и имитируйте езду на велосипеде, касаясь локтями коленей."),
        Exercise(name = "Подъём корпуса", sets = 3, reps = "15-20", weight = "Без веса", imageRes = "exercise_crunches", muscleGroups = listOf("Пресс"), description = "Лягте на спину, согните колени. Скрутите корпус, поднимая плечи к коленям, затем опустите. Руки за головой, не тяните шею."),
        Exercise(name = "Планка с касанием плеча", sets = 3, reps = "10 на плечо", weight = "Без веса", imageRes = "exercise_plank_tap", muscleGroups = listOf("Пресс"), description = "Встаньте в упор лёжа. Поочерёдно касайтесь левым и правым плечом противоположной рукой. Держите бёдра неподвижно.")
    )

    // КАРДИО
    private val cardioAlternatives = listOf(
        Exercise(name = "Велотренажёр", sets = 1, reps = "15-20 мин", weight = "Лёгкая нагрузка", imageRes = "exercise_bike", muscleGroups = listOf("Кардио"), description = "Крутите педали в комфортном темпе. Пульс 120-140 уд/мин. Регулируйте сопротивление для нужной интенсивности."),
        Exercise(name = "Эллиптический тренажёр", sets = 1, reps = "15-20 мин", weight = "Лёгкая нагрузка", imageRes = "exercise_elliptical", muscleGroups = listOf("Кардио"), description = "Встаньте на педали и двигайтесь в эллиптической траектории. Руками также работайте. Пульс 120-140 уд/мин."),
        Exercise(name = "Бёрпи", sets = 3, reps = "10-12", weight = "Средняя нагрузка", imageRes = "exercise_burpee", muscleGroups = listOf("Кардио"), description = "Из положения стоя опуститесь в упор лёжа, сделайте отжимание, прыжком вернитесь вверх и хлопните над головой. Работайте в темпе.")
    )

    private val burpeeAlternatives = listOf(
        Exercise(name = "Прыжки на месте", sets = 3, reps = "30 сек", weight = "Средняя нагрузка", imageRes = "exercise_jump_jack", muscleGroups = listOf("Кардио"), description = "Прыгайте на месте, разводя руки и ноги в стороны, затем возвращаясь. Работайте 30 секунд, отдых 30 секунд."),
        Exercise(name = "Прыжки на скакалке", sets = 1, reps = "5-10 мин", weight = "Лёгкая нагрузка", imageRes = "exercise_jump_rope", muscleGroups = listOf("Кардио"), description = "Прыгайте через скакалку в умеренном темпе. Если нет скакалки - имитируйте движение без неё."),
        Exercise(name = "Горка", sets = 3, reps = "30 сек", weight = "Средняя нагрузка", imageRes = "exercise_mountain_climber", muscleGroups = listOf("Кардио"), description = "Встаньте в упор лёжа. Поочерёдно подтягивайте колени к груди в быстром темпе. Спина прямая, бёдра не поднимаются.")
    )

    // === 4-НЕДЕЛЬНЫЙ ПЛАН ТРЕНИРОВОК ===

    private val fatLossBeginnerPlan = WorkoutPlan(
        id = "fat_loss_beginner_4weeks",
        name = "Жиросжигание: Новичок (4 недели)",
        description = "4-недельная программа для жиросжигания. 3 тренировки в неделю, полная проработка тела. Акцент на лёгкий-средний вес, 12-15 повторений, кардио.",
        frequency = "3 раза в неделю",
        targetGoal = "Похудение",
        targetLevel = "Новичок",
        muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
        weeks = listOf(
            Week(
                weekNumber = 1,
                description = "Неделя 1: АДАПТАЦИЯ. Лёгкий вес, 12-15 повторений, пульс 60-70% от максимума.",
                focus = "Изучение техники, адаптация организма",
                workoutPlan = WorkoutPlan(
                    id = "fat_loss_w1",
                    name = "Неделя 1",
                    description = "Лёгкая нагрузка, акцент на технику выполнения.",
                    frequency = "3 раза в неделю",
                    targetGoal = "Похудение",
                    targetLevel = "Новичок",
                    muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                    days = listOf(
                        WorkoutDay(
                            dayName = "День 1: НОГИ + ГРУДЬ + ПРЕСС",
                            muscleGroups = listOf("Ноги", "Грудь", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Приседания со штангой", sets = 3, reps = "12-15", weight = "50-55%", rest = "90 сек", imageRes = "exercise_squat", muscleGroups = listOf("Ноги"), description = "Возьмите штангу на плечи. Опуститесь вниз, сгибая колени до параллели с полом, затем вернитесь вверх. Спина прямая, колени в стороны.", alternatives = squatAlternatives),
                                Exercise(name = "Жим гантелей лёжа", sets = 3, reps = "12-15", weight = "50%", rest = "90 сек", imageRes = "exercise_bench_press", muscleGroups = listOf("Грудь"), description = "Лягте на скамью, возьмите гантели. Опустите их к груди, затем выжмите вверх. Локти под углом 45 градусов.", alternatives = benchPressAlternatives),
                                Exercise(name = "Выпады с гантелями", sets = 3, reps = "10 на ногу", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_lunges", muscleGroups = listOf("Ноги"), description = "Возьмите гантели. Сделайте шаг вперёд, опуститесь до касания задним коленом пола, затем вернитесь.", alternatives = lungesAlternatives),
                                Exercise(name = "Тяга верхнего блока", sets = 3, reps = "12-15", weight = "50%", rest = "90 сек", imageRes = "exercise_lat_pulldown", muscleGroups = listOf("Спина"), description = "Сядьте к тренажёру, возьмите рукоятку широким хватом. Потяните её вниз к груди, затем отпустите. Лопатки сжимаются.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Подъём ног в висе", sets = 3, reps = "12-15", weight = "Без веса", rest = "60 сек", imageRes = "exercise_leg_raise", muscleGroups = listOf("Пресс"), description = "Повисните на турнике. Поднимите прямые ноги вверх до параллели с полом, затем опустите. Не раскачивайтесь.", alternatives = plankAlternatives),
                                Exercise(name = "Планка", sets = 3, reps = "30-45 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_plank", muscleGroups = listOf("Пресс"), description = "Встаньте в упор лёжа на локтях. Держите тело прямо, не прогибайтесь и не поднимайте таз.", alternatives = plankAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 2: СПИНА + ПЛЕЧИ + ПРЕСС",
                            muscleGroups = listOf("Спина", "Плечи", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Тяга штанги в наклоне", sets = 3, reps = "12-15", weight = "50%", rest = "90 сек", imageRes = "exercise_barbell_row", muscleGroups = listOf("Спина"), description = "Наклонитесь вперёд, возьмите штангу. Подтяните её к поясу, затем опустите. Спина прямая, колени согнуты.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Жим гантелей сидя", sets = 3, reps = "12-15", weight = "50%", rest = "90 сек", imageRes = "exercise_shoulder_press", muscleGroups = listOf("Плечи"), description = "Сядьте, возьмите гантели на плечи. Выжмите их вверх, затем опустите. Не отклоняйтесь назад.", alternatives = shoulderPressAlternatives),
                                Exercise(name = "Тяга гантели в наклоне", sets = 3, reps = "12-15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_bent_over_row", muscleGroups = listOf("Спина"), description = "Наклонитесь, возьмите гантель. Подтяните её к поясу, затем опустите. Локоть направлен назад.", alternatives = rowAlternatives),
                                Exercise(name = "Отжимания", sets = 3, reps = "10-15", weight = "Без веса", rest = "90 сек", imageRes = "exercise_pushups", muscleGroups = listOf("Грудь"), description = "Встаньте в упор лёжа. Опуститесь вниз, затем выжмитесь вверх. Тело прямо.", alternatives = pushupsAlternatives),
                                Exercise(name = "Боковая планка", sets = 3, reps = "20-30 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_side_plank", muscleGroups = listOf("Пресс"), description = "Лягте на бок, обопритесь на локоть. Поднимите тело, удерживайте прямую линию.", alternatives = plankAlternatives),
                                Exercise(name = "Скручивания", sets = 3, reps = "15-20", weight = "Без веса", rest = "60 сек", imageRes = "exercise_crunches", muscleGroups = listOf("Пресс"), description = "Лягте, согните колени. Скрутите корпус, поднимая плечи к коленям. Нижняя часть спины прижата к полу.", alternatives = plankAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 3: КОМПЛЕКС + КАРДИО",
                            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                            exercises = listOf(
                                Exercise(name = "Приседания с гантелями", sets = 4, reps = "15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_dumbbell_squat", muscleGroups = listOf("Ноги"), description = "Возьмите гантели, встаньте прямо. Опуститесь вниз, затем вернитесь вверх.", alternatives = squatAlternatives),
                                Exercise(name = "Жим лёжа узким хватом", sets = 3, reps = "12-15", weight = "50%", rest = "90 сек", imageRes = "exercise_close_grip", muscleGroups = listOf("Грудь", "Трицепс"), description = "Лягте, возьмите гантели. Опустите их к груди, выжимая вверх. Локти направлены в стороны.", alternatives = pushupsAlternatives),
                                Exercise(name = "Становая тяга", sets = 3, reps = "12-15", weight = "50%", rest = "90 сек", imageRes = "exercise_deadlift", muscleGroups = listOf("Спина", "Ноги"), description = "Встаньте перед штангой, наклонитесь, возьмите её. Поднимите штангу, держа спину прямо, затем опустите.", alternatives = rowAlternatives),
                                Exercise(name = "Тяга к подбородку", sets = 3, reps = "12-15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_upright_row", muscleGroups = listOf("Плечи"), description = "Встаньте прямо, возьмите гантели. Тяните их вверх к подбородку, локти вверх и в стороны.", alternatives = lateralRaiseAlternatives),
                                Exercise(name = "Горка на пресс", sets = 3, reps = "15-20", weight = "Без веса", rest = "60 сек", imageRes = "exercise_mountain_climber", muscleGroups = listOf("Пресс", "Кардио"), description = "Встаньте в упор лёжа. Поочерёдно подтягивайте колени к груди в быстром темпе.", alternatives = burpeeAlternatives),
                                Exercise(name = "Бёрпи", sets = 3, reps = "8-10", weight = "Средняя", rest = "60 сек", imageRes = "exercise_burpee", muscleGroups = listOf("Кардио"), description = "Опуститесь, отожмитесь, прыжком вернитесь вверх с хлопком. Работайте в темпе.", alternatives = burpeeAlternatives),
                                Exercise(name = "Прыжки на месте", sets = 3, reps = "30 сек", weight = "Средняя", rest = "60 сек", imageRes = "exercise_jump_jack", muscleGroups = listOf("Кардио"), description = "Прыгайте, разводя руки и ноги в стороны. Поддерживайте пульс 120-140.", alternatives = cardioAlternatives)
                            )
                        )
                    )
                )
            ),
            Week(
                weekNumber = 2,
                description = "Неделя 2: ПРОДОЛЖЕНИЕ. Сохраняем лёгкий вес, работаем над выносливостью.",
                focus = "Улучшение техники, увеличение объёма",
                workoutPlan = WorkoutPlan(
                    id = "fat_loss_w2",
                    name = "Неделя 2",
                    description = "Лёгкая нагрузка, увеличение повторений.",
                    frequency = "3 раза в неделю",
                    targetGoal = "Похудение",
                    targetLevel = "Новичок",
                    muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                    days = listOf(
                        WorkoutDay(
                            dayName = "День 1: НОГИ + ГРУДЬ + ПРЕСС",
                            muscleGroups = listOf("Ноги", "Грудь", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Приседания со штангой", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_squat", muscleGroups = listOf("Ноги"), description = "Техника прежняя, вес чуть больше.", alternatives = squatAlternatives),
                                Exercise(name = "Жим гантелей лёжа", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_bench_press", muscleGroups = listOf("Грудь"), description = "Контролируемое опускание, мощный подъём.", alternatives = benchPressAlternatives),
                                Exercise(name = "Сгибания ног лёжа", sets = 3, reps = "12-15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_leg_curl", muscleGroups = listOf("Ноги"), description = "Лягте на живот, согните ноги с весом.", alternatives = legCurlAlternatives),
                                Exercise(name = "Тяга верхнего блока", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_lat_pulldown", muscleGroups = listOf("Спина"), description = "Тяните к груди, сжимая лопатки.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Подъём ног в висе", sets = 3, reps = "12-15", weight = "Без веса", rest = "60 сек", imageRes = "exercise_leg_raise", muscleGroups = listOf("Пресс"), description = "Медленное опускание, контроль.", alternatives = plankAlternatives),
                                Exercise(name = "Планка", sets = 3, reps = "40-60 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_plank", muscleGroups = listOf("Пресс"), description = "Увеличиваем время удержания.", alternatives = plankAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 2: СПИНА + ПЛЕЧИ + ПРЕСС",
                            muscleGroups = listOf("Спина", "Плечи", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Тяга штанги в наклоне", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_barbell_row", muscleGroups = listOf("Спина"), description = "Спина параллельно полу.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Жим гантелей сидя", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_shoulder_press", muscleGroups = listOf("Плечи"), description = "Плечи параллельно полу в нижней точке.", alternatives = shoulderPressAlternatives),
                                Exercise(name = "Разведение гантелей", sets = 3, reps = "12-15", weight = "Очень лёгкий", rest = "90 сек", imageRes = "exercise_lateral_raise", muscleGroups = listOf("Плечи"), description = "Разводите до уровня плеч, не выше.", alternatives = lateralRaiseAlternatives),
                                Exercise(name = "Тяга гантели в наклоне", sets = 3, reps = "12-15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_bent_over_row", muscleGroups = listOf("Спина"), description = "Одна рука на скамье, тяга другой.", alternatives = rowAlternatives),
                                Exercise(name = "Боковая планка", sets = 3, reps = "25-35 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_side_plank", muscleGroups = listOf("Пресс"), description = "По 25-35 секунд на каждую сторону.", alternatives = plankAlternatives),
                                Exercise(name = "Велосипед", sets = 3, reps = "25-30", weight = "Без веса", rest = "60 сек", imageRes = "exercise_bicycle", muscleGroups = listOf("Пресс"), description = "Касайтесь локтями коленей.", alternatives = legRaisesAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 3: КОМПЛЕКС + КАРДИО",
                            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                            exercises = listOf(
                                Exercise(name = "Приседания с гантелями", sets = 4, reps = "15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_dumbbell_squat", muscleGroups = listOf("Ноги"), description = "Без остановки внизу.", alternatives = squatAlternatives),
                                Exercise(name = "Жим лёжа узким хватом", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_close_grip", muscleGroups = listOf("Грудь"), description = "Локти ближе к телу.", alternatives = pushupsAlternatives),
                                Exercise(name = "Становая тяга", sets = 3, reps = "12-15", weight = "55%", rest = "90 сек", imageRes = "exercise_deadlift", muscleGroups = listOf("Спина"), description = "Ноги чуть согнуты, спина прямая.", alternatives = rowAlternatives),
                                Exercise(name = "Тяга к подбородку", sets = 3, reps = "12-15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_upright_row", muscleGroups = listOf("Плечи"), description = "Локти выше кистей.", alternatives = lateralRaiseAlternatives),
                                Exercise(name = "Горка", sets = 3, reps = "30 сек", weight = "Средняя", rest = "60 сек", imageRes = "exercise_mountain_climber", muscleGroups = listOf("Пресс", "Кардио"), description = "Быстрый темп.", alternatives = burpeeAlternatives),
                                Exercise(name = "Бёрпи", sets = 3, reps = "10", weight = "Средняя", rest = "60 сек", imageRes = "exercise_burpee", muscleGroups = listOf("Кардио"), description = "Полное движение.", alternatives = burpeeAlternatives),
                                Exercise(name = "Прыжки на месте", sets = 3, reps = "30 сек", weight = "Средняя", rest = "60 сек", imageRes = "exercise_jump_jack", muscleGroups = listOf("Кардио"), description = "Поддерживайте пульс.", alternatives = cardioAlternatives)
                            )
                        )
                    )
                )
            ),
            Week(
                weekNumber = 3,
                description = "Неделя 3: ПРОГРЕССИЯ. Увеличиваем вес до 60-65%, уменьшаем повторения до 10-12.",
                focus = "Увеличение интенсивности, начало прогресса",
                workoutPlan = WorkoutPlan(
                    id = "fat_loss_w3",
                    name = "Неделя 3",
                    description = "Повышенная интенсивность, вес 60-65%.",
                    frequency = "3 раза в неделю",
                    targetGoal = "Похудение",
                    targetLevel = "Новичок",
                    muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                    days = listOf(
                        WorkoutDay(
                            dayName = "День 1: НОГИ + ГРУДЬ + ПРЕСС",
                            muscleGroups = listOf("Ноги", "Грудь", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Приседания со штангой", sets = 4, reps = "10-12", weight = "60-65%", rest = "90 сек", imageRes = "exercise_squat", muscleGroups = listOf("Ноги"), description = "Вес увеличен, 4 подхода.", alternatives = squatAlternatives),
                                Exercise(name = "Жим гантелей лёжа", sets = 4, reps = "10-12", weight = "60%", rest = "90 сек", imageRes = "exercise_bench_press", muscleGroups = listOf("Грудь"), description = "Медленнее в негативной фазе.", alternatives = benchPressAlternatives),
                                Exercise(name = "Выпады с гантелями", sets = 3, reps = "12 на ногу", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_lunges", muscleGroups = listOf("Ноги"), description = "Шаг чуть шире.", alternatives = lungesAlternatives),
                                Exercise(name = "Тяга верхнего блока", sets = 3, reps = "10-12", weight = "60%", rest = "90 сек", imageRes = "exercise_lat_pulldown", muscleGroups = listOf("Спина"), description = "Мощное сжатие вверху.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Ягодичный мостик", sets = 3, reps = "15-20", weight = "Лёгкий", rest = "60 сек", imageRes = "exercise_glute_bridge", muscleGroups = listOf("Ноги"), description = "Сжимайте ягодицы вверху.", alternatives = legCurlAlternatives),
                                Exercise(name = "Подъём ног в висе", sets = 3, reps = "12-15", weight = "Без веса", rest = "60 сек", imageRes = "exercise_leg_raise", muscleGroups = listOf("Пресс"), description = "Ноги прямые или согнутые.", alternatives = plankAlternatives),
                                Exercise(name = "Планка", sets = 4, reps = "40-50 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_plank", muscleGroups = listOf("Пресс"), description = "4 подхода.", alternatives = plankAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 2: СПИНА + ПЛЕЧИ + ПРЕСС",
                            muscleGroups = listOf("Спина", "Плечи", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Тяга штанги в наклоне", sets = 4, reps = "10-12", weight = "60-65%", rest = "90 сек", imageRes = "exercise_barbell_row", muscleGroups = listOf("Спина"), description = "Вес увеличен.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Жим гантелей сидя", sets = 4, reps = "10-12", weight = "60%", rest = "90 сек", imageRes = "exercise_shoulder_press", muscleGroups = listOf("Плечи"), description = "Полная амплитуда.", alternatives = shoulderPressAlternatives),
                                Exercise(name = "Тяга гантели в наклоне", sets = 3, reps = "10-12", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_bent_over_row", muscleGroups = listOf("Спина"), description = "Лопатка работающей руки к позвоночнику.", alternatives = rowAlternatives),
                                Exercise(name = "Разведение гантелей", sets = 3, reps = "12-15", weight = "Очень лёгкий", rest = "90 сек", imageRes = "exercise_lateral_raise", muscleGroups = listOf("Плечи"), description = "Плавное движение.", alternatives = lateralRaiseAlternatives),
                                Exercise(name = "Отжимания", sets = 3, reps = "12-15", weight = "Без веса", rest = "90 сек", imageRes = "exercise_pushups", muscleGroups = listOf("Грудь"), description = "Если легко - добавьте вес.", alternatives = pushupsAlternatives),
                                Exercise(name = "Боковая планка", sets = 3, reps = "30-40 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_side_plank", muscleGroups = listOf("Пресс"), description = "По 30-40 секунд на сторону.", alternatives = plankAlternatives),
                                Exercise(name = "Русские скручивания", sets = 3, reps = "20", weight = "Без веса", rest = "60 сек", imageRes = "exercise_russian_twist", muscleGroups = listOf("Пресс"), description = "Скручивания в стороны.", alternatives = legRaisesAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 3: КОМПЛЕКС + ИНТЕРВАЛЬНОЕ КАРДИО",
                            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                            exercises = listOf(
                                Exercise(name = "Приседания с гантелями", sets = 4, reps = "12", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_dumbbell_squat", muscleGroups = listOf("Ноги"), description = "Взрывной подъём.", alternatives = squatAlternatives),
                                Exercise(name = "Жим лёжа узким хватом", sets = 4, reps = "10-12", weight = "60%", rest = "90 сек", imageRes = "exercise_close_grip", muscleGroups = listOf("Грудь"), description = "Локти ближе к телу.", alternatives = pushupsAlternatives),
                                Exercise(name = "Становая тяга", sets = 4, reps = "10-12", weight = "60%", rest = "90 сек", imageRes = "exercise_deadlift", muscleGroups = listOf("Спина", "Ноги"), description = "Мощное движение.", alternatives = rowAlternatives),
                                Exercise(name = "Тяга к подбородку", sets = 3, reps = "12", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_upright_row", muscleGroups = listOf("Плечи"), description = "Локти выше.", alternatives = lateralRaiseAlternatives),
                                Exercise(name = "Зашагивания на платформу", sets = 3, reps = "10 на ногу", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_step_up", muscleGroups = listOf("Ноги"), description = "Работайте в темпе.", alternatives = lungesAlternatives),
                                Exercise(name = "Горка", sets = 4, reps = "30 сек", weight = "Средняя", rest = "30 сек", imageRes = "exercise_mountain_climber", muscleGroups = listOf("Пресс", "Кардио"), description = "Интервалы 30/30.", alternatives = burpeeAlternatives),
                                Exercise(name = "Бёрпи", sets = 3, reps = "10", weight = "Средняя", rest = "30 сек", imageRes = "exercise_burpee", muscleGroups = listOf("Кардио"), description = "Быстрый темп.", alternatives = burpeeAlternatives)
                            )
                        )
                    )
                )
            ),
            Week(
                weekNumber = 4,
                description = "Неделя 4: ЗАВЕРШЕНИЕ. Максимальная интенсивность, подготовка к следующему циклу.",
                focus = "Максимальная интенсивность, тест прогресса",
                workoutPlan = WorkoutPlan(
                    id = "fat_loss_w4",
                    name = "Неделя 4",
                    description = "Пиковая интенсивность, тест результатов.",
                    frequency = "3 раза в неделю",
                    targetGoal = "Похудение",
                    targetLevel = "Новичок",
                    muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                    days = listOf(
                        WorkoutDay(
                            dayName = "День 1: НОГИ + ГРУДЬ + ПРЕСС",
                            muscleGroups = listOf("Ноги", "Грудь", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Приседания со штангой", sets = 4, reps = "8-10", weight = "65-70%", rest = "90 сек", imageRes = "exercise_squat", muscleGroups = listOf("Ноги"), description = "Максимальный вес недели 4.", alternatives = squatAlternatives),
                                Exercise(name = "Жим гантелей лёжа", sets = 4, reps = "8-10", weight = "65%", rest = "90 сек", imageRes = "exercise_bench_press", muscleGroups = listOf("Грудь"), description = "Последний силовой недели.", alternatives = benchPressAlternatives),
                                Exercise(name = "Сгибания ног лёжа", sets = 3, reps = "12-15", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_leg_curl", muscleGroups = listOf("Ноги"), description = "Добиваем бицепс бедра.", alternatives = legCurlAlternatives),
                                Exercise(name = "Тяга верхнего блока", sets = 3, reps = "10-12", weight = "60%", rest = "90 сек", imageRes = "exercise_lat_pulldown", muscleGroups = listOf("Спина"), description = "Каждый подход до отказа.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Подъём ног в висе", sets = 4, reps = "12-15", weight = "Без веса", rest = "60 сек", imageRes = "exercise_leg_raise", muscleGroups = listOf("Пресс"), description = "Медленно опускайте.", alternatives = plankAlternatives),
                                Exercise(name = "Планка с касанием плеча", sets = 3, reps = "10 на плечо", weight = "Без веса", rest = "60 сек", imageRes = "exercise_plank_tap", muscleGroups = listOf("Пресс"), description = "По 10 касаний на каждую сторону.", alternatives = plankAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 2: СПИНА + ПЛЕЧИ + ПРЕСС",
                            muscleGroups = listOf("Спина", "Плечи", "Пресс"),
                            exercises = listOf(
                                Exercise(name = "Тяга штанги в наклоне", sets = 4, reps = "8-10", weight = "65-70%", rest = "90 сек", imageRes = "exercise_barbell_row", muscleGroups = listOf("Спина"), description = "Максимальное усилие.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Жим гантелей сидя", sets = 4, reps = "8-10", weight = "65%", rest = "90 сек", imageRes = "exercise_shoulder_press", muscleGroups = listOf("Плечи"), description = "Последний силовой.", alternatives = shoulderPressAlternatives),
                                Exercise(name = "Разведение гантелей", sets = 3, reps = "12-15", weight = "Очень лёгкий", rest = "90 сек", imageRes = "exercise_lateral_raise", muscleGroups = listOf("Плечи"), description = "Пиковое сокращение.", alternatives = lateralRaiseAlternatives),
                                Exercise(name = "Подтягивания", sets = 3, reps = "Максимум", weight = "Собственный вес", rest = "90 сек", imageRes = "exercise_pullups", muscleGroups = listOf("Спина"), description = "Сколько сможете.", alternatives = latPulldownAlternatives),
                                Exercise(name = "Боковая планка", sets = 3, reps = "40-50 сек", weight = "Без веса", rest = "60 сек", imageRes = "exercise_side_plank", muscleGroups = listOf("Пресс"), description = "Максимальное время.", alternatives = plankAlternatives),
                                Exercise(name = "Велосипед", sets = 3, reps = "30", weight = "Без веса", rest = "60 сек", imageRes = "exercise_bicycle", muscleGroups = listOf("Пресс"), description = "Быстрый темп.", alternatives = legRaisesAlternatives)
                            )
                        ),
                        WorkoutDay(
                            dayName = "День 3: КОМПЛЕКС + КАРДИО + ЗАВЕРШЕНИЕ",
                            muscleGroups = listOf("Ноги", "Грудь", "Спина", "Плечи", "Пресс", "Кардио"),
                            exercises = listOf(
                                Exercise(name = "Приседания с гантелями", sets = 3, reps = "12", weight = "Лёгкий", rest = "90 сек", imageRes = "exercise_dumbbell_squat", muscleGroups = listOf("Ноги"), description = "Лёгкий вес, много повторений.", alternatives = squatAlternatives),
                                Exercise(name = "Жим лёжа узким хватом", sets = 3, reps = "12", weight = "55%", rest = "90 сек", imageRes = "exercise_close_grip", muscleGroups = listOf("Грудь"), description = "Лёгкий вес.", alternatives = pushupsAlternatives),
                                Exercise(name = "Становая тяга", sets = 3, reps = "12", weight = "55%", rest = "90 сек", imageRes = "exercise_deadlift", muscleGroups = listOf("Спина"), description = "Техника важнее веса.", alternatives = rowAlternatives),
                                Exercise(name = "Интервальное кардио", sets = 8, reps = "20/40 сек", weight = "Средняя", rest = "20 сек", imageRes = "exercise_sprint", muscleGroups = listOf("Кардио"), description = "Спринт 20 сек / ходьба 40 сек. Пульс 140-150.", alternatives = cardioAlternatives),
                                Exercise(name = "Горка", sets = 4, reps = "30 сек", weight = "Средняя", rest = "30 сек", imageRes = "exercise_mountain_climber", muscleGroups = listOf("Пресс", "Кардио"), description = "Завершающий аккорд.", alternatives = burpeeAlternatives),
                                Exercise(name = "Планка", sets = 1, reps = "60 сек", weight = "Без веса", rest = "0 сек", imageRes = "exercise_plank", muscleGroups = listOf("Пресс"), description = "Финальная планка.", alternatives = plankAlternatives)
                            )
                        )
                    )
                )
            )
        )
    )
}
