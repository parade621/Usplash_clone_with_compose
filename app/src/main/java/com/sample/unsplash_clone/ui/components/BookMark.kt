package com.sample.unsplash_clone.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sample.unsplash_clone.R

@Composable
fun BookMark(
    modifier: Modifier = Modifier,
) {

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color.Transparent)
    ) {
        Image(
            painter = painterResource(id = R.drawable.vector_favorite),
            contentDescription = "BookMark",
            modifier = modifier
                .size(24.dp)
        )
    }
}

@Preview
@Composable
fun PreviewBookMark() {
    BookMark()
}