package com.example.willog_unsplash.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DetailInfo(type: String = "type", value: String = "value") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = type,
            style = typo.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(12.dp)
        )
        Spacer(modifier = Modifier.weight(1f)) // 여기서 남은 공간을 차지합니다.
        Text(
            text = value,
            style = typo.labelMedium,
            color = ligthHeavyGray,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Preview
@Composable
fun PreviewDetailInfo() {
    DetailInfo()
}