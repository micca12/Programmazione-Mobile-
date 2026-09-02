package com.micca.taskmanager.uicompose.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micca.taskmanager.uicompose.R
import com.micca.taskmanager.uicompose.common.errorMessage

/**
 * Schermata di login/registrazione: un form solo col toggle, l'errore resta
 * a video. email e password in rememberSaveable, cosi' non si perdono se ruoto.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val isLoginMode by viewModel.isLoginMode.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val needsConfirmation by viewModel.signUpNeedsConfirmation.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var localValidationError by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
    ) {
        Text(
            text = stringResource(
                if (isLoginMode) R.string.auth_login_title else R.string.auth_signup_title
            ),
            style = MaterialTheme.typography.headlineLarge,
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = stringResource(R.string.auth_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                localValidationError = false
            },
            label = { Text(text = stringResource(R.string.auth_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = localValidationError,
            modifier = Modifier.fillMaxWidth(),
        )

        // Errori: prima la validazione locale (password corta, come il
        // minlength=6 del web), poi quella del server
        if (localValidationError) {
            Text(
                text = stringResource(R.string.auth_password_too_short),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            val error = authError
            if (error != null) {
                Text(
                    text = errorMessage(error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (needsConfirmation) {
            Text(
                text = stringResource(R.string.auth_check_email),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = {
                if (password.length < 6) {
                    localValidationError = true
                } else {
                    viewModel.submit(email = email.trim(), password = password)
                }
            },
            enabled = !isLoading && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(4.dp))
            } else {
                Text(
                    text = stringResource(
                        if (isLoginMode) R.string.auth_login_button else R.string.auth_signup_button
                    )
                )
            }
        }

        TextButton(onClick = { viewModel.toggleMode() }) {
            Text(
                text = stringResource(
                    if (isLoginMode) R.string.auth_switch_to_signup else R.string.auth_switch_to_login
                )
            )
        }
    }
}
