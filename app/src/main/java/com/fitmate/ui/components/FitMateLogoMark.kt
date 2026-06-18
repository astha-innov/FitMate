package com.fitmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.fitmate.R

@Composable
fun FitMateLogoMark(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.mipmap.ic_launcher_foreground),
        contentDescription = "FitMate logo",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
