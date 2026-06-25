package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.degage.ui.components.highlightBrand
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.theme.*

@Composable
fun UserGuideScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.guide_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .background(AccentOrange, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.guide_intro),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
            item { GuideSection(title = stringResource(R.string.guide_sec1_title), body = stringResource(R.string.guide_sec1_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec2_title), body = stringResource(R.string.guide_sec2_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec3_title), body = stringResource(R.string.guide_sec3_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec4_title), body = stringResource(R.string.guide_sec4_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec5_title), body = stringResource(R.string.guide_sec5_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec6_title), body = stringResource(R.string.guide_sec6_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec7_title), body = stringResource(R.string.guide_sec7_body)) }
            item { GuideSection(title = stringResource(R.string.guide_sec8_title), body = stringResource(R.string.guide_sec8_body)) }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun GuideSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(highlightBrand(title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        Text(highlightBrand(body), fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
    }
}
