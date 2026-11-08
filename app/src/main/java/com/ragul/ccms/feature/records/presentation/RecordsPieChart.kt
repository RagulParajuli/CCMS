package com.ragul.ccms.feature.records.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.feature.records.data.RecordCounts
import com.ragul.ccms.ui.theme.AuthTextPrimary
import com.ragul.ccms.ui.theme.AuthTextSecondary
import com.ragul.ccms.ui.theme.RecordLoanDeposit
import com.ragul.ccms.ui.theme.RecordMembership
import com.ragul.ccms.ui.theme.RecordMonthlyDue

private data class PieSlice(val label: String, val count: Int, val color: Color)

@Composable
fun RecordsPieChartCard(
    counts: RecordCounts,
    modifier: Modifier = Modifier,
) {
    val slices = listOf(
        PieSlice("Membership", counts.memberships, RecordMembership),
        PieSlice("Loan or Deposit", counts.loanDeposits, RecordLoanDeposit),
        PieSlice("Monthly Due", counts.monthlyDues, RecordMonthlyDue),
    )
    val total = counts.total

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Records overview",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AuthTextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (total == 0) {
                    "Your chart will appear when you add the first record."
                } else {
                    "Distribution of the $total records saved in this account."
                },
                fontSize = 13.sp,
                color = AuthTextSecondary,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(132.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 22.dp.toPx()
                        if (total == 0) {
                            drawArc(
                                color = Color(0xFFE3EEE4),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                        } else {
                            var startAngle = -90f
                            slices.filter { it.count > 0 }.forEach { slice ->
                                val sweep = slice.count.toFloat() / total * 360f
                                val gap = if (sweep > 4f) 2f else 0f
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = (sweep - gap).coerceAtLeast(1f),
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                )
                                startAngle += sweep
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = total.toString(),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuthTextPrimary,
                        )
                        Text(
                            text = if (total == 1) "record" else "records",
                            fontSize = 12.sp,
                            color = AuthTextSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    slices.forEach { slice ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(slice.color, CircleShape),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = slice.label,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                color = AuthTextSecondary,
                            )
                            Text(
                                text = slice.count.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuthTextPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}
