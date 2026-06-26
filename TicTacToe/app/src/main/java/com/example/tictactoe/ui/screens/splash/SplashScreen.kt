package com.example.tictactoe.ui.screens.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    ready: Boolean,
    onContinue: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logoScale",
    )

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(ready) {
        if (ready) {
            delay(1100)
            onContinue()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF4F3FF0), Color(0xFF3A2FB0)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LogoMark(modifier = Modifier.scale(scale))
            Text(
                "TicTac",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(
                "three in a row, beautifully",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
        }
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp)
                .size(30.dp),
        )
    }
}

@Composable
private fun LogoMark(modifier: Modifier = Modifier) {
    val cell = @Composable { bg: Color, fg: Color, mark: String ->
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(mark, color = fg, fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
    Box(
        modifier = modifier
            .size(104.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cell(Color(0xFFE2F7F4), Color(0xFF14B8A6), "X")
                cell(Color(0xFFFEEEE2), Color(0xFFF97316), "O")
            }
            Row(Modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                cell(Color(0xFFFEEEE2), Color(0xFFF97316), "O")
                cell(Color(0xFFE2F7F4), Color(0xFF14B8A6), "X")
            }
        }
    }
}
