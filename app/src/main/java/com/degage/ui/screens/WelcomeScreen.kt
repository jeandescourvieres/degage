package com.degage.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.components.highlightBrand
import com.degage.ui.theme.*

@Composable
fun WelcomeScreen(
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            Text(
                text = highlightBrand(stringResource(R.string.welcome_title)),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Hero
                val heroFlash = rememberInfiniteTransition(label = "heroFlash")
                val heroPulse by heroFlash.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heroPulse"
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                RedAlert.copy(alpha = 0.2f + 0.8f * heroPulse),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.robot),
                            contentDescription = null,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        buildAnnotatedString {
                            append("TU ")
                            withStyle(SpanStyle(color = NeonGreen)) {
                                append("DÉGAGES")
                            }
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.welcome_hero_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Text(
                        text = stringResource(R.string.welcome_hero_subtitle),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.welcome_hero_desc),
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )
                }
            }

            item {
                Text(
                    text = highlightBrand(stringResource(R.string.welcome_sections_intro)),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
            item {
                WelcomeSection(
                    stringResource(R.string.welcome_section1_title),
                    stringResource(R.string.welcome_section1_body),
                    accentColor = NeonGreen
                )
            }
            item {
                WelcomeSection(
                    stringResource(R.string.welcome_section2_title),
                    stringResource(R.string.welcome_section2_body),
                    accentColor = AccentBlue
                )
            }
            item {
                WelcomeSection(
                    stringResource(R.string.welcome_section3_title),
                    stringResource(R.string.welcome_section3_body),
                    accentColor = AccentPurple
                )
            }
            item {
                WelcomeSection(
                    stringResource(R.string.welcome_section4_title),
                    stringResource(R.string.welcome_section4_body),
                    accentColor = AccentOrange
                )
            }
            item {
                WelcomeSection(
                    stringResource(R.string.welcome_section5_title),
                    stringResource(R.string.welcome_section5_body),
                    accentColor = AccentPink
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_tip_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.welcome_tip_body),
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 21.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Bouton dismiss
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text(
                    text = stringResource(R.string.welcome_dismiss_button),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun WelcomeSection(title: String, body: String, accentColor: Color) {
    val parts = title.split(" ", limit = 2)
    val icon = parts.getOrNull(0) ?: ""
    val titleText = parts.getOrNull(1) ?: title

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                highlightBrand(titleText),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(highlightBrand(body), fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
    }
}
