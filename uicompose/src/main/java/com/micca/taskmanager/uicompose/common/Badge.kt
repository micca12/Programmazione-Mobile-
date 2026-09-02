package com.micca.taskmanager.uicompose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Pillola colorata; cliccabile se passo onClick.
 *  Se onClick è presente diventa un bottone. */
@Composable
fun Badge(
    text: String,
    colorName: String?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = badgeColorsFor(colorName)
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        color = colors.content,
        modifier = modifier
            .background(color = colors.container, shape = CircleShape)
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
