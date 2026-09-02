package com.micca.taskmanager.uicompose.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.micca.taskmanager.domain.models.ErrorState
import com.micca.taskmanager.uicompose.R

/**
 * Traduce l'ErrorState (tipizzato, indipendente dalla lingua) nella stringa
 * localizzata: la mappatura errore -> testo vive solo nella UI.
 */
@Composable
fun errorMessage(error: ErrorState): String = when (error) {
    ErrorState.Network -> stringResource(R.string.error_network)
    ErrorState.Unauthorized -> stringResource(R.string.error_unauthorized)
    ErrorState.Generic -> stringResource(R.string.error_generic)
}

@Composable
fun ErrorBanner(
    error: ErrorState,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Text(
                text = errorMessage(error),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.home_retry))
                }
            }
        }
    }
}
