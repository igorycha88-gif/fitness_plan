package com.example.fitness_plan.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Цветовая палитра (Dark Theme)
private val BackgroundDark = Color(0xFF0A0A0A)
private val CardDark = Color(0xFF1E1E1E)
private val AccentGreen = Color(0xFF4CAF50)
private val AccentYellow = Color(0xFFFFC107)
private val PurpleGradientStart = Color(0xFF667EEA)
private val PurpleGradientEnd = Color(0xFF8B5CF6)
private val WhiteText = Color(0xFFFFFFFF)
private val GrayText = Color(0xFFA0A0A0)

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit
) {
    // Анимация пульсации логотипа
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Анимация появления контента
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        showContent = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hero секция (верхняя часть экрана)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Анимированный логотип
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                ) {
                    // Градиентный круг
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PurpleGradientStart.copy(alpha = 0.3f),
                                        PurpleGradientEnd.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(70.dp)
                            )
                    )
                    // Иконка гантели
                    Text(
                        text = "🏋️",
                        fontSize = 64.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Заголовок
                Text(
                    text = "Начни свой путь",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = WhiteText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Подзаголовок
                Text(
                    text = "к лучшей версии себя",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light
                    ),
                    color = GrayText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Дескриптор
                Text(
                    text = "Персональный план тренировок\nСтатистика прогресса\nМотивация каждый день",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            // Кнопки (нижняя часть)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка "Войти"
                OutlinedButton(
                    onClick = { /* Navigate to login */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WhiteText
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GrayText.copy(alpha = 0.5f), GrayText.copy(alpha = 0.5f))
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Войти",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Кнопка "Зарегистрироваться" с градиентом
                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        PurpleGradientStart,
                                        PurpleGradientEnd
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Зарегистрироваться",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WhiteText
                        )
                    }
                }
            }

            // Футер
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fitness Plan",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText.copy(alpha = 0.6f)
            )
        }
    }
}
