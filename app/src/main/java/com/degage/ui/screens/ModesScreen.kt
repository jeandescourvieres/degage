package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.degage.ui.components.InfoDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.modes.AppMode
import com.degage.modes.localizedLabel
import com.degage.ui.components.PremiumBadge
import com.degage.ui.theme.*

val modeInfoList = listOf(
    AppMode.POLI,
    AppMode.PRO,
    AppMode.AMICAL,
    AppMode.DIRECT,
    AppMode.HUMOUR,
    AppMode.SARCASTIQUE,
    AppMode.TROLL,
    AppMode.ROBOT,
    AppMode.FROID,
    AppMode.CINGLANT,
)

@Composable
fun AppMode.localizedDescription(): String = when (this) {
    AppMode.POLI -> stringResource(R.string.mode_poli_desc)
    AppMode.PRO -> stringResource(R.string.mode_pro_desc)
    AppMode.AMICAL -> stringResource(R.string.mode_amical_desc)
    AppMode.DIRECT -> stringResource(R.string.mode_direct_desc)
    AppMode.HUMOUR -> stringResource(R.string.mode_humour_desc)
    AppMode.SARCASTIQUE -> stringResource(R.string.mode_sarcastique_desc)
    AppMode.TROLL -> stringResource(R.string.mode_troll_desc)
    AppMode.ROBOT -> stringResource(R.string.mode_robot_desc)
    AppMode.FROID -> stringResource(R.string.mode_froid_desc)
    AppMode.CINGLANT -> stringResource(R.string.mode_cinglant_desc)
}

@Composable
fun AppMode.localizedExample(): String = when (this) {
    AppMode.POLI -> stringResource(R.string.mode_poli_example)
    AppMode.PRO -> stringResource(R.string.mode_pro_example)
    AppMode.AMICAL -> stringResource(R.string.mode_amical_example)
    AppMode.DIRECT -> stringResource(R.string.mode_direct_example)
    AppMode.HUMOUR -> stringResource(R.string.mode_humour_example)
    AppMode.SARCASTIQUE -> stringResource(R.string.mode_sarcastique_example)
    AppMode.TROLL -> stringResource(R.string.mode_troll_example)
    AppMode.ROBOT -> stringResource(R.string.mode_robot_example)
    AppMode.FROID -> stringResource(R.string.mode_froid_example)
    AppMode.CINGLANT -> stringResource(R.string.mode_cinglant_example)
}

@Composable
fun ModesScreen(
    activeMode: AppMode,
    onSelectMode: (AppMode) -> Unit,
    onPreviewMode: (AppMode) -> Unit,
    isPremium: Boolean = true,
    onUpgrade: () -> Unit = {},
    onBack: () -> Unit = {},
    appLanguage: String = "",
    onSetAppLanguage: (String) -> Unit = {},
    onNavigateMessageBuilder: () -> Unit = {},
    modeFullTexts: Map<String, String> = emptyMap(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
    ) {
        var showInfo by remember { mutableStateOf(false) }
        if (showInfo) InfoDialog(
            title = stringResource(R.string.modes_info_title),
            content = stringResource(R.string.modes_info_content),
            onDismiss = { showInfo = false }
        )
        var showFullMessageFor by remember { mutableStateOf<AppMode?>(null) }
        showFullMessageFor?.let { mode ->
            InfoDialog(
                title = "${mode.emoji} ${mode.localizedLabel()}",
                content = modeFullTexts[mode.name]?.takeIf { it.isNotBlank() } ?: mode.localizedExample(),
                onDismiss = { showFullMessageFor = null }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.modes_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(stringResource(R.string.modes_subtitle), color = TextSecondary, fontSize = 13.sp)
            }
            IconButton(onClick = { showInfo = true }) {
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_help), tint = NeonGreen, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                LanguageFlagHeader(appLanguage = appLanguage, onSetAppLanguage = onSetAppLanguage)
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.modes_intro_text),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable { onNavigateMessageBuilder() }
                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.mb_header_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            items(modeInfoList) { mode ->
                val locked = !isPremium && mode != AppMode.POLI
                ModeCard(
                    mode = mode,
                    isSelected = activeMode == mode,
                    locked = locked,
                    onClick = {
                        if (locked) onUpgrade() else {
                            onSelectMode(mode)
                            showFullMessageFor = mode
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onPreviewMode(activeMode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.modes_preview_button), color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun LanguageFlagHeader(appLanguage: String, onSetAppLanguage: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        LanguageFlagChip("🇩🇪", "D", selected = appLanguage == "DE", onClick = { onSetAppLanguage("DE") })
        Spacer(modifier = Modifier.width(4.dp))
        LanguageFlagChip("🇪🇸", "ES", selected = appLanguage == "ES", onClick = { onSetAppLanguage("ES") })
        Spacer(modifier = Modifier.width(4.dp))
        LanguageFlagChip("🇫🇷", "FR", selected = appLanguage == "FR", onClick = { onSetAppLanguage("FR") })
        Spacer(modifier = Modifier.width(4.dp))
        LanguageFlagChip("🇬🇧", "GB", selected = appLanguage == "EN", onClick = { onSetAppLanguage("EN") })
        Spacer(modifier = Modifier.width(4.dp))
        LanguageFlagChip("🇮🇹", "IT", selected = appLanguage == "IT", onClick = { onSetAppLanguage("IT") })
    }
}

@Composable
private fun LanguageFlagChip(flag: String, code: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(if (selected) NeonGreen else CardBgAlt, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Text(flag, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            code,
            color = if (selected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModeCard(mode: AppMode, isSelected: Boolean, locked: Boolean = false, onClick: () -> Unit) {
    val borderColor = if (isSelected) NeonGreen else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 0.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = mode.emoji, fontSize = 32.sp, modifier = Modifier.alpha(if (locked) 0.4f else 1f))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).alpha(if (locked) 0.4f else 1f)) {
            Text(mode.localizedLabel(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text(mode.localizedDescription(), color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(mode.localizedExample(), color = Color(0xFF6A6A6A), fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
        if (locked) {
            PremiumBadge()
        } else {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = NeonGreen, unselectedColor = TextSecondary)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun ModesScreenPreview() {
    DegageTheme {
        ModesScreen(
            activeMode = AppMode.SARCASTIQUE,
            onSelectMode = {},
            onPreviewMode = {}
        )
    }
}
