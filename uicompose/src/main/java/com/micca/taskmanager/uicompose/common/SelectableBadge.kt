package com.micca.taskmanager.uicompose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Badge dei filtri: quando e' selezionato diventa pieno (nero). */
@Composable
fun SelectableBadge(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .background(
                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
