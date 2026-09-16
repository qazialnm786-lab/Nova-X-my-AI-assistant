package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.GlowingOrbView
import com.example.ui.components.OrbState
import com.example.ui.components.QuickPromptsBar
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.NovaAiBubble
import com.example.ui.theme.NovaBackground
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaEmerald
import com.example.ui.theme.NovaMagenta
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.theme.NovaViolet

@Composable
fun NovaXScreen(
    viewModel: NovaXViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val messages by viewModel.allMessages.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isListening by viewModel.voiceManager.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsStateWithLifecycle()
    val audioRms by viewModel.voiceManager.audioRms.collectAsStateWithLifecycle()
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val currentSpeakingContent by viewModel.currentSpeakingContent.collectAsStateWithLifecycle()
    val orbState by viewModel.orbState.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Permission launcher for microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice chat", Toast.LENGTH_SHORT).show()
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBackground)
            .imePadding(),
        containerColor = NovaBackground,
        topBar = {
            // Futuristic Header Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NovaSurface)
                    .padding(top = statusBarPadding)
                    .border(width = 0.5.dp, color = NovaSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Glowing Mini Orb + Assistant Title & Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.toggleVoiceInput() }
                            .padding(4.dp)
                    ) {
                        GlowingOrbView(
                            state = orbState,
                            audioRms = audioRms,
                            size = 46.dp,
                            onClick = { viewModel.toggleVoiceInput() }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = settings.assistantName,
                                    color = NovaTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Online/Active Status Dot
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (orbState) {
                                                OrbState.LISTENING -> NovaEmerald
                                                OrbState.SPEAKING -> NovaMagenta
                                                OrbState.THINKING -> NovaViolet
                                                OrbState.IDLE -> NovaCyan
                                            }
                                        )
                                )
                            }
                            Text(
                                text = when (orbState) {
                                    OrbState.LISTENING -> stringResource(R.string.status_listening)
                                    OrbState.THINKING -> stringResource(R.string.status_thinking)
                                    OrbState.SPEAKING -> stringResource(R.string.status_speaking)
                                    OrbState.IDLE -> stringResource(R.string.status_online)
                                },
                                color = when (orbState) {
                                    OrbState.LISTENING -> NovaEmerald
                                    OrbState.SPEAKING -> NovaMagenta
                                    OrbState.THINKING -> NovaViolet
                                    OrbState.IDLE -> NovaTextMuted
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Right: Action buttons (New Chat, Voice Toggle, Settings)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Voice Mute / Unmute Toggle
                        IconButton(
                            onClick = {
                                viewModel.updateAutoSpeak(!settings.autoSpeakEnabled)
                                if (isSpeaking) viewModel.stopSpeaking()
                            },
                            modifier = Modifier.testTag("voice_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (settings.autoSpeakEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Toggle auto-voice",
                                tint = if (settings.autoSpeakEnabled) NovaCyan else NovaTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // New Chat Button
                        IconButton(
                            onClick = { viewModel.startNewChat() },
                            modifier = Modifier.testTag("new_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.action_new_chat),
                                tint = NovaTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.action_settings),
                                tint = NovaTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Audio visualizer banner shown when speaking or listening
                AnimatedVisibility(
                    visible = isListening || isSpeaking,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    AudioVisualizer(
                        isActive = isListening || isSpeaking,
                        isSpeaking = isSpeaking,
                        audioRms = audioRms,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        },
        bottomBar = {
            // Bottom Voice & Text Input Console
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NovaSurface)
                    .border(width = 0.5.dp, color = NovaSurfaceBorder)
                    .navigationBarsPadding()
            ) {
                // Quick suggestions chips bar
                QuickPromptsBar(
                    onPromptSelected = { prompt ->
                        viewModel.sendMessage(prompt)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Input Bar Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isListening) "Listening to you…" else stringResource(R.string.input_placeholder),
                                color = NovaTextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NovaSurfaceVariant,
                            unfocusedContainerColor = NovaSurfaceVariant,
                            focusedBorderColor = NovaCyan,
                            unfocusedBorderColor = NovaSurfaceBorder,
                            focusedTextColor = NovaTextPrimary,
                            unfocusedTextColor = NovaTextPrimary,
                            cursorColor = NovaCyan
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // If text entered -> show Send button; otherwise -> show Mic button
                    if (inputText.isNotBlank()) {
                        // Send Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(NovaCyan, NovaViolet))
                                )
                                .clickable {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                    keyboardController?.hide()
                                }
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.action_send),
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // Interactive Glowing Microphone Button
                        VoiceMicButton(
                            isListening = isListening,
                            isSpeaking = isSpeaking,
                            onClick = {
                                if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.toggleVoiceInput()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(
                        message = msg,
                        isSpeakingThisMessage = isSpeaking && currentSpeakingContent == msg.content,
                        onSpeak = { text, lang ->
                            viewModel.speakResponse(text, lang)
                        },
                        onStop = {
                            viewModel.stopSpeaking()
                        }
                    )
                }

                // AI Thinking Indicator bubble
                if (isThinking) {
                    item(key = "thinking_indicator") {
                        ThinkingBubble()
                    }
                }
            }

            // Error banner if any
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, NovaMagenta, RoundedCornerShape(12.dp)),
                    color = NovaAiBubble
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assistant Alert",
                                color = NovaMagenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = errorMessage ?: "",
                                color = NovaTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = NovaTextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onDismiss = { showSettingsDialog = false },
            onUpdateName = { viewModel.updateAssistantName(it) },
            onUpdateLanguage = { viewModel.updateLanguageMode(it) },
            onUpdateSpeed = { viewModel.updateVoiceSpeed(it) },
            onUpdatePitch = { viewModel.updateVoicePitch(it) },
            onUpdateAutoSpeak = { viewModel.updateAutoSpeak(it) },
            onUpdateMemory = { viewModel.updateMemory(it) },
            onUpdateApiKey = { viewModel.updateCustomApiKey(it) },
            onClearChat = { viewModel.clearChat() },
            onResetAll = { viewModel.resetAllData() },
            onTestVoice = { viewModel.testVoice() }
        )
    }
}

/**
 * Animated tactile Microphone / Stop button
 */
@Composable
private fun VoiceMicButton(
    isListening: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MicPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicScale"
    )

    val buttonColor = when {
        isSpeaking -> NovaMagenta
        isListening -> NovaEmerald
        else -> NovaCyan
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(if (isListening) pulseScale else 1.0f)
            .clip(CircleShape)
            .background(buttonColor)
            .clickable(onClick = onClick)
            .testTag("microphone_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                isSpeaking -> Icons.Default.Stop
                isListening -> Icons.Default.Mic
                else -> Icons.Default.Mic
            },
            contentDescription = if (isSpeaking) "Stop Speaking" else "Microphone",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Animated thinking bubble with glowing dots
 */
@Composable
private fun ThinkingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(NovaAiBubble)
                .border(1.dp, NovaSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("thinking_bubble")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = NovaCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NOVA-X is thinking…",
                    color = NovaCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
