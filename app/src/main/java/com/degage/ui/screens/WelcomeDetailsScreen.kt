package com.degage.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.components.highlightBrand
import com.degage.ui.theme.*

@Composable
fun WelcomeDetailsScreen(
    onBack: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateFaq: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Text(
                text = highlightBrand(stringResource(R.string.welcome_sections_intro)),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(AccentBlue, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.welcome_details_intro),
                    fontSize = 15.sp,
                    color = TextSecondary,
                    lineHeight = 21.sp
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.welcome_details_outro),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateSettings() }
                        .background(AccentYellow.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, AccentYellow, RoundedCornerShape(14.dp))
                        .padding(vertical = 12.dp, horizontal = 12.dp),
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
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateFaq() }
                        .background(AccentPink.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .border(1.dp, AccentPink, RoundedCornerShape(14.dp))
                        .padding(vertical = 12.dp, horizontal = 12.dp),
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
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun WelcomeSection(title: String, body: String, accentColor: Color) {
    val parts = title.split(" ", limit = 2)
    val icon = parts.getOrNull(0) ?: ""
    val titleText = parts.getOrNull(1) ?: title
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 44.dp)
                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 4.dp),
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
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = accentColor
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(highlightBrand(body), fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
        }
    }
}
