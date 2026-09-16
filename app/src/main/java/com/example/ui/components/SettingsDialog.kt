package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserSettings
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaViolet
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(
    settings: UserSettings,
    onDismiss: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onUpdatePitch: (Float) -> Unit,
    onUpdateAutoSpeak: (Boolean) -> Unit,
    onUpdateMemory: (Boolean) -> Unit,
    onUpdateApiKey: (String) -> Unit,
    onClearChat: () -> Unit,
    onResetAll: () -> Unit,
    onTestVoice: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var tempName by remember(settings.assistantName) { mutableStateOf(settings.assistantName) }
    var tempApiKey by remember(settings.customApiKey) { mutableStateOf(settings.customApiKey) }

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, NovaSurfaceBorder, RoundedCornerShape(24.dp))
                .testTag("settings_dialog"),
            color = NovaSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NOVA-X Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "Voice AI & Persona Customization",
                            fontSize = 12.sp,
                            color = NovaTextMuted
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NovaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Assistant Name
                Text(
                    text = "Assistant Identity",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = tempName,
                    onValueChange = {
                        tempName = it
                        onUpdateName(it)
                    },
                    label = { Text("Assistant Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assistant_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = NovaSurfaceBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Language Support (Hindi, English, Hinglish)
                Text(
                    text = "Language Preference",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                val languages = listOf("Auto-Detect", "English", "Hindi", "Hinglish")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (lang in languages) {
                        val isSelected = settings.languageMode == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NovaCyan else NovaSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) NovaCyan else NovaSurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onUpdateLanguage(lang) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang,
                                color = if (isSelected) Color.Black else NovaTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Female AI Voice Settings
                Text(
                    text = "Voice Synthesis (Female AI)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaCyan
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Voice Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Speech Rate", color = NovaTextSecondary, fontSize = 13.sp)
                    Text(
                        "${(settings.voiceSpeed * 100).roundToInt() / 100f}x",
                        color = NovaCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = settings.voiceSpeed,
                    onValueChange = onUpdateSpeed,
                    valueRange = 0.6f..1.8f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaCyan,
                        activeTrackColor = NovaCyan,
                        inactiveTrackColor = NovaSurfaceVariant
                    ),
                    modifier = Modifier.testTag("voice_speed_slider")
                )

                // Voice Pitch Slider (Natural young female tone)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Voice Pitch (Female Tone)", color = NovaTextSecondary, fontSize = 13.sp)
                    Text(
                        "${(settings.voicePitch * 100).roundToInt() / 100f}x",
                        color = NovaViolet,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = settings.voicePitch,
                    onValueChange = onUpdatePitch,
                    valueRange = 0.8f..1.4f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaViolet,
                        activeTrackColor = NovaViolet,
                        inactiveTrackColor = NovaSurfaceVariant
                    ),
                    modifier = Modifier.testTag("voice_pitch_slider")
                )

                // Test Voice Button
                Button(
                    onClick = onTestVoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_voice_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Test Voice",
                        tint = NovaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Female Voice Output", color = NovaTextPrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-speak switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NovaSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Speak Responses", color = NovaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Automatically speaks AI replies aloud", color = NovaTextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.autoSpeakEnabled,
                        onCheckedChange = onUpdateAutoSpeak,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaCyan,
                            checkedTrackColor = NovaCyan.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("auto_speak_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Conversation Memory switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NovaSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Conversation Memory", color = NovaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Retains context of past turns in this chat", color = NovaTextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.memoryEnabled,
                        onCheckedChange = onUpdateMemory,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaViolet,
                            checkedTrackColor = NovaViolet.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("memory_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom API Key Override (Optional)
                Text(
                    text = "Gemini API Key (Optional Override)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If not configured via AI Studio Secrets panel, you can enter it here.",
                    fontSize = 11.sp,
                    color = NovaTextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = tempApiKey,
                    onValueChange = {
                        tempApiKey = it
                        onUpdateApiKey(it)
                    },
                    placeholder = { Text("AIzaSy…") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_api_key_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Key",
                            tint = NovaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = NovaSurfaceBorder,
                        focusedTextColor = NovaTextPrimary,
                        unfocusedTextColor = NovaTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action: Clear Conversation & Stored Data
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showClearConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_chat_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear Chat", color = NovaMagenta, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_all_data_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset All", color = NovaTextMuted, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Clear Chat Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Chat History?", color = NovaTextPrimary) },
            text = { Text("All messages in the current conversation will be permanently deleted.", color = NovaTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearChat()
                        showClearConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Clear", color = NovaMagenta)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = NovaTextMuted)
                }
            },
            containerColor = NovaSurface,
            modifier = Modifier.testTag("clear_confirm_dialog")
        )
    }

    // Reset All Confirmation Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Data?", color = NovaTextPrimary) },
            text = { Text("This will erase all chat logs, memory, and restore default assistant settings.", color = NovaTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAll()
                        showResetConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Reset Everything", color = NovaMagenta)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = NovaTextMuted)
                }
            },
            containerColor = NovaSurface,
            modifier = Modifier.testTag("reset_confirm_dialog")
        )
    }
}
