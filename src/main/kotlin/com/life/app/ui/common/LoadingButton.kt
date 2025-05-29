package com.life.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A button that shows a loading indicator when in loading state.
 * Styled to match Apple's iOS design language.
 */
@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        contentPadding = contentPadding,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor,
            disabledContainerColor = color.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.8f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            }
            
            // Button text
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = text,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * A secondary style loading button with Apple-like design.
 */
@Composable
fun SecondaryLoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
) {
    LoadingButton(
        text = text,
        isLoading = isLoading,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = contentPadding,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
}