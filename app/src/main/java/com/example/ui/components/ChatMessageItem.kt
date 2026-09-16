package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageEntity
import com.example.ui.theme.NovaAiBubble
import com.example.ui.theme.NovaAiBubbleBorder
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaUserBubbleEnd
import com.example.ui.theme.NovaUserBubbleStart
import com.example.ui.theme.NovaViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isSpeakingThisMessage: Boolean,
    onSpeak: (String, String) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    val timeFormatted = rememberTime(message.timestamp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Role header / indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            if (!isUser) {
                // NOVA-X Icon Badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NovaCyan, NovaViolet))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "X",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NOVA-X",
                    color = NovaCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Language pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NovaViolet.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = message.detectedLanguage,
                        color = NovaViolet,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                if (message.isVoiceInput) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Spoken by voice",
                        tint = NovaCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Voice",
                        color = NovaCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "You",
                    color = NovaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeFormatted,
                color = NovaTextMuted,
                fontSize = 11.sp
            )
        }

        // Message Bubble Surface
        if (isUser) {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(NovaUserBubbleStart, NovaUserBubbleEnd)
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = NovaCyan.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("user_message_bubble")
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
        } else {
            // Assistant Bubble with code snippet support
            Box(
                modifier = Modifier
                    .widthIn(max = 330.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(NovaAiBubble)
                    .border(
                        width = 1.dp,
                        color = NovaAiBubbleBorder,
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("assistant_message_bubble")
            ) {
                Column {
                    FormattedContent(
                        content = message.content,
                        onCopyCode = { code ->
                            copyToClipboard(context, "NOVA-X Code", code)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action bar under assistant reply: Speak/Replay, Stop, Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSpeakingThisMessage) {
                            // Stop Speaking Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NovaMagenta.copy(alpha = 0.2f))
                                    .clickable { onStop() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("stop_speaking_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = NovaMagenta,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Stop",
                                        color = NovaMagenta,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            // Replay / Speak Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NovaCyan.copy(alpha = 0.12f))
                                    .clickable { onSpeak(message.content, message.detectedLanguage) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("replay_voice_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Replay,
                                        contentDescription = "Replay Voice",
                                        tint = NovaCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Voice",
                                        color = NovaCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Copy Text Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NovaTextMuted.copy(alpha = 0.15f))
                                .clickable {
                                    copyToClipboard(context, "NOVA-X Response", message.content)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("copy_text_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = NovaTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy",
                                    color = NovaTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Splits text into standard text paragraphs and ```code``` blocks.
 */
@Composable
private fun FormattedContent(
    content: String,
    onCopyCode: (String) -> Unit
) {
    val parts = splitCodeBlocks(content)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (part in parts) {
            when (part) {
                is ContentChunk.Text -> {
                    Text(
                        text = part.text,
                        color = NovaTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
                is ContentChunk.Code -> {
                    CodeSnippetCard(
                        language = part.language,
                        code = part.code,
                        onCopy = { onCopyCode(part.code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeSnippetCard(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0F1D))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF12182B))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                color = NovaCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onCopy() }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = NovaTextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy",
                    color = NovaTextMuted,
                    fontSize = 11.sp
                )
            }
        }
        Text(
            text = code,
            color = Color(0xFFE2E8F0),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 18.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}

sealed class ContentChunk {
    data class Text(val text: String) : ContentChunk()
    data class Code(val language: String, val code: String) : ContentChunk()
}

private fun splitCodeBlocks(raw: String): List<ContentChunk> {
    val chunks = mutableListOf<ContentChunk>()
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\\n?([\\s\\S]*?)```")

    var lastIndex = 0
    codeBlockRegex.findAll(raw).forEach { matchResult ->
        val textBefore = raw.substring(lastIndex, matchResult.range.first)
        if (textBefore.isNotBlank()) {
            chunks.add(ContentChunk.Text(textBefore.trim()))
        }

        val lang = matchResult.groupValues[1].trim()
        val code = matchResult.groupValues[2].trim()
        chunks.add(ContentChunk.Code(language = lang, code = code))

        lastIndex = matchResult.range.last + 1
    }

    if (lastIndex < raw.length) {
        val remaining = raw.substring(lastIndex).trim()
        if (remaining.isNotBlank()) {
            chunks.add(ContentChunk.Text(remaining))
        }
    }

    if (chunks.isEmpty() && raw.isNotBlank()) {
        chunks.add(ContentChunk.Text(raw))
    }

    return chunks
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

@Composable
private fun rememberTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
