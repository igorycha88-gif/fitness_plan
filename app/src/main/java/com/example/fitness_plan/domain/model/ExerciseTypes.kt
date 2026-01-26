package com.example.fitness_plan.domain.model

enum class ExerciseType(val displayName: String) {
    STRENGTH("Силовые"),
    CARDIO("Кардио"),
    STRETCHING("Растяжка")
}

enum class EquipmentType(
    val displayName: String,
    val imageName: String
) {
    BODYWEIGHT("Собственный вес", "equipment_bodyweight"),
    DUMBBELLS("Гантели", "equipment_dumbbells"),
    CABLE_MACHINE("Тренажёр с тросами", "equipment_cable_machine"),
    BARBELL("Штанга", "equipment_barbell"),
    LEVER_MACHINE("Тренажёр с рычагами", "equipment_lever_machine"),
    EXPANDER("Эспандер", "equipment_expander"),
    KETTLEBELL("Гиря", "equipment_kettlebell"),
    SPECIAL_BENCH("Специальная скамья", "equipment_special_bench"),
    WEIGHT_PLATES("С отягощениями", "equipment_weight_plates"),
    TRX("Подвеска TRX", "equipment_trx"),
    SPECIAL_BARBELL("Специальная штанга", "equipment_special_barbell"),
    SMITH_MACHINE("Смит-машина", "equipment_smith_machine"),
    FOAM_ROLLER("Форм роллер", "equipment_foam_roller"),
    SOCCER_BALL("Футбол", "equipment_soccer_ball"),
    EZ_BARBELL("EZ-штанга", "equipment_ez_barbell"),
    SPECIAL_MACHINE("Специальный тренажёр", "equipment_special_machine"),
    LANDESMINE_MACHINE("Тренажёр Лэндмайн", "equipment_landesmine_machine"),
    STICK("Палка", "equipment_stick"),
    MEDICINE_BALL("Медицинский мяч", "equipment_medicine_ball"),
    ROWING_MACHINE("Тяговая машина", "equipment_rowing_machine"),
    TRAPEZE_BAR("Штанга-трапеция", "equipment_trapeze_bar"),
    JUMP_ROPE("Скакалка", "equipment_jump_rope"),
    AB_ROLLER("Ролик для пресса", "equipment_ab_roller"),
    TREADMILL("Беговая дорожка", "equipment_treadmill"),
    ROLLING_BALL("Роликовый мяч", "equipment_rolling_ball")
}

enum class MuscleGroup(
    val displayName: String,
    val imageName: String
) {
    CHEST("Грудь", "muscle_chest"),
    TRICEPS("Трицепсы", "muscle_triceps"),
    LATS("Широчайшие", "muscle_lats"),
    BICEPS("Бицепсы", "muscle_biceps"),
    SHOULDERS("Плечи", "muscle_shoulders"),
    ABS("Пресс", "muscle_abs"),
    FOREARMS("Предплечья", "muscle_forearms"),
    TRAPS("Трапеции", "muscle_traps"),
    GLUTES("Ягодицы", "muscle_glutes"),
    QUADS("Квадрицепсы", "muscle_quads"),
    HAMSTRINGS("Бёдра сзади", "muscle_hamstrings"),
    CALVES("Икры", "muscle_calves"),
    LOWER_BACK("Поясница", "muscle_lower_back"),
    BRACHIALIS("Плечелучевая", "muscle_brachialis")
}
