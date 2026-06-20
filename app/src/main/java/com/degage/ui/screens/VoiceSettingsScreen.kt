package com.degage.ui.screens

import android.speech.tts.Voice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.degage.R
import com.degage.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun VoiceSettingsScreen(
    voices: List<Voice>,
    selectedVoiceName: String,
    previewingVoiceName: String? = null,
    speechRate: Float,
    pitch: Float,
    replyLanguageLabel: String,
    onBack: () -> Unit,
    onSelectVoice: (String) -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onPreview: (String, Float, Float) -> Unit,
) {
    var localRate by remember(speechRate) { mutableFloatStateOf(speechRate) }
    var localPitch by remember(pitch) { mutableFloatStateOf(pitch) }
    var localVoice by remember(selectedVoiceName) { mutableStateOf(selectedVoiceName) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    if (showVoiceDialog) {
        VoiceListDialog(
            voices = voices,
            selectedVoiceName = localVoice,
            previewingVoiceName = previewingVoiceName,
            replyLanguageLabel = replyLanguageLabel,
            onSelectVoice = {
                localVoice = it
                onSelectVoice(it)
            },
            onPreviewVoice = { onPreview(it, localRate, localPitch) },
            onDismiss = { showVoiceDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Text(
                stringResource(R.string.voice_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .background(AccentCyan, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                stringResource(R.string.voice_autosave_hint),
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bouton "Voix disponibles" -> ouvre le pop-up de choix
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AccentCyan, RoundedCornerShape(14.dp))
                        .clickable { showVoiceDialog = true }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.voice_available_title, replyLanguageLabel, voices.size),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
                }
            }

            // Bouton "Voix actuellement sélectionnée" -> écoute la voix active
            item {
                val selectedIndex = voices.indexOfFirst { it.name == localVoice }
                if (selectedIndex >= 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBg, RoundedCornerShape(14.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.voice_current_label, stringResource(R.string.voice_generic_label, selectedIndex + 1)),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { onPreview(localVoice, localRate, localPitch) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = stringResource(R.string.voice_test_button), tint = NeonGreen, modifier = Modifier.size(26.dp))
                        }
                    }
                }
            }

            // Sliders vitesse + pitch
            item {
                VoiceSliderCard(
                    label = stringResource(R.string.voice_rate_label),
                    value = localRate,
                    range = 0.5f..2.0f,
                    displayValue = "${(localRate * 100).roundToInt()}%",
                    onValueChange = {
                        localRate = it
                        onRateChange(it)
                    }
                )
            }
            item {
                VoiceSliderCard(
                    label = stringResource(R.string.voice_pitch_label),
                    value = localPitch,
                    range = 0.5f..2.0f,
                    displayValue = "${(localPitch * 100).roundToInt()}%",
                    onValueChange = {
                        localPitch = it
                        onPitchChange(it)
                    }
                )
            }

            // Bouton aperçu
            item {
                Button(
                    onClick = { onPreview(localVoice, localRate, localPitch) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.voice_test_button), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun VoiceSliderCard(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(displayValue, color = NeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = 29,
            colors = SliderDefaults.colors(
                thumbColor = NeonGreen,
                activeTrackColor = NeonGreen,
                inactiveTrackColor = CardBgAlt
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.voice_speed_slow), color = TextSecondary, fontSize = 11.sp)
            Text(stringResource(R.string.voice_speed_normal), color = TextSecondary, fontSize = 11.sp)
            Text(stringResource(R.string.voice_speed_fast), color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun VoiceRow(voice: Voice, label: String, isSelected: Boolean, isPreviewing: Boolean, onClick: () -> Unit, onPreviewClick: () -> Unit) {
    val highlighted = isSelected || isPreviewing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlighted) NeonGreenDim.copy(alpha = 0.2f) else CardBg, RoundedCornerShape(14.dp))
            .border(if (highlighted) 1.5.dp else 0.dp, if (highlighted) NeonGreen else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = if (highlighted) NeonGreen else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonGreen, RoundedCornerShape(4.dp))
                    )
                }
            }
            Text(
                text = voice.locale.displayLanguage,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.voice_preview_hint),
                color = TextSecondary,
                fontSize = 12.sp
            )
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(onClick = onPreviewClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.VolumeUp, contentDescription = stringResource(R.string.voice_test_button), tint = NeonGreen, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun VoiceListDialog(
    voices: List<Voice>,
    selectedVoiceName: String,
    previewingVoiceName: String?,
    replyLanguageLabel: String,
    onSelectVoice: (String) -> Unit,
    onPreviewVoice: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(CardBg, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.voice_available_title, replyLanguageLabel, voices.size),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close), tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (voices.isEmpty()) {
                Text(
                    stringResource(R.string.voice_none_found),
                    color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(voices, key = { _, voice -> voice.name }) { index, voice ->
                        VoiceRow(
                            voice = voice,
                            label = stringResource(R.string.voice_generic_label, index + 1),
                            isSelected = selectedVoiceName == voice.name,
                            isPreviewing = previewingVoiceName == voice.name,
                            onClick = {
                                onSelectVoice(voice.name)
                                onDismiss()
                            },
                            onPreviewClick = { onPreviewVoice(voice.name) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun VoiceSettingsPreview() {
    DegageTheme {
        VoiceSettingsScreen(
            voices = emptyList(),
            selectedVoiceName = "",
            speechRate = 1.0f,
            pitch = 1.0f,
            replyLanguageLabel = "français",
            onBack = {},
            onSelectVoice = {},
            onRateChange = {},
            onPitchChange = {},
            onPreview = { _, _, _ -> }
        )
    }
}
