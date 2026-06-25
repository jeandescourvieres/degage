package com.degage.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.components.LinkRow
import com.degage.ui.components.highlightBrand
import com.degage.ui.theme.*
import kotlinx.coroutines.delay

private val NavBlue = Color(0xFF3B9DFF)
private val NavYellow = Color(0xFFFFD60A)
private val NavCyan = Color(0xFF00E5FF)
private val NavPink = Color(0xFFFF6FB0)

@Composable
fun HomeScreen(
    refreshKey: Int = 0,
    isEnabled: Boolean,
    activeMode: String,
    onToggle: () -> Unit,
    onNavigateDetails: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateFaq: () -> Unit,
    onNavigateDashboard: () -> Unit,
    onNavigateModes: () -> Unit = {},
    onNavigateVoiceSettings: () -> Unit = {},
    appLanguage: String = "",
    onSetAppLanguage: (String) -> Unit = {},
    welcomeMusicEnabled: Boolean = true,
    shouldPlayWelcomeChime: Boolean = false,
    onWelcomeChimePlayed: () -> Unit = {},
    showBackgroundColorTip: Boolean = false,
    onBackgroundColorTipDismissed: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            LanguageFlagHeader(appLanguage = appLanguage, onSetAppLanguage = onSetAppLanguage)
        }
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                key(refreshKey) {
                var logoVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    logoVisible = true
                    if (welcomeMusicEnabled && shouldPlayWelcomeChime) {
                        com.degage.tts.WelcomeChime.play()
                        onWelcomeChimePlayed()
                    }
                }
                AnimatedVisibility(
                    visible = logoVisible,
                    enter = slideInHorizontally(
                        animationSpec = tween(1800),
                        initialOffsetX = { -it }
                    ) + fadeIn(animationSpec = tween(1800))
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(
                            4.dp,
                            NeonGreen.copy(alpha = 0.5f + 0.5f * heroPulse),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TU DÉGAGES !",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    val titleTranslation = stringResource(R.string.home_title_translation)
                    if (titleTranslation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$titleTranslation !",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .background(NeonGreen, RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_hero_welcome),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                }
                }
                Spacer(modifier = Modifier.height(22.dp))
                key(refreshKey) {
                var badgeVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { badgeVisible = true }
                AnimatedVisibility(
                    visible = badgeVisible,
                    enter = slideInHorizontally(
                        animationSpec = tween(1800),
                        initialOffsetX = { it }
                    ) + fadeIn(animationSpec = tween(1800))
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.home_hero_badge),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonGreen,
                        textAlign = TextAlign.Center
                    )
                }
                }
                }
                Spacer(modifier = Modifier.height(22.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                }
                Spacer(modifier = Modifier.height(22.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.welcome_hero_headline),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentOrange,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val heroDesc = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.welcome_hero_desc_q))
                        }
                        append("\n\n")
                        append(highlightBrand(stringResource(R.string.welcome_hero_desc_body)))
                    }
                    Text(
                        text = heroDesc,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val messageDesc = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.welcome_hero_msg_bold1))
                        }
                        append(" ")
                        append(stringResource(R.string.welcome_hero_msg_rest))
                        append("\n")
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.welcome_hero_msg_bold2))
                        }
                    }
                    LinkRow(
                        text = messageDesc,
                        onClick = onNavigateModes
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val voiceDesc = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append(stringResource(R.string.welcome_hero_voice_bold))
                        }
                        append(" ")
                        append(stringResource(R.string.welcome_hero_voice_rest))
                    }
                    LinkRow(
                        text = voiceDesc,
                        onClick = onNavigateVoiceSettings
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IntroBadge(stringResource(R.string.home_badge_others_block))
                    Spacer(modifier = Modifier.height(6.dp))
                    IntroBadge(highlightBrand(stringResource(R.string.home_badge_we_reply)))
                    Spacer(modifier = Modifier.height(6.dp))
                    IntroBadge(stringResource(R.string.home_badge_offline))
                    Spacer(modifier = Modifier.height(6.dp))
                    IntroBadge(stringResource(R.string.home_badge_languages))
                    Spacer(modifier = Modifier.height(6.dp))
                    IntroBadge(stringResource(R.string.home_badge_countries))
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateDetails() }
                        .background(NavBlue.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, NavBlue, RoundedCornerShape(14.dp))
                        .padding(vertical = 7.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = highlightBrand(stringResource(R.string.welcome_sections_intro)),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateSettings() }
                        .background(NavYellow.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, NavYellow, RoundedCornerShape(14.dp))
                        .padding(vertical = 7.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.welcome_settings_link),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateDashboard() }
                        .background(NavCyan.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, NavCyan, RoundedCornerShape(14.dp))
                        .padding(vertical = 7.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.welcome_dashboard_link),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateFaq() }
                        .background(NavPink.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, NavPink, RoundedCornerShape(14.dp))
                        .padding(vertical = 7.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.welcome_faq_link),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_protection_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = highlightBrand(stringResource(R.string.home_protection_intro)),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProtectionStatusCard(
                    isEnabled = isEnabled,
                    activeMode = activeMode,
                    onToggle = onToggle
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showBackgroundColorTip) {
        BackgroundColorTipPopup(
            onOpenSettings = {
                onBackgroundColorTipDismissed()
                onNavigateSettings()
            },
            onDismiss = onBackgroundColorTipDismissed
        )
    }
    }
}

@Composable
private fun BoxScope.BackgroundColorTipPopup(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2200)
        visible = true
        delay(7000)
        if (visible) {
            visible = false
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    visible = false
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable {
                        visible = false
                        onOpenSettings()
                    }
                    .background(CardBgAlt, RoundedCornerShape(24.dp))
                    .border(2.dp, NeonGreen, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        visible = false
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.home_bg_color_tip),
                    color = Color.White,
                    fontSize = 19.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun IntroBadge(label: String) {
    IntroBadge(AnnotatedString(label))
}

@Composable
private fun IntroBadge(label: AnnotatedString) {
    Text(
        label,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
