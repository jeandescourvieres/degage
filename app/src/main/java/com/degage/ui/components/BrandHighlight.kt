package com.degage.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.degage.ui.theme.NeonGreen

/** Met "Tu dégages" en évidence en vert fluo dans le texte, quelle que soit la langue. */
fun highlightBrand(text: String): AnnotatedString = buildAnnotatedString {
    val brand = "Tu dégages"
    var start = 0
    while (true) {
        val index = text.indexOf(brand, start, ignoreCase = true)
        if (index == -1) {
            append(text.substring(start))
            break
        }
        append(text.substring(start, index))
        withStyle(SpanStyle(color = NeonGreen)) {
            append(text.substring(index, index + brand.length).uppercase())
        }
        start = index + brand.length
    }
}
