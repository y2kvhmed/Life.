package com.life.app.ui.nutrition

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.data.model.MealType
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.DateTimeUtils

@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel = hiltViewModel()
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
                // Header
                Text(
                    text = stringResource(R.string.nutrition),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tabs for Meals and Water
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    stringResource(R.string.meals),
                    stringResource(R.string.water)
                )
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> MealsTab(
                        meals = uiState.meals,
                        isLoading = uiState.isLoading,
                        onAddMeal = { viewModel.showAddMealDialog() },
                        onMealClick = { viewModel.selectMeal(it) },
                        onGenerateMealSuggestions = { viewModel.generateMealSuggestions() },
                        isGeneratingSuggestions = uiState.isGeneratingSuggestions,
                        mealSuggestions = uiState.mealSuggestions
                    )
                    1 -> WaterTab(
                        waterIntake = uiState.waterIntake,
                        isLoading = uiState.isLoading,
                        onAddWater = { amount -> viewModel.addWater(amount) }
                    )
                }
            }
            
            // Add Meal Dialog
            if (uiState.showAddMealDialog) {
                AddMealDialog(
                    onDismiss = { viewModel.hideAddMealDialog() },
                    onAddMeal = { name, type, description ->
                        viewModel.addMeal(name, type, description)
                    }
                )
            }
            
            // Meal Detail Dialog
            if (uiState.selectedMeal != null) {
                MealDetailDialog(
                    meal = uiState.selectedMeal!!,
                    onDismiss = { viewModel.deselectMeal() },
                    onDelete = { viewModel.deleteMeal(uiState.selectedMeal!!) }
                )
            }
            
            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorSnackbar(
                    message = uiState.errorMessage,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            // Add Meal FAB
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { viewModel.showAddMealDialog() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_meal)
                    )
                }
            }
        }
    }
}

@Composable
fun MealsTab(
    meals: List<MealUiModel>,
    isLoading: Boolean,
    onAddMeal: () -> Unit,
    onMealClick: (MealUiModel) -> Unit,
    onGenerateMealSuggestions: () -> Unit,
    isGeneratingSuggestions: Boolean,
    mealSuggestions: List<String>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Meal suggestions section
        MealSuggestionsSection(
            onGenerateSuggestions = onGenerateMealSuggestions,
            isGenerating = isGeneratingSuggestions,
            suggestions = mealSuggestions
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meals list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (meals.isEmpty()) {
            EmptyMealsView(onAddMeal = onAddMeal)
        } else {
            Text(
                text = stringResource(R.string.your_meals),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(meals) { meal ->
                    MealItem(
                        meal = meal,
                        onClick = { onMealClick(meal) }
                    )
                }
            }
        }
    }
}

@Composable
fun MealSuggestionsSection(
    onGenerateSuggestions: () -> Unit,
    isGenerating: Boolean,
    suggestions: List<String>
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
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(R.string.meal_suggestions),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onGenerateSuggestions,
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.generate))
                    }
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
            } else if (suggestions.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_suggestions_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                suggestions.forEach { suggestion ->
                    Text(
                        text = "• $suggestion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WaterTab(
    waterIntake: Float,
    isLoading: Boolean,
    onAddWater: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Water intake display
        WaterIntakeDisplay(
            waterIntake = waterIntake,
            isLoading = isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick add buttons
        Text(
            text = stringResource(R.string.quick_add),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WaterButton(amount = 0.1f, onAddWater = onAddWater)
            WaterButton(amount = 0.25f, onAddWater = onAddWater)
            WaterButton(amount = 0.5f, onAddWater = onAddWater)
            WaterButton(amount = 1f, onAddWater = onAddWater)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Custom amount input
        var customAmount by remember { mutableStateOf("") }
        
        Text(
            text = stringResource(R.string.custom_amount),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customAmount,
                onValueChange = { value ->
                    // Only allow numbers and decimal point
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                        customAmount = value
                    }
                },
                label = { Text(stringResource(R.string.liters)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    customAmount.toFloatOrNull()?.let { amount ->
                        if (amount > 0) {
                            onAddWater(amount)
                            customAmount = ""
                        }
                    }
                },
                enabled = customAmount.isNotEmpty() && customAmount.toFloatOrNull() != null && customAmount.toFloatOrNull()!! > 0
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }
}

@Composable
fun WaterIntakeDisplay(
    waterIntake: Float,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = String.format("%.1f L", waterIntake),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.today),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun WaterButton(
    amount: Float,
    onAddWater: (Float) -> Unit
) {
    Button(
        onClick = { onAddWater(amount) },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        modifier = Modifier.size(64.dp)
    ) {
        Text(
            text = if (amount < 1f) {
                String.format("+%d ml", (amount * 1000).toInt())
            } else {
                "+1 L"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyMealsView(onAddMeal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_meals_yet),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.track_your_first_meal),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddMeal,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.add_meal))
        }
    }
}

@Composable
fun MealItem(
    meal: MealUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal type icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getMealTypeColor(meal.type).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getMealTypeIcon(meal.type),
                    contentDescription = null,
                    tint = getMealTypeColor(meal.type),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Meal details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = DateTimeUtils.formatTime(meal.time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (String, MealType, String) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var mealDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_meal)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Meal Name
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text(stringResource(R.string.meal_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Type
                Text(
                    text = stringResource(R.string.meal_type),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.values().forEach { type ->
                        FilterChip(
                            selected = mealType == type,
                            onClick = { mealType = type },
                            label = { Text(getMealTypeName(type)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getMealTypeIcon(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Description
                OutlinedTextField(
                    value = mealDescription,
                    onValueChange = { mealDescription = it },
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mealName.isNotBlank()) {
                        onAddMeal(mealName, mealType, mealDescription)
                        onDismiss()
                    }
                },
                enabled = mealName.isNotBlank()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun MealDetailDialog(
    meal: MealUiModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(meal.name) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Meal Type and Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getMealTypeIcon(meal.type),
                        contentDescription = null,
                        tint = getMealTypeColor(meal.type),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = getMealTypeName(meal.type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getMealTypeColor(meal.type)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = DateTimeUtils.formatTime(meal.time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = DateTimeUtils.formatDate(meal.time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Description (if any)
                if (meal.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = meal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDelete()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.delete))
            }
        }
    )
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

// Helper functions
@Composable
fun getMealTypeName(type: MealType): String {
    return when (type) {
        MealType.BREAKFAST -> stringResource(R.string.breakfast)
        MealType.LUNCH -> stringResource(R.string.lunch)
        MealType.DINNER -> stringResource(R.string.dinner)
        MealType.SNACK -> stringResource(R.string.snack)
    }
}

fun getMealTypeIcon(type: MealType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        MealType.BREAKFAST -> Icons.Default.FreeBreakfast
        MealType.LUNCH -> Icons.Default.LunchDining
        MealType.DINNER -> Icons.Default.DinnerDining
        MealType.SNACK -> Icons.Default.Restaurant
    }
}

fun getMealTypeColor(type: MealType): Color {
    return when (type) {
        MealType.BREAKFAST -> Color(0xFF4CAF50) // Green
        MealType.LUNCH -> Color(0xFFFFA000)     // Amber
        MealType.DINNER -> Color(0xFF7B1FA2)    // Purple
        MealType.SNACK -> Color(0xFF00BCD4)     // Cyan
    }
}

// Data class for UI
data class MealUiModel(
    val id: Long,
    val name: String,
    val type: MealType,
    val description: String,
    val time: LocalDateTime
)