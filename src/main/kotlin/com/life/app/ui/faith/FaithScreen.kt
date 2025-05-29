package com.life.app.ui.faith

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.data.model.Religion
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.DateTimeUtils

@Composable
fun FaithScreen(
    viewModel: FaithViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LifeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with religion selection
                FaithHeader(
                    selectedReligion = uiState.selectedReligion,
                    onReligionSelected = { viewModel.selectReligion(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on selected religion
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (uiState.selectedReligion) {
                        Religion.ISLAM -> IslamContent(
                            uiState = uiState,
                            onPrayerCompleted = { viewModel.completePrayer(it) },
                            onGenerateMessage = { viewModel.generateReligiousMessage() },
                            onFastingToggle = { viewModel.toggleFasting() }
                        )
                        Religion.CHRISTIANITY -> ChristianityContent(
                            uiState = uiState,
                            onPrayerCompleted = { viewModel.completePrayer(it) },
                            onGenerateMessage = { viewModel.generateReligiousMessage() }
                        )
                        Religion.JUDAISM -> JudaismContent(
                            uiState = uiState,
                            onPrayerCompleted = { viewModel.completePrayer(it) },
                            onGenerateMessage = { viewModel.generateReligiousMessage() },
                            onToggleShabbat = { viewModel.toggleShabbat() }
                        )
                    }
                }
            }
            
            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorSnackbar(
                    message = uiState.errorMessage,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun FaithHeader(
    selectedReligion: Religion,
    onReligionSelected: (Religion) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.faith),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Religion selection chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReligionChip(
                religion = Religion.ISLAM,
                isSelected = selectedReligion == Religion.ISLAM,
                onSelected = { onReligionSelected(Religion.ISLAM) }
            )
            
            ReligionChip(
                religion = Religion.CHRISTIANITY,
                isSelected = selectedReligion == Religion.CHRISTIANITY,
                onSelected = { onReligionSelected(Religion.CHRISTIANITY) }
            )
            
            ReligionChip(
                religion = Religion.JUDAISM,
                isSelected = selectedReligion == Religion.JUDAISM,
                onSelected = { onReligionSelected(Religion.JUDAISM) }
            )
        }
    }
}

@Composable
fun ReligionChip(
    religion: Religion,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val (icon, label) = when (religion) {
        Religion.ISLAM -> Pair("☪", stringResource(R.string.islam))
        Religion.CHRISTIANITY -> Pair("✝", stringResource(R.string.christianity))
        Religion.JUDAISM -> Pair("✡", stringResource(R.string.judaism))
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onSelected,
        label = { Text(label) },
        leadingIcon = {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
        },
        modifier = Modifier.height(40.dp)
    )
}

@Composable
fun IslamContent(
    uiState: FaithUiState,
    onPrayerCompleted: (String) -> Unit,
    onGenerateMessage: () -> Unit,
    onFastingToggle: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Quran verse of the day
        ReligiousMessageCard(
            message = uiState.religiousMessage,
            isGenerating = uiState.isGeneratingMessage,
            onGenerateMessage = onGenerateMessage,
            title = stringResource(R.string.quran_verse_of_the_day)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Prayer times
        Text(
            text = stringResource(R.string.prayer_times),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Prayer cards
        val prayers = listOf(
            Triple("Fajr", uiState.prayerTimes["Fajr"] ?: "", uiState.completedPrayers.contains("Fajr")),
            Triple("Dhuhr", uiState.prayerTimes["Dhuhr"] ?: "", uiState.completedPrayers.contains("Dhuhr")),
            Triple("Asr", uiState.prayerTimes["Asr"] ?: "", uiState.completedPrayers.contains("Asr")),
            Triple("Maghrib", uiState.prayerTimes["Maghrib"] ?: "", uiState.completedPrayers.contains("Maghrib")),
            Triple("Isha", uiState.prayerTimes["Isha"] ?: "", uiState.completedPrayers.contains("Isha"))
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayers) { (name, time, completed) ->
                PrayerCard(
                    name = name,
                    time = time,
                    completed = completed,
                    onCompleted = { onPrayerCompleted(name) }
                )
            }
            
            // Fasting tracker
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.fasting_tracker),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FastingCard(
                    isFasting = uiState.isFasting,
                    onToggle = onFastingToggle
                )
            }
        }
    }
}

@Composable
fun ChristianityContent(
    uiState: FaithUiState,
    onPrayerCompleted: (String) -> Unit,
    onGenerateMessage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bible verse of the day
        ReligiousMessageCard(
            message = uiState.religiousMessage,
            isGenerating = uiState.isGeneratingMessage,
            onGenerateMessage = onGenerateMessage,
            title = stringResource(R.string.bible_verse_of_the_day)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Prayer goals
        Text(
            text = stringResource(R.string.prayer_goals),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Prayer cards
        val prayers = listOf(
            Triple("Morning Prayer", "6:00 AM", uiState.completedPrayers.contains("Morning Prayer")),
            Triple("Midday Prayer", "12:00 PM", uiState.completedPrayers.contains("Midday Prayer")),
            Triple("Evening Prayer", "6:00 PM", uiState.completedPrayers.contains("Evening Prayer"))
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayers) { (name, time, completed) ->
                PrayerCard(
                    name = name,
                    time = time,
                    completed = completed,
                    onCompleted = { onPrayerCompleted(name) }
                )
            }
            
            // Reflection space
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.reflection_space),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ReflectionCard()
            }
        }
    }
}

@Composable
fun JudaismContent(
    uiState: FaithUiState,
    onPrayerCompleted: (String) -> Unit,
    onGenerateMessage: () -> Unit,
    onToggleShabbat: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Torah passage of the day
        ReligiousMessageCard(
            message = uiState.religiousMessage,
            isGenerating = uiState.isGeneratingMessage,
            onGenerateMessage = onGenerateMessage,
            title = stringResource(R.string.torah_passage_of_the_day)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Prayer times
        Text(
            text = stringResource(R.string.prayer_times),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Prayer cards
        val prayers = listOf(
            Triple("Shacharit", "Morning", uiState.completedPrayers.contains("Shacharit")),
            Triple("Mincha", "Afternoon", uiState.completedPrayers.contains("Mincha")),
            Triple("Maariv", "Evening", uiState.completedPrayers.contains("Maariv"))
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prayers) { (name, time, completed) ->
                PrayerCard(
                    name = name,
                    time = time,
                    completed = completed,
                    onCompleted = { onPrayerCompleted(name) }
                )
            }
            
            // Shabbat reminder
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.shabbat_reminder),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ShabbatCard(
                    isShabbat = uiState.isShabbat,
                    onToggle = onToggleShabbat
                )
            }
            
            // Mitzvot tracker
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.mitzvot_tracker),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                MitzvotCard(completedMitzvot = uiState.completedMitzvot)
            }
        }
    }
}

@Composable
fun ReligiousMessageCard(
    message: String,
    isGenerating: Boolean,
    onGenerateMessage: () -> Unit,
    title: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = onGenerateMessage,
                    enabled = !isGenerating
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.generate_new_message),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (message.isNotEmpty()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Text(
                    text = stringResource(R.string.tap_refresh_for_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PrayerCard(
    name: String,
    time: String,
    completed: Boolean,
    onCompleted: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCompleted),
        shape = RoundedCornerShape(12.dp),
        color = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prayer icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (completed) Icons.Default.Check else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Prayer details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (completed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (completed) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Checkbox
            Checkbox(
                checked = completed,
                onCheckedChange = { onCompleted() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun FastingCard(
    isFasting: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isFasting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isFasting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NoFood,
                    contentDescription = null,
                    tint = if (isFasting) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.fasting_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFasting) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isFasting) stringResource(R.string.currently_fasting) else stringResource(R.string.not_fasting_today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFasting) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Switch
            Switch(
                checked = isFasting,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun ReflectionCard() {
    var reflectionText by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.daily_reflection),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = reflectionText,
                onValueChange = { reflectionText = it },
                placeholder = { Text(stringResource(R.string.write_your_reflection)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { /* Save reflection */ },
                modifier = Modifier.align(Alignment.End),
                enabled = reflectionText.isNotBlank()
            ) {
                Text(stringResource(R.string.save_reflection))
            }
        }
    }
}

@Composable
fun ShabbatCard(
    isShabbat: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isShabbat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isShabbat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Nightlight,
                    contentDescription = null,
                    tint = if (isShabbat) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.shabbat),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isShabbat) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isShabbat) stringResource(R.string.currently_observing) else stringResource(R.string.not_shabbat),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isShabbat) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Switch
            Switch(
                checked = isShabbat,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun MitzvotCard(completedMitzvot: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.mitzvot_completed),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = completedMitzvot.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { completedMitzvot.toFloat() / 613f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.out_of_613),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        action = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dismiss))
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.dismiss)
                )
            }
        }
    ) {
        Text(message)
    }
}