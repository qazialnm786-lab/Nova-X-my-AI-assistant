package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextPrimary

data class QuickPromptItem(
    val title: String,
    val prompt: String,
    val icon: ImageVector
)

val defaultQuickPrompts = listOf(
    QuickPromptItem(
        title = "Explain simply",
        prompt = "Explain quantum computing in simple language with an analogy.",
        icon = Icons.Default.Lightbulb
    ),
    QuickPromptItem(
        title = "Coding help",
        prompt = "Write a Python script to fetch the current weather using an API.",
        icon = Icons.Default.Code
    ),
    QuickPromptItem(
        title = "Hinglish chat",
        prompt = "Mujhe machine learning step by step aasan bhasha mein samjhao.",
        icon = Icons.Default.Translate
    ),
    QuickPromptItem(
        title = "Daily plan",
        prompt = "Help me organize a productive daily schedule for study and work.",
        icon = Icons.Default.Schedule
    ),
    QuickPromptItem(
        title = "Math solver",
        prompt = "Solve step-by-step: Find the derivative of f(x) = x^3 * sin(x).",
        icon = Icons.Default.Calculate
    ),
    QuickPromptItem(
        title = "Study guide",
        prompt = "Summarize the key differences between SQL and NoSQL databases.",
        icon = Icons.Default.MenuBook
    )
)

@Composable
fun QuickPromptsBar(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (item in defaultQuickPrompts) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NovaSurfaceVariant)
                    .border(1.dp, NovaSurfaceBorder, RoundedCornerShape(20.dp))
                    .clickable { onPromptSelected(item.prompt) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("quick_prompt_${item.title.lowercase().replace(" ", "_")}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = NovaCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.title,
                        color = NovaTextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
