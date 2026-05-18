package com.queryb.medlog.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.queryb.medlog.R

@Composable
fun MedLogLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_medlog_logo),
        contentDescription = "MedLog Logo",
        modifier = modifier.size(size)
    )
}