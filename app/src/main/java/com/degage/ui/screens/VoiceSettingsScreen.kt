package com.degage.ui.screens

import android.speech.tts.Voice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.degage.R
import com.degage.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun VoiceSettingsScreen(
    voices: List<Voice>,
    selectedVoiceName: String,
    speechRate: Float,
    pitch: Float,
    onBack: () -> Unit,
    onSelectVoice: (String) -> Unit,
    onRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onPreview: (String, Float, Float) -> Unit,
) {
    var localRate by remember(speechRate) { mutableFloatStateOf(speechRate) }
    var localPitch by remember(pitch) { mutableFloatStateOf(pitch) }
    var localVoice by remember(selectedVoiceName) { mutableStateOf(selectedVoiceName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Text(stringResource(R.string.voice_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            // Liste des voix disponibles
            item {
                Text(stringResource(R.string.voice_available_title), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (voices.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.voice_none_found),
                        color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp
                    )
                }
            }

            items(voices, key = { it.name }) { voice ->
                VoiceRow(
                    voice = voice,
                    isSelected = localVoice == voice.name,
                    onClick = {
                        localVoice = voice.name
                        onSelectVoice(voice.name)
                    }
                )
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
private fun VoiceRow(voice: Voice, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) NeonGreenDim.copy(alpha = 0.2f) else CardBg, RoundedCornerShape(14.dp))
            .border(if (isSelected) 1.5.dp else 0.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = voice.name.replace("fr-fr-", "").replace("-", " ").replaceFirstChar { it.uppercase() },
                color = if (isSelected) NeonGreen else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = voice.locale.displayLanguage,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(NeonGreen, RoundedCornerShape(5.dp))
            )
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
            onBack = {},
            onSelectVoice = {},
            onRateChange = {},
            onPitchChange = {},
            onPreview = { _, _, _ -> }
        )
    }
}
