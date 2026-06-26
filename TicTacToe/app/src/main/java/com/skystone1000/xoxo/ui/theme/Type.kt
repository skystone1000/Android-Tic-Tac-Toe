package com.skystone1000.xoxo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.skystone1000.xoxo.R

@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) =
    Font(
        resId = resId,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    )

/** Space Grotesk — display, scores, numerals. */
val SpaceGrotesk = FontFamily(
    variableFont(R.font.space_grotesk_variable, FontWeight.Medium),
    variableFont(R.font.space_grotesk_variable, FontWeight.SemiBold),
    variableFont(R.font.space_grotesk_variable, FontWeight.Bold),
)

/** Hanken Grotesk — body, labels, UI. */
val HankenGrotesk = FontFamily(
    variableFont(R.font.hanken_grotesk_variable, FontWeight.Normal),
    variableFont(R.font.hanken_grotesk_variable, FontWeight.Medium),
    variableFont(R.font.hanken_grotesk_variable, FontWeight.SemiBold),
    variableFont(R.font.hanken_grotesk_variable, FontWeight.Bold),
)

// Type scale from the design doc. Space Grotesk for display/headline numerals,
// Hanken Grotesk for everything textual.
val TicTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = HankenGrotesk, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp,
    ),
)
