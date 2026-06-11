package com.degage.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.R
import com.degage.ui.theme.*

@Composable
fun PremiumScreen(
    isPremium: Boolean,
    onBack: () -> Unit = {},
    onToggleDevPremium: (Boolean) -> Unit = {},
) {
    val premiumFeatures = stringArrayResource(R.array.premium_features)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back), tint = Color.White)
                }
                Text(stringResource(R.string.premium_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonGreenDim.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text("⭐", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.premium_hero_title), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.premium_hero_subtitle),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        items(premiumFeatures) { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(feature, color = Color.White, fontSize = 14.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceCard(modifier = Modifier.weight(1f), price = stringResource(R.string.premium_price_monthly), period = stringResource(R.string.premium_price_monthly_period))
                PriceCard(modifier = Modifier.weight(1f), price = stringResource(R.string.premium_price_yearly), period = stringResource(R.string.premium_price_yearly_period), highlight = true)
            }
        }

        item {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, disabledContainerColor = CardBgAlt)
            ) {
                Text(stringResource(R.string.premium_coming_soon), color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.premium_coming_soon_detail),
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = CardBgAlt)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.premium_dev_mode_title), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.premium_dev_mode_desc),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isPremium,
                    onCheckedChange = onToggleDevPremium,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = CardBgAlt
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun PriceCard(modifier: Modifier = Modifier, price: String, period: String, highlight: Boolean = false) {
    Column(
        modifier = modifier
            .background(if (highlight) NeonGreenDim.copy(alpha = 0.18f) else CardBg, RoundedCornerShape(16.dp))
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (highlight) {
            Text(stringResource(R.string.premium_best_price), color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(price, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(period, color = TextSecondary, fontSize = 12.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun PremiumScreenPreview() {
    DegageTheme { PremiumScreen(isPremium = false) }
}
