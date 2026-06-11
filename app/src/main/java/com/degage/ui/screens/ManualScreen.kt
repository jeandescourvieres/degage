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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.theme.*

@Composable
fun ManualScreen(onBack: () -> Unit) {
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
            Text(stringResource(R.string.manual_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q1_title),
                    body = stringResource(R.string.manual_q1_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q2_title),
                    body = stringResource(R.string.manual_q2_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q3_title),
                    body = stringResource(R.string.manual_q3_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q4_title),
                    body = stringResource(R.string.manual_q4_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q5_title),
                    body = stringResource(R.string.manual_q5_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q6_title),
                    body = stringResource(R.string.manual_q6_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q7_title),
                    body = stringResource(R.string.manual_q7_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q8_title),
                    body = stringResource(R.string.manual_q8_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q9_title),
                    body = stringResource(R.string.manual_q9_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q10_title),
                    body = stringResource(R.string.manual_q10_body)
                )
            }

            item { CategoryHeader(stringResource(R.string.manual_cat_db)) }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q11_title),
                    body = stringResource(R.string.manual_q11_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q12_title),
                    body = stringResource(R.string.manual_q12_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q13_title),
                    body = stringResource(R.string.manual_q13_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q14_title),
                    body = stringResource(R.string.manual_q14_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q15_title),
                    body = stringResource(R.string.manual_q15_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q16_title),
                    body = stringResource(R.string.manual_q16_body)
                )
            }

            item { CategoryHeader(stringResource(R.string.manual_cat_usage)) }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q17_title),
                    body = stringResource(R.string.manual_q17_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q18_title),
                    body = stringResource(R.string.manual_q18_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q19_title),
                    body = stringResource(R.string.manual_q19_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q20_title),
                    body = stringResource(R.string.manual_q20_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q21_title),
                    body = stringResource(R.string.manual_q21_body)
                )
            }

            item { CategoryHeader(stringResource(R.string.manual_cat_issues)) }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q22_title),
                    body = stringResource(R.string.manual_q22_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q23_title),
                    body = stringResource(R.string.manual_q23_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q24_title),
                    body = stringResource(R.string.manual_q24_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q25_title),
                    body = stringResource(R.string.manual_q25_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q26_title),
                    body = stringResource(R.string.manual_q26_body)
                )
            }

            item { CategoryHeader(stringResource(R.string.manual_cat_tech)) }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q27_title),
                    body = stringResource(R.string.manual_q27_body)
                )
            }

            item { CategoryHeader(stringResource(R.string.manual_cat_other)) }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q28_title),
                    body = stringResource(R.string.manual_q28_body)
                )
            }
            item {
                ManualSection(
                    title = stringResource(R.string.manual_q29_title),
                    body = stringResource(R.string.manual_q29_body)
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = NeonGreen,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun ManualSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        Text(body, fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
    }
}
