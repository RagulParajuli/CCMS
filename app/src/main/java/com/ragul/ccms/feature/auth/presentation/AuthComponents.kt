package com.ragul.ccms.feature.auth.presentation

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ragul.ccms.R
import com.ragul.ccms.ui.theme.AuthBackground
import com.ragul.ccms.ui.theme.AuthBorder
import com.ragul.ccms.ui.theme.AuthErrorContainer
import com.ragul.ccms.ui.theme.AuthErrorContent
import com.ragul.ccms.ui.theme.AuthPrimary
import com.ragul.ccms.ui.theme.AuthSuccessContainer
import com.ragul.ccms.ui.theme.AuthSuccessContent
import com.ragul.ccms.ui.theme.AuthWarningContainer
import com.ragul.ccms.ui.theme.AuthWarningContent

/** Shared shell for every authentication screen. */
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
fun AuthHeader(title: String, subtitle: String) {
    Image(
        painter = painterResource(id = R.drawable.ic_leaf),
        contentDescription = null,
        modifier = Modifier.size(56.dp),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = title,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = subtitle,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val supportingContent: (@Composable () -> Unit)? = if (supportingText != null) {
        { Text(text = supportingText) }
    } else {
        null
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingContent,
        visualTransformation = visualTransformation,
        trailingIcon = trailingContent,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    imeAction: ImeAction = ImeAction.Done,
    isError: Boolean = false,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    AuthTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        isError = isError,
        supportingText = supportingText,
        enabled = enabled,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            TextButton(
                onClick = { visible = !visible },
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text(
                    text = if (visible) "Hide" else "Show",
                    fontSize = 12.sp,
                    color = AuthPrimary,
                )
            }
        },
    )
}

@Composable
fun PrimaryAuthButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimary,
            contentColor = Color.White,
            disabledContainerColor = AuthPrimary.copy(alpha = 0.55f),
            disabledContentColor = Color.White.copy(alpha = 0.85f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun GoogleAuthButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AuthBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = AuthPrimary,
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
        Text(
            text = "or",
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
    }
}

@Composable
fun MessageBanner(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (isError) AuthErrorContainer else AuthSuccessContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            color = if (isError) AuthErrorContent else AuthSuccessContent,
            fontSize = 14.sp,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
fun FirebaseSetupBanner(modifier: Modifier = Modifier) {
    MessageBanner(
        message = "Firebase is not configured. Add app/google-services.json and rebuild. " +
            "See docs/FIREBASE_SETUP.md for the steps.",
        isError = true,
        modifier = modifier,
    )
}

@Composable
fun GoogleSetupBanner(modifier: Modifier = Modifier) {
    Surface(
        color = AuthWarningContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Google Sign-In is not configured. Enable the Google provider in Firebase, " +
                "add your SHA-1/SHA-256 fingerprint, then re-download google-services.json or " +
                "set GOOGLE_WEB_CLIENT_ID. See docs/FIREBASE_SETUP.md.",
            color = AuthWarningContent,
            fontSize = 14.sp,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
fun AuthFooter(
    question: String,
    action: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = question,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onActionClick) {
            Text(
                text = action,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AuthPrimary,
            )
        }
    }
}
