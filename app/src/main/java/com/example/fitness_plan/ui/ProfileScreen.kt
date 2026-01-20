package com.example.fitness_plan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitness_plan.presentation.viewmodel.ProfileViewModel
import com.example.fitness_plan.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var currentUsername by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val adaptiveInfo = rememberAdaptiveInfo()
    val spacing = getSpacing()
    val cornerRadius = getCornerRadius()
    val cardElevation = getCardElevation()
    val iconSize = getIconSize()
    val screenPadding = getScreenPadding()

    LaunchedEffect(Unit) {
        currentUsername = viewModel.getCurrentUsername()
    }

    val tabs = listOf("Профиль", "Статистика", "Аккаунт")

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(
                    username = currentUsername,
                    profile = userProfile,
                    adaptiveInfo = adaptiveInfo,
                    cornerRadius = cornerRadius,
                    iconSize = iconSize,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceLight,
                    contentColor = FitnessPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Default.Person
                                        1 -> Icons.Default.List
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            item {
                when (selectedTab) {
                    0 -> ProfileContent(
                        userProfile = userProfile,
                        onEditClick = { selectedTab = 3 },
                        onWeightEditClick = { showWeightDialog = true },
                        adaptiveInfo = adaptiveInfo,
                        spacing = spacing,
                        cornerRadius = cornerRadius,
                        cardElevation = cardElevation
                    )
                    1 -> StatsContent(userProfile = userProfile)
                    2 -> AccountContent(
                        username = currentUsername,
                        onPasswordChange = { showPasswordDialog = true }
                    )
                    3 -> EditProfileContent(
                        userProfile = userProfile,
                        onSave = { profile ->
                            viewModel.saveUserProfile(profile)
                            selectedTab = 0
                        },
                        onCancel = { selectedTab = 0 }
                    )
                }
            }

            if (selectedTab != 3) {
                item {
                    LogoutButton(
                        onLogoutClick = {
                            viewModel.logout()
                            onLogoutClick()
                        },
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }

    if (showPasswordDialog) {
        ChangeCredentialsDialog(
            currentUsername = currentUsername,
            onDismiss = { showPasswordDialog = false },
            onSave = { newUsername, newPassword ->
                coroutineScope.launch {
                    viewModel.updateCredentials(currentUsername, newUsername, newPassword)
                    currentUsername = newUsername
                }
                showPasswordDialog = false
            }
        )
    }

    if (showWeightDialog && userProfile != null) {
        UpdateWeightDialog(
            currentWeight = userProfile!!.weight,
            onDismiss = { showWeightDialog = false },
            onSave = { newWeight ->
                coroutineScope.launch {
                    viewModel.saveWeightEntry(newWeight)
                }
                showWeightDialog = false
            }
        )
    }
}

@Composable
fun ProfileHeader(
    username: String,
    profile: com.example.fitness_plan.domain.model.UserProfile?,
    adaptiveInfo: AdaptiveInfo,
    cornerRadius: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val headerPadding = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 16.dp
        DeviceType.MEDIUM -> 24.dp
        DeviceType.EXPANDED -> 32.dp
    }
    val avatarSize = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 64.dp
        DeviceType.MEDIUM -> 72.dp
        DeviceType.EXPANDED -> 88.dp
    }
    val iconPadding = iconSize * 2
    val headerElevation = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 4.dp
        DeviceType.MEDIUM -> 6.dp
        DeviceType.EXPANDED -> 8.dp
    }

    Card(
        modifier = modifier.padding(headerPadding, 8.dp),
        shape = RoundedCornerShape(cornerRadius * 2),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = headerElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(FitnessPrimary.copy(alpha = 0.1f), FitnessSecondary.copy(alpha = 0.1f))
                    )
                )
                .padding(headerPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(FitnessPrimary, FitnessPrimaryDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextOnPrimary,
                        modifier = Modifier.size(avatarSize * 0.55f)
                    )
                }

                Spacer(modifier = Modifier.width(iconPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username.ifEmpty { "Пользователь" },
                        style = when (adaptiveInfo.deviceType) {
                            DeviceType.COMPACT -> MaterialTheme.typography.headlineSmall
                            DeviceType.MEDIUM -> MaterialTheme.typography.headlineMedium
                            DeviceType.EXPANDED -> MaterialTheme.typography.headlineLarge
                        },
                        fontWeight = FontWeight.Bold
                    )
                    profile?.let {
                        Text(
                            text = it.goal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitnessPrimaryDark
                        )
                    }
                }

                if (profile != null) {
                    QuickStatsColumn(profile = profile)
                }
            }
        }
    }
}

@Composable
fun QuickStatsColumn(profile: com.example.fitness_plan.domain.model.UserProfile) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = FitnessSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%.1f".format(profile.weight) + " кг",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = FitnessSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%.0f".format(profile.height) + " см",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProfileContent(
    userProfile: com.example.fitness_plan.domain.model.UserProfile?,
    onEditClick: () -> Unit,
    onWeightEditClick: () -> Unit,
    adaptiveInfo: AdaptiveInfo,
    spacing: Dp,
    cornerRadius: Dp,
    cardElevation: Dp
) {
    val scrollState = rememberScrollState()
    val horizontalPadding = when (adaptiveInfo.deviceType) {
        DeviceType.COMPACT -> 16.dp
        DeviceType.MEDIUM -> 24.dp
        DeviceType.EXPANDED -> 32.dp
    }
    val maxWidth = getContentMaxWidth()

    Column(
        modifier = Modifier
            .width(maxWidth)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        if (userProfile != null) {
            InfoCard(
                title = "Цель тренировок",
                icon = Icons.Default.CheckCircle,
                value = userProfile.goal,
                color = FitnessPrimary
            )

            InfoCard(
                title = "Уровень подготовки",
                icon = Icons.Default.ThumbUp,
                value = userProfile.level,
                color = FitnessTertiary
            )

            InfoCard(
                title = "Частота тренировок",
                icon = Icons.Default.DateRange,
                value = userProfile.frequency,
                color = FitnessSecondary
            )

            WeightCard(
                weight = userProfile.weight,
                onEditClick = onWeightEditClick,
                cornerRadius = cornerRadius,
                cardElevation = cardElevation
            )

            InfoCard(
                title = "Рост",
                icon = Icons.Default.Star,
                value = "%.0f".format(userProfile.height) + " см",
                color = FitnessSecondary
            )

            InfoCard(
                title = "Пол",
                icon = Icons.Default.Person,
                value = userProfile.gender,
                color = FitnessTertiary
            )

            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Редактировать профиль")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                colors = CardDefaults.cardColors(containerColor = WarningYellowLight)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningYellow
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Профиль не заполнен",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun WeightCard(
    weight: Double,
    onEditClick: () -> Unit,
    cornerRadius: Dp,
    cardElevation: Dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(InfoBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = InfoBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Текущий вес",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "%.1f".format(weight) + " кг",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            TextButton(onClick = onEditClick) {
                Text("Изменить")
            }
        }
    }
}

@Composable
fun StatsContent(userProfile: com.example.fitness_plan.domain.model.UserProfile?) {
    val spacing = getSpacing()
    val cornerRadius = getCornerRadius()
    val maxWidth = getContentMaxWidth()

    Column(
        modifier = Modifier
            .width(maxWidth)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Text(
            text = "Ваши показатели",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (userProfile != null) {
            val bmi = calculateBMI(userProfile.weight, userProfile.height)
            val bmiCategory = getBMICategory(bmi)

            val columns = getGridColumns()
            if (columns > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    StatCard(
                        title = "BMI",
                        value = "%.1f".format(bmi),
                        subtitle = bmiCategory,
                        color = when {
                            bmi < 18.5f -> InfoBlue
                            bmi < 25f -> SuccessGreen
                            bmi < 30f -> WarningYellow
                            else -> ErrorRed
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Вес",
                        value = "%.1f".format(userProfile.weight),
                        subtitle = "кг",
                        color = FitnessPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(spacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing)
                ) {
                    StatCard(
                        title = "Рост",
                        value = "%.0f".format(userProfile.height),
                        subtitle = "см",
                        color = FitnessSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Пол",
                        value = userProfile.gender,
                        subtitle = "",
                        color = FitnessTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                StatCard(
                    title = "BMI",
                    value = "%.1f".format(bmi),
                    subtitle = bmiCategory,
                    color = when {
                        bmi < 18.5f -> InfoBlue
                        bmi < 25f -> SuccessGreen
                        bmi < 30f -> WarningYellow
                        else -> ErrorRed
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing))

                StatCard(
                    title = "Вес",
                    value = "%.1f".format(userProfile.weight),
                    subtitle = "кг",
                    color = FitnessPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing))

                StatCard(
                    title = "Рост",
                    value = "%.0f".format(userProfile.height),
                    subtitle = "см",
                    color = FitnessSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing))

                StatCard(
                    title = "Пол",
                    value = userProfile.gender,
                    subtitle = "",
                    color = FitnessTertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(spacing))

            BMIExplanation(bmiCategory = bmiCategory, cornerRadius = cornerRadius)
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Заполните профиль для просмотра статистики",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AccountContent(
    username: String,
    onPasswordChange: () -> Unit
) {
    val spacing = getSpacing()
    val cornerRadius = getCornerRadius()
    val maxWidth = getContentMaxWidth()

    Column(
        modifier = Modifier
            .width(maxWidth)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Text(
            text = "Данные аккаунта",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AccountInfoRow(
                    icon = Icons.Default.Person,
                    label = "Логин",
                    value = username
                )

                Divider(modifier = Modifier.padding(vertical = spacing))

                AccountInfoRow(
                    icon = Icons.Default.Lock,
                    label = "Пароль",
                    value = "********",
                    showEdit = true,
                    onEdit = onPasswordChange
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ErrorRedLight),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = ErrorRed
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Безопасность аккаунта",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Ваши данные хранятся локально на устройстве",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun EditProfileContent(
    userProfile: com.example.fitness_plan.domain.model.UserProfile?,
    onSave: (com.example.fitness_plan.domain.model.UserProfile) -> Unit,
    onCancel: () -> Unit
) {
    var editedGoal by remember { mutableStateOf(userProfile?.goal ?: "") }
    var editedLevel by remember { mutableStateOf(userProfile?.level ?: "") }
    var editedFrequency by remember { mutableStateOf(userProfile?.frequency ?: "") }

    val goalOptions = listOf("Похудение", "Наращивание мышечной массы", "Поддержание формы")
    val levelOptions = listOf("Новичок", "Любитель", "Профессионал")
    val frequencyOptions = listOf("1 раз в неделю", "3 раза в неделю", "5 раз в неделю")

    val spacing = getSpacing()
    val cornerRadius = getCornerRadius()
    val maxWidth = getContentMaxWidth()

    Column(
        modifier = Modifier
            .width(maxWidth)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        Text(
            text = "Редактирование профиля",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (userProfile != null) {
            ProfileDropdown(
                label = "Цель тренировок",
                options = goalOptions,
                selectedOption = editedGoal,
                onOptionSelected = { editedGoal = it }
            )

            ProfileDropdown(
                label = "Уровень подготовки",
                options = levelOptions,
                selectedOption = editedLevel,
                onOptionSelected = { editedLevel = it }
            )

            ProfileDropdown(
                label = "Частота тренировок",
                options = frequencyOptions,
                selectedOption = editedFrequency,
                onOptionSelected = { editedFrequency = it }
            )

            Spacer(modifier = Modifier.height(spacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(cornerRadius),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        val updatedProfile = userProfile.copy(
                            goal = editedGoal,
                            level = editedLevel,
                            frequency = editedFrequency
                        )
                        onSave(updatedProfile)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(cornerRadius),
                    enabled = editedGoal.isNotEmpty() && editedLevel.isNotEmpty() && editedFrequency.isNotEmpty(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
fun LogoutButton(onLogoutClick: () -> Unit, cornerRadius: Dp) {
    val spacing = getSpacing()
    val buttonHeight = getButtonHeight()
    val maxWidth = getContentMaxWidth()

    Card(
        modifier = Modifier
            .width(maxWidth)
            .padding(horizontal = 16.dp, vertical = spacing),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Выйти из аккаунта")
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    value: String,
    color: Color
) {
    val cardElevation = getCardElevation()
    val cornerRadius = getCornerRadius()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val cardElevation = getCardElevation()
    val cornerRadius = getCornerRadius()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun AccountInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showEdit: Boolean = false,
    onEdit: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FitnessPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        if (showEdit && onEdit != null) {
            TextButton(onClick = onEdit) {
                Text("Изменить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cornerRadius = getCornerRadius()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(label) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FitnessPrimary,
                        focusedLabelColor = FitnessPrimary
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BMIExplanation(bmiCategory: String, cornerRadius: Dp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = InfoBlueLight),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = InfoBlue
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Индекс массы тела",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = bmiCategory,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun UpdateWeightDialog(
    currentWeight: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var newWeight by remember { mutableStateOf(currentWeight.toString()) }
    var weightError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Обновить вес") },
        text = {
            Column {
                OutlinedTextField(
                    value = newWeight,
                    onValueChange = {
                        newWeight = it
                        weightError = null
                    },
                    label = { Text("Вес (кг)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = weightError != null,
                    supportingText = weightError?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weightValue = newWeight.toDoubleOrNull()
                    if (weightValue == null) {
                        weightError = "Введите корректное число"
                    } else if (weightValue <= 0 || weightValue > 300) {
                        weightError = "Введите вес от 1 до 300 кг"
                    } else {
                        onSave(weightValue)
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun ChangeCredentialsDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (newUsername: String, newPassword: String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить данные") },
        text = {
            Column {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = {
                        newUsername = it
                        usernameError = null
                    },
                    label = { Text("Логин") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = usernameError != null,
                    supportingText = usernameError?.let { { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = null
                    },
                    label = { Text("Новый пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmError = null
                    },
                    label = { Text("Повторите пароль") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmError != null,
                    supportingText = confirmError?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var isValid = true
                    if (newUsername.isBlank()) {
                        usernameError = "Логин не может быть пустым"
                        isValid = false
                    }
                    if (newPassword.length < 6) {
                        passwordError = "Минимум 6 символов"
                        isValid = false
                    }
                    if (newPassword != confirmPassword) {
                        confirmError = "Пароли не совпадают"
                        isValid = false
                    }
                    if (isValid) {
                        onSave(newUsername, newPassword)
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun calculateBMI(weight: Double, height: Double): Float {
    val heightInMeters = height / 100.0
    return (weight / (heightInMeters * heightInMeters)).toFloat()
}

private fun getBMICategory(bmi: Float): String {
    return when {
        bmi < 18.5f -> "Недостаточный вес"
        bmi < 25f -> "Нормальный вес"
        bmi < 30f -> "Избыточный вес"
        else -> "Ожирение"
    }
}
