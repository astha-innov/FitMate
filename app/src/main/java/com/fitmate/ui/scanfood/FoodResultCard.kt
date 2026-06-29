package com.fitmate.ui.scanfood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fitmate.domain.model.FoodItem

// ── Palette (dark sheet background) ───────────────────────────────────────────
private val SheetBg        = Color(0xFF0F1117)
private val CardBg         = Color(0xFF1A1E2A)
private val CardBorder     = Color(0xFF2A2F3E)
private val ScanGreen      = Color(0xFF16C47F)
private val ScanGreenLight = Color(0x2016C47F)
private val ScanGreenDim   = Color(0xFF0FA363)
private val TextPrimary    = Color(0xFFEFF1F5)
private val TextSecondary  = Color(0xFF9CA3AF)
private val TextHint       = Color(0xFF6B7280)
private val DividerColor   = Color(0xFF1F2535)

// NutriScore palette
private val NutriA = Color(0xFF038141)
private val NutriB = Color(0xFF85BB2F)
private val NutriC = Color(0xFFFECC02)
private val NutriD = Color(0xFFEE8100)
private val NutriE = Color(0xFFE63E11)

// NOVA palette
private val Nova1Color = Color(0xFF27AE60)
private val Nova2Color = Color(0xFFF39C12)
private val Nova3Color = Color(0xFFE67E22)
private val Nova4Color = Color(0xFFE74C3C)

// ── FoodResultCard ─────────────────────────────────────────────────────────────
@Composable
fun FoodResultCard(
    food:               FoodItem,
    onAddToDailyIntake: () -> Unit,
    onScanAgain:        () -> Unit,
    onClose:            () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
    ) {

        // ── Handle bar + close ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .align(Alignment.Center)
            )
            IconButton(
                onClick  = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(CardBg)
                    .border(1.dp, CardBorder, CircleShape)
            ) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Close",
                    tint               = TextSecondary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(400)) + expandVertically(tween(400))
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── Product Image ──────────────────────────────────────────────
                if (!food.imageUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardBg)
                    ) {
                        AsyncImage(
                            model              = food.imageUrl,
                            contentDescription = food.name,
                            modifier           = Modifier.fillMaxWidth(),
                            contentScale       = ContentScale.Fit
                        )
                        // Gradient fade at bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, SheetBg)
                                    )
                                )
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Product Name & Brand ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text       = food.name,
                        color      = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize   = 22.sp,
                        lineHeight = 28.sp
                    )
                    if (food.brand.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text      = food.brand,
                            color     = TextSecondary,
                            fontSize  = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!food.quantity.isNullOrEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text      = food.quantity!!,
                            color     = TextHint,
                            fontSize  = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Score Badges Row (NutriScore + NOVA) ─────────────────────
                if (food.nutriScore != null || food.novaGroup != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        food.nutriScore?.let { score ->
                            NutriScoreBadge(
                                score    = score.uppercase(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        food.novaGroup?.let { nova ->
                            NovaGroupBadge(
                                group    = nova,
                                modifier = if (food.nutriScore != null) Modifier.weight(1f) else Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ── Calorie Hero ──────────────────────────────────────────────
                CalorieHeroCard(food = food)

                Spacer(Modifier.height(16.dp))

                // ── Macro Grid ────────────────────────────────────────────────
                MacroGrid(food = food)

                Spacer(Modifier.height(16.dp))

                // ── Detailed Nutrition ────────────────────────────────────────
                DetailedNutritionCard(food = food)

                Spacer(Modifier.height(16.dp))

                // ── Health Indicator ──────────────────────────────────────────
                HealthIndicatorCard(food = food)

                Spacer(Modifier.height(16.dp))

                // ── Ingredients ───────────────────────────────────────────────
                if (!food.ingredients.isNullOrEmpty()) {
                    IngredientsCard(ingredients = food.ingredients!!)
                    Spacer(Modifier.height(16.dp))
                }

                // ── Action Buttons ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary CTA
                    Button(
                        onClick  = onAddToDailyIntake,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ScanGreen),
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Add To Daily Intake",
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                    }

                    // Secondary: Scan Again
                    OutlinedButton(
                        onClick  = onScanAgain,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Scan Again",
                            color      = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── Calorie Hero Card ──────────────────────────────────────────────────────────
@Composable
private fun CalorieHeroCard(food: FoodItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(ScanGreen.copy(alpha = 0.25f), ScanGreenLight)
                )
            )
            .border(1.dp, ScanGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text      = "CALORIES",
                    color     = ScanGreen,
                    fontSize  = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text       = "${food.calories.toInt()}",
                    color      = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize   = 48.sp,
                    lineHeight = 52.sp
                )
                Text(
                    text      = "kcal per serving",
                    color     = TextSecondary,
                    fontSize  = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ScanGreen.copy(alpha = 0.15f))
                    .border(1.dp, ScanGreen.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint               = ScanGreen,
                    modifier           = Modifier.size(32.dp)
                )
            }
        }
    }
}

// ── Macro Grid (Protein / Fat / Carbs / Fiber) ─────────────────────────────────
@Composable
private fun MacroGrid(food: FoodItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MacroTile(
            label    = "Protein",
            value    = "${food.protein}g",
            color    = Color(0xFF60A5FA),
            icon     = Icons.Default.Restaurant,
            modifier = Modifier.weight(1f)
        )
        MacroTile(
            label    = "Fat",
            value    = "${food.fat}g",
            color    = Color(0xFFFBBF24),
            icon     = Icons.Default.Scale,
            modifier = Modifier.weight(1f)
        )
        MacroTile(
            label    = "Carbs",
            value    = "${food.carbs}g",
            color    = Color(0xFFA78BFA),
            icon     = Icons.Default.MonitorWeight,
            modifier = Modifier.weight(1f)
        )
        MacroTile(
            label    = "Fiber",
            value    = "${food.fiber}g",
            color    = Color(0xFF34D399),
            icon     = Icons.Outlined.Grain,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MacroTile(
    label:    String,
    value:    String,
    color:    Color,
    icon:     ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text       = value,
            color      = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
        Text(
            text      = label,
            color     = TextHint,
            fontSize  = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Detailed Nutrition Card ────────────────────────────────────────────────────
@Composable
private fun DetailedNutritionCard(food: FoodItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SectionHeader(
            icon  = Icons.Outlined.Science,
            title = "Nutrition Facts"
        )
        Spacer(Modifier.height(14.dp))

        NutritionDetailRow("Calories",  "${food.calories} kcal",  isHighlighted = true)
        NutritionDetailDivider()
        NutritionDetailRow("Protein",   "${food.protein} g")
        NutritionDetailDivider()
        NutritionDetailRow("Total Fat", "${food.fat} g")
        NutritionDetailDivider()
        NutritionDetailRow("Carbohydrates", "${food.carbs} g")
        NutritionDetailDivider()
        NutritionDetailRow("Fiber",  "${food.fiber} g",  isIndented = true)
        NutritionDetailDivider()
        NutritionDetailRow("Sugar",  "${food.sugar} g",  isIndented = true)
        NutritionDetailDivider()
        NutritionDetailRow("Salt",   "${food.salt} g")
    }
}

@Composable
private fun NutritionDetailRow(
    label:         String,
    value:         String,
    isHighlighted: Boolean = false,
    isIndented:    Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start    = if (isIndented) 16.dp else 0.dp,
                top      = 10.dp,
                bottom   = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = label,
            color      = if (isHighlighted) TextPrimary else TextSecondary,
            fontSize   = if (isHighlighted) 15.sp else 14.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text       = value,
            color      = if (isHighlighted) ScanGreen else TextPrimary,
            fontSize   = if (isHighlighted) 15.sp else 14.sp,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun NutritionDetailDivider() {
    HorizontalDivider(color = DividerColor, thickness = 1.dp)
}

// ── Health Indicator Card ──────────────────────────────────────────────────────
@Composable
private fun HealthIndicatorCard(food: FoodItem) {
    // Simple health score: higher protein + fiber = better; high fat + sugar + salt = worse
    val healthScore = calculateHealthScore(food)
    val (healthLabel, healthColor, healthDesc) = when {
        healthScore >= 75 -> Triple("Excellent", Color(0xFF16C47F), "Great nutritional profile")
        healthScore >= 55 -> Triple("Good",      Color(0xFF60A5FA), "Reasonably balanced")
        healthScore >= 35 -> Triple("Moderate",  Color(0xFFFBBF24), "Consume in moderation")
        else               -> Triple("Poor",     Color(0xFFEF4444), "Limited nutritional value")
    }

    val progressAnim by animateFloatAsState(
        targetValue   = healthScore / 100f,
        animationSpec = tween(800),
        label         = "health_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionHeader(
            icon  = Icons.Default.FiberManualRecord,
            title = "Health Indicator"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = healthLabel,
                    color      = healthColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp
                )
                Text(
                    text     = healthDesc,
                    color    = TextSecondary,
                    fontSize = 13.sp
                )
            }
            Text(
                text       = "${healthScore.toInt()}",
                color      = healthColor,
                fontWeight = FontWeight.Black,
                fontSize   = 36.sp
            )
        }

        LinearProgressIndicator(
            progress       = { progressAnim },
            modifier       = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color          = healthColor,
            trackColor     = CardBorder,
            strokeCap      = StrokeCap.Round
        )

        // Mini breakdown chips
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HealthChip(
                label    = "Protein",
                isGood   = food.protein >= 5.0,
                modifier = Modifier.weight(1f)
            )
            HealthChip(
                label    = "Sugar",
                isGood   = food.sugar <= 10.0,
                modifier = Modifier.weight(1f)
            )
            HealthChip(
                label    = "Salt",
                isGood   = food.salt <= 1.5,
                modifier = Modifier.weight(1f)
            )
            HealthChip(
                label    = "Fiber",
                isGood   = food.fiber >= 3.0,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HealthChip(
    label:    String,
    isGood:   Boolean,
    modifier: Modifier = Modifier
) {
    val chipColor = if (isGood) Color(0xFF16C47F) else Color(0xFFEF4444)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipColor.copy(alpha = 0.12f))
            .border(1.dp, chipColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text      = if (isGood) "✓" else "✗",
            color     = chipColor,
            fontWeight = FontWeight.Black,
            fontSize  = 14.sp
        )
        Text(
            text      = label,
            color     = chipColor,
            fontSize  = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis
        )
    }
}

private fun calculateHealthScore(food: FoodItem): Float {
    var score = 50f
    // Positive signals
    score += (food.protein.coerceAtMost(30.0) / 30.0 * 25).toFloat()
    score += (food.fiber.coerceAtMost(10.0)   / 10.0 * 15).toFloat()
    // Negative signals
    score -= (food.sugar.coerceAtMost(30.0)   / 30.0 * 20).toFloat()
    score -= (food.salt.coerceAtMost(5.0)     / 5.0  * 10).toFloat()
    score -= ((food.fat - 5).coerceAtLeast(0.0).coerceAtMost(20.0) / 20.0 * 10).toFloat()
    return score.coerceIn(0f, 100f)
}

// ── Ingredients Card ──────────────────────────────────────────────────────────
@Composable
private fun IngredientsCard(ingredients: String) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (expanded || ingredients.length <= 200) ingredients
    else ingredients.take(200) + "…"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            icon  = Icons.Outlined.Inventory2,
            title = "Ingredients"
        )
        Text(
            text      = displayText,
            color     = TextSecondary,
            fontSize  = 13.sp,
            lineHeight = 20.sp
        )
        if (ingredients.length > 200) {
            androidx.compose.material3.TextButton(
                onClick          = { expanded = !expanded },
                contentPadding   = PaddingValues(0.dp)
            ) {
                Text(
                    text       = if (expanded) "Show less" else "Show more",
                    color      = ScanGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp
                )
            }
        }
    }
}

// ── NutriScore Badge ───────────────────────────────────────────────────────────
@Composable
private fun NutriScoreBadge(score: String, modifier: Modifier = Modifier) {
    val grades  = listOf("A", "B", "C", "D", "E")
    val colors  = listOf(NutriA, NutriB, NutriC, NutriD, NutriE)
    val activeIndex = grades.indexOf(score.uppercase()).takeIf { it >= 0 } ?: 2

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text       = "NUTRI-SCORE",
            color      = TextHint,
            fontSize   = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            grades.forEachIndexed { index, grade ->
                val isActive = index == activeIndex
                Box(
                    modifier = Modifier
                        .size(if (isActive) 36.dp else 28.dp)
                        .clip(RoundedCornerShape(if (isActive) 10.dp else 8.dp))
                        .background(if (isActive) colors[index] else colors[index].copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = grade,
                        color      = if (isActive) Color.White else colors[index].copy(alpha = 0.7f),
                        fontWeight = FontWeight.Black,
                        fontSize   = if (isActive) 16.sp else 12.sp
                    )
                }
            }
        }
    }
}

// ── NOVA Group Badge ───────────────────────────────────────────────────────────
@Composable
private fun NovaGroupBadge(group: Int, modifier: Modifier = Modifier) {
    val (novaColor, novaLabel) = when (group) {
        1    -> Pair(Nova1Color, "Unprocessed")
        2    -> Pair(Nova2Color, "Processed\nIngredients")
        3    -> Pair(Nova3Color, "Processed")
        4    -> Pair(Nova4Color, "Ultra-Processed")
        else -> Pair(TextHint,  "Unknown")
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text       = "NOVA GROUP",
            color      = TextHint,
            fontSize   = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(novaColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "$group",
                    color      = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize   = 18.sp
                )
            }
            Text(
                text       = novaLabel,
                color      = novaColor,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                lineHeight = 17.sp
            )
        }
    }
}

// ── Section Header ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(ScanGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = ScanGreen,
                modifier           = Modifier.size(17.dp)
            )
        }
        Text(
            text       = title,
            color      = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 16.sp
        )
    }
}