package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.theme.DegageTheme
import com.degage.ui.theme.NeonGreen

@Composable
fun OnboardingScreen(onStart: () -> Unit) {
    var step by remember { mutableStateOf(0) }

    when (step) {
        0 -> OnboardingWelcomeScreen(onNext = { step = 1 })
        1 -> OnboardingFeaturesScreen(onNext = { step = 2 })
        else -> PermissionExplanationScreen(onAuthorize = onStart)
    }
}

@Composable
private fun OnboardingFeaturesScreen(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.onboarding_how_it_works),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                FeatureRow(emoji = "📞", title = stringResource(R.string.onboarding_feature_detection_title), desc = stringResource(R.string.onboarding_feature_detection_desc))
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow(emoji = "🎙️", title = stringResource(R.string.onboarding_feature_voice_title), desc = stringResource(R.string.onboarding_feature_voice_desc))
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow(emoji = "📊", title = stringResource(R.string.onboarding_feature_stats_title), desc = stringResource(R.string.onboarding_feature_stats_desc))
                Spacer(modifier = Modifier.height(16.dp))
                FeatureRow(emoji = "✏️", title = stringResource(R.string.onboarding_feature_custom_title), desc = stringResource(R.string.onboarding_feature_custom_desc))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(stringResource(R.string.onboarding_next_button), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, fontSize = 13.sp, color = Color(0xFF8A8A8A), lineHeight = 19.sp)
        }
    }
}

@Composable
private fun PermissionExplanationScreen(onAuthorize: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.onboarding_permission_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.onboarding_permission_intro),
                    fontSize = 15.sp,
                    color = Color(0xFF8A8A8A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_permission_popup_text),
                        fontSize = 14.sp,
                        color = NeonGreen,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.onboarding_permission_steps),
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.onboarding_permission_warning),
                    fontSize = 13.sp,
                    color = Color(0xFF8A8A8A),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onAuthorize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_permission_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun OnboardingWelcomeScreen(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo mascot placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(60.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Tu dégages",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.onboarding_welcome_tagline),
                    fontSize = 18.sp,
                    color = NeonGreen,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.onboarding_your_time_is),
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.onboarding_precious),
                    fontSize = 22.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_start_button),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.onboarding_essential_features),
                    color = Color(0xFF8A8A8A),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    DegageTheme { OnboardingScreen(onStart = {}) }
}
