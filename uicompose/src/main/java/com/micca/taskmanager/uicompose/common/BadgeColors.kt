package com.micca.taskmanager.uicompose.common

import androidx.compose.ui.graphics.Color

/**
 * Colori dei badge. I nomi arrivano dal DB (colonna `color` di
 * priorities/statuses/tags) e sono gli stessi della web app, che li mappava
 * a classi Tailwind (bg-300 / text-700): qui replichiamo la stessa palette.
 */
data class BadgeColors(
    val container: Color,
    val content: Color,
)

fun badgeColorsFor(name: String?): BadgeColors = when (name) {
    "slate" -> BadgeColors(container = Color(0xFFCBD5E1), content = Color(0xFF334155))
    "sky" -> BadgeColors(container = Color(0xFF7DD3FC), content = Color(0xFF0369A1))
    "teal" -> BadgeColors(container = Color(0xFF5EEAD4), content = Color(0xFF0F766E))
    "emerald" -> BadgeColors(container = Color(0xFF6EE7B7), content = Color(0xFF047857))
    "amber" -> BadgeColors(container = Color(0xFFFCD34D), content = Color(0xFFB45309))
    "orange" -> BadgeColors(container = Color(0xFFFDBA74), content = Color(0xFFC2410C))
    "rose" -> BadgeColors(container = Color(0xFFFDA4AF), content = Color(0xFFBE123C))
    // fallback identico al web: grigio
    else -> BadgeColors(container = Color(0xFFE5E7EB), content = Color(0xFF374151))
}
