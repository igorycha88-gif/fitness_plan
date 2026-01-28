package com.example.fitness_plan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_plan.domain.model.InsightType
import com.example.fitness_plan.domain.model.MuscleGroupInsight
import java.util.regex.Pattern

@Composable
fun MuscleGroupInsightsCard(
    insights: List<MuscleGroupInsight>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Что это значит?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            insights.forEach { insight ->
                InsightItem(insight = insight)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InsightItem(insight: MuscleGroupInsight) {
    val iconColor = when (insight.type) {
        InsightType.MOST_TRAINED -> MaterialTheme.colorScheme.primary
        InsightType.LEAST_TRAINED -> MaterialTheme.colorScheme.secondary
        InsightType.NEEDS_ATTENTION -> MaterialTheme.colorScheme.error
        InsightType.PROGRESS_INCREASE -> MaterialTheme.colorScheme.primary
        InsightType.PROGRESS_DECREASE -> MaterialTheme.colorScheme.error
        InsightType.BALANCED -> MaterialTheme.colorScheme.tertiary
        InsightType.GENERAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = iconColor,
                    shape = RoundedCornerShape(50)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = formatInsightMessage(insight.message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatInsightMessage(message: String): String {
    val pattern = Pattern.compile("\\*\\*(.*?)\\*\\*")
    val matcher = pattern.matcher(message)
    val result = StringBuffer()

    while (matcher.find()) {
        matcher.appendReplacement(result, matcher.group(1))
    }
    matcher.appendTail(result)

    return result.toString()
}