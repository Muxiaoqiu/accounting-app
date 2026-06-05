package com.muxiaoqiu.accounting.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muxiaoqiu.accounting.data.entity.Budget

private val AlertPink = Color(0xFFFDE8ED)

@Composable
fun BudgetAlertCard(
    alerts: List<BudgetAlert>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = AlertPink)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚠️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "预算提醒",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            alerts.take(2).forEachIndexed { index, alert ->
                if (index > 0) {
                    HorizontalDivider(
                        color = Color(0x33000000),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                val statusColor = if (alert.isOverBudget) MaterialTheme.colorScheme.error
                else Color(0xFFF08C99)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = buildAlertLabel(alert.budget),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (alert.isOverBudget) {
                            Text(
                                text = "已超支 ¥${"%.2f".format(alert.spent - alert.budget.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "剩余 ¥${"%.2f".format(alert.budget.amount - alert.spent)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${(alert.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

private fun buildAlertLabel(budget: Budget): String {
    return if (budget.categoryName != null) {
        "${budget.categoryName}分类预算"
    } else if (budget.month != null) {
        "${budget.month + 1}月总预算"
    } else {
        "${budget.year}年总预算"
    }
}
