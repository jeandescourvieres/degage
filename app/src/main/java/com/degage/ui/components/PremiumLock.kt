package com.degage.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.degage.ui.theme.NeonGreen

/** Petit badge cadenas à afficher à côté d'une fonctionnalité réservée aux abonnés Premium. */
@Composable
fun PremiumBadge() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Fonctionnalité Premium",
        tint = NeonGreen,
        modifier = Modifier.padding(start = 6.dp).size(16.dp)
    )
}

@Composable
fun PremiumLabel() {
    Text("Premium", color = NeonGreen, fontSize = 11.sp)
}
