package com.example.hvkclientmitbenachrichtigungen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevealMarkScreen(
    modifier: Modifier = Modifier,
    grade: String
) {
    Column(
        //modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Deine Note",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Try to parse the grade as an integer
        val gradeAsInt = grade.toIntOrNull()

        if (gradeAsInt != null) {
            Spacer(modifier = Modifier.height(16.dp))

            var gradePoints = remember { grade.toIntOrNull()?.coerceIn(0, 15) ?: 0 }
            var randomIndices = remember { (0 until 15).shuffled().take(gradePoints) }

            Text(randomIndices.toString())

            Row(
                //modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 15) {
                    val isHighlighted = i in randomIndices

                    Text(
                        text = if (isHighlighted) "★" else "☆",
                        fontSize = 24.sp,
                        color = if (isHighlighted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            var clickCount by remember { mutableIntStateOf(0) }
            var currentGrade by remember { mutableIntStateOf(0) }


            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        if (randomIndices.contains(clickCount)) {
                            currentGrade += 1
                        }
                        clickCount += 1

                    },
                contentAlignment = Alignment.Center
            ) {

                Column {
                    Text(
                        text = "Deine Note: $currentGrade",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "$clickCount / 15 mal getippt",
                    )
                }


            }



        } else {
            // It's a text-based grade (like "mit gutem erfolg")
            Text(
                text = grade,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Herzlichen Glückwunsch!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}