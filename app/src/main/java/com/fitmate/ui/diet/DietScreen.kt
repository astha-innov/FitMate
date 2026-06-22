package com.fitmate.ui.diet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.fitmate.R
import kotlinx.coroutines.launch

// ─── Theme Palette ────────────────────────────────────────────────────────────
private val AppBackground    = Color(0xFFF5F5F5)
private val AppAccentGreen   = Color(0xFFD8FF3E)
private val AppText          = Color.Black
private val AppSecondaryText = Color(0xFF8A8A8A)
private val AppCardWhite     = Color.White
private val AppMacroProtein  = Color(0xFF2F54FF)
private val AppMacroCarbs    = Color(0xFFFF9F0A)
private val AppMacroFat      = Color(0xFFFF3B30)

// ─── Goal Config ──────────────────────────────────────────────────────────────
data class GoalConfig(
    val emoji: String,
    val label: String,
    val subtitle: String,
    val gradientStart: Color,
    val gradientEnd: Color
)

val goalConfigs = listOf(
    GoalConfig("🔥", "Muscle Gain",           "Build strength with high-protein nutrition.",         Color(0xFF1A1A2E), Color(0xFF16213E)),
    GoalConfig("⚖️", "Fat Loss",              "Stay in a calorie deficit without sacrificing health.",Color(0xFF0F3460), Color(0xFF533483)),
    GoalConfig("💪", "Lean Body",             "Balanced nutrition for a lean physique.",              Color(0xFF1B4332), Color(0xFF2D6A4F)),
    GoalConfig("🧘", "Reduce Stress & Relax", "Foods that help your body recover and relax.",         Color(0xFF4A1942), Color(0xFF6D3B47)),
    GoalConfig("🏃", "Cardio / Stamina",      "Fuel your endurance and performance.",                 Color(0xFF7B2D00), Color(0xFFBF4E00)),
    GoalConfig("🤸", "Flexibility & Mobility","Support recovery and mobility through nutrition.",      Color(0xFF003049), Color(0xFF0077B6))
)

val dietOptions = listOf(
    Pair("🥬", "Vegetarian"),
    Pair("🥚", "Eggetarian"),
    Pair("🍗", "Non Vegetarian")
)

// Meal slot UI metadata
data class MealSlotMeta(val emoji: String, val label: String, val slotKey: String)
val mealSlots = listOf(
    MealSlotMeta("🍳", "Breakfast", "Breakfast"),
    MealSlotMeta("🍱", "Lunch",     "Lunch"),
    MealSlotMeta("🌙", "Dinner",    "Dinner"),
    MealSlotMeta("🥜", "Snacks",    "Snacks")
)

// ─── Domain Model ─────────────────────────────────────────────────────────────
data class DietMealItem(
    val name: String,
    val calories: String,
    val protein: String,
    val mealType: String,
    val imageRes: Int,
    val isNonVeg: Boolean,
    val hasEgg: Boolean,
    val isHighProtein: Boolean,
    val isLowCalorie: Boolean
)

// ─── Repository ───────────────────────────────────────────────────────────────
object FoodImageRepository {
    val allMeals = listOf(
        // BREAKFAST
        DietMealItem("Peanut Butter Oats",    "450 kcal", "20g", "Breakfast", R.drawable.peanut_butter_oats,           false, false, true,  false),
        DietMealItem("Poached Egg Toast",      "320 kcal", "18g", "Breakfast", R.drawable.poached_egg_brown_bread,      false, true,  true,  true),
        DietMealItem("Rava Upma",              "280 kcal", "8g",  "Breakfast", R.drawable.rava_upma,                    false, false, false, true),
        DietMealItem("Vegetable Poha",         "250 kcal", "6g",  "Breakfast", R.drawable.vegetable_poha,               false, false, false, true),
        DietMealItem("Boiled Eggs",            "140 kcal", "12g", "Breakfast", R.drawable.boiled_eggs,                  false, true,  true,  true),
        DietMealItem("Paneer Sandwich",        "380 kcal", "16g", "Breakfast", R.drawable.paneer_sandwich,              false, false, true,  false),
        DietMealItem("Besan Chilla & Curd",    "290 kcal", "14g", "Breakfast", R.drawable.besan_chilla_curd,            false, false, true,  true),
        // LUNCH
        DietMealItem("Mixed Paneer Pulao",     "520 kcal", "22g", "Lunch",     R.drawable.mixed_paneer_pulao,           false, false, true,  false),
        DietMealItem("Chole Rice",             "480 kcal", "15g", "Lunch",     R.drawable.chole_rice,                   false, false, false, false),
        DietMealItem("Dal Rice",               "410 kcal", "14g", "Lunch",     R.drawable.rice_dal,                     false, false, false, false),
        DietMealItem("Fish Curry & Rice",      "450 kcal", "32g", "Lunch",     R.drawable.fish_rice,                    true,  false, true,  false),
        DietMealItem("Egg Fried Rice",         "420 kcal", "18g", "Lunch",     R.drawable.egg_fried_rice,               false, true,  true,  false),
        DietMealItem("Tofu Rice Bowl",         "390 kcal", "24g", "Lunch",     R.drawable.tofu_rice,                    false, false, true,  true),
        // DINNER
        DietMealItem("Paneer Bhurji & Roti",   "430 kcal", "24g", "Dinner",    R.drawable.paneer_bhurji_roti,           false, false, true,  false),
        DietMealItem("Soya Chunks & Roti",     "380 kcal", "28g", "Dinner",    R.drawable.soya_chunks_roti,             false, false, true,  true),
        DietMealItem("Mixed Veg & Roti",       "320 kcal", "10g", "Dinner",    R.drawable.roti_vegetables,              false, false, false, true),
        DietMealItem("Grilled Chicken Breast", "280 kcal", "45g", "Dinner",    R.drawable.chicken_breast,               true,  false, true,  true),
        DietMealItem("Chicken & Sweet Potato", "410 kcal", "38g", "Dinner",    R.drawable.chicken_grilled_sweet_potato, true,  false, true,  false),
        // SNACKS
        DietMealItem("Fruit Chaat",            "150 kcal", "2g",  "Snack",     R.drawable.fruit_chaat,                  false, false, false, true),
        DietMealItem("Dry Fruits Mix",         "220 kcal", "6g",  "Snack",     R.drawable.dry_fruits_mix,               false, false, false, false),
        DietMealItem("Roasted Chana",          "180 kcal", "10g", "Snack",     R.drawable.roasted_chana,                false, false, true,  true),
        DietMealItem("Greek Yogurt",           "120 kcal", "15g", "Snack",     R.drawable.greek_yogurt,                 false, false, true,  true),
        DietMealItem("Tuna Salad",             "210 kcal", "28g", "Snack",     R.drawable.tuna_salad,                   true,  false, true,  true),
        // DRINKS
        DietMealItem("Whey Protein Shake",     "140 kcal", "25g", "Drink",     R.drawable.whey_protein_shake,           false, false, true,  true),
        DietMealItem("Banana Shake",           "280 kcal", "8g",  "Drink",     R.drawable.banana_shake,                 false, false, false, false),
        DietMealItem("Blueberry Smoothie",     "190 kcal", "5g",  "Drink",     R.drawable.blueberry_smoothie,           false, false, false, true),
        DietMealItem("Almond Milk",            "60 kcal",  "2g",  "Drink",     R.drawable.almond_milk,                  false, false, false, true),
        DietMealItem("Sattu Drink",            "180 kcal", "12g", "Drink",     R.drawable.sattu_drink,                  false, false, true,  true),
        DietMealItem("Black Coffee",           "5 kcal",   "0g",  "Drink",     R.drawable.black_coffee,                 false, false, false, true),
        DietMealItem("Green Tea",              "2 kcal",   "0g",  "Drink",     R.drawable.green_tea,                    false, false, false, true),
        DietMealItem("Lemon Ginger Tea",       "10 kcal",  "0g",  "Drink",     R.drawable.lemon_ginger_tea,             false, false, false, true),
        // SALADS
        DietMealItem("Fresh Garden Salad",     "110 kcal", "4g",  "Salad",     R.drawable.fresh_salad,                  false, false, false, true),
        DietMealItem("Classic Egg Salad",      "260 kcal", "16g", "Salad",     R.drawable.classic_egg_salad,            false, true,  true,  true),
        DietMealItem("Sprouts Salad",          "180 kcal", "14g", "Salad",     R.drawable.sprouts_salad,                false, false, true,  true),
        // SOUPS
        DietMealItem("Chicken Veg Soup",       "220 kcal", "24g", "Soup",      R.drawable.chicken_vegetable_soup,       true,  false, true,  true)
    )

    // Strict slot → mealType mapping (no cross-slot mixing)
    private fun slotToMealType(slot: String): String = when (slot) {
        "Breakfast" -> "Breakfast"
        "Lunch"     -> "Lunch"
        "Dinner"    -> "Dinner"
        "Snacks"    -> "Snack"
        else        -> slot
    }

    private fun applyDietFilter(meals: List<DietMealItem>, diet: String) = meals.filter { m ->
        when (diet) {
            "Vegetarian"     -> !m.isNonVeg && !m.hasEgg
            "Eggetarian"     -> !m.isNonVeg
            "Non Vegetarian" -> true
            else             -> true
        }
    }

    private fun applyGoalSort(meals: List<DietMealItem>, goal: String) = when (goal) {
        "Muscle Gain"            -> meals.sortedByDescending { it.isHighProtein }
        "Fat Loss"               -> meals.sortedByDescending { it.isLowCalorie }
        "Reduce Stress & Relax"  -> meals.sortedByDescending { it.isLowCalorie }
        "Cardio / Stamina"       -> meals.sortedByDescending { it.isHighProtein }
        "Flexibility & Mobility" -> meals.sortedByDescending { it.isLowCalorie }
        else                     -> meals
    }

    /** Used by "Meals For Your Goal" section – mixed types OK */
    fun getRecommendations(goal: String, diet: String): List<DietMealItem> {
        val filtered = applyGoalSort(applyDietFilter(allMeals, diet), goal)
        return filtered.take(6)
    }

    /** Strict: only meals that belong to the exact slot */
    fun getMealsForSlot(slot: String, goal: String, diet: String): List<DietMealItem> {
        val mealType = slotToMealType(slot)
        val slotMeals = allMeals.filter { it.mealType == mealType }
        val dietFiltered = applyDietFilter(slotMeals, diet)
        val goalSorted   = applyGoalSort(dietFiltered, goal)
        return goalSorted.take(6)
    }

    /** Count of available meals for a slot (used in slot card badge) */
    fun countForSlot(slot: String, goal: String, diet: String): Int =
        getMealsForSlot(slot, goal, diet).size
}

// ─── Badge helpers ─────────────────────────────────────────────────────────────
private fun DietMealItem.primaryBadge(): Pair<String, Color>? = when {
    isHighProtein && isNonVeg -> "Non Veg"     to Color(0xFFFF3B30)
    isHighProtein && hasEgg   -> "Eggetarian"  to Color(0xFFFF9F0A)
    isHighProtein             -> "High Protein" to AppMacroProtein
    isLowCalorie              -> "Low Calorie"  to Color(0xFF34C759)
    isNonVeg                  -> "Non Veg"     to Color(0xFFFF3B30)
    else                      -> null
}

private fun DietMealItem.suitableGoals(): String {
    val goals = mutableListOf<String>()
    if (isHighProtein) goals.add("Muscle Gain")
    if (isLowCalorie)  goals.add("Fat Loss")
    if (!isHighProtein && !isLowCalorie) goals.add("Lean Body")
    if (isLowCalorie)  goals.add("Reduce Stress")
    return if (goals.isEmpty()) "All Goals" else goals.joinToString(" · ")
}

private fun DietMealItem.dietLabel(): String = when {
    isNonVeg -> "Non Vegetarian"
    hasEgg   -> "Eggetarian"
    else     -> "Vegetarian"
}

// ═══════════════════════════════════════════════════════════════════════════════
// ROOT SCREEN
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizedNutritionScreen(modifier: Modifier = Modifier) {
    var userGoal by remember { mutableStateOf("Muscle Gain") }
    var userDiet by remember { mutableStateOf("Eggetarian") }

    // Progress constants
    val targetCalories = 2800; val targetProtein = 160
    val targetCarbs    = 320;  val targetFat     = 80
    val consumedCalories = 2380; val consumedProtein = 132
    val consumedCarbs    = 290;  val consumedFat     = 72

    // Bottom sheet
    var selectedSlot     by remember { mutableStateOf<String?>(null) }
    val sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope             = rememberCoroutineScope()

    // Meal detail dialog
    var detailMeal by remember { mutableStateOf<DietMealItem?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 1 ─ Animated Hero GIF banner
            item { AnimatedHeroBanner() }

            // 2 ─ Goal-aware hero card
            item {
                HeroBannerCard(goal = userGoal)
                Spacer(Modifier.height(24.dp))
            }

            // 3 ─ Today's progress
            item {
                TodayProgressSection(
                    consumedCal  = consumedCalories, targetCal  = targetCalories,
                    consumedPro  = consumedProtein,  targetPro  = targetProtein,
                    consumedCarb = consumedCarbs,    targetCarb = targetCarbs,
                    consumedFats = consumedFat,      targetFats = targetFat
                )
                Spacer(Modifier.height(24.dp))
            }

            // 4 ─ Nutrition insight
            item {
                NutritionInsightCard(consumedProtein, targetProtein)
                Spacer(Modifier.height(24.dp))
            }

            // 5 ─ Meals for your goal
            item {
                MealsForYourGoalSection(
                    goal   = userGoal,
                    diet   = userDiet,
                    onMealTap = { detailMeal = it }
                )
                Spacer(Modifier.height(24.dp))
            }

            // 6 ─ Water tracker
            item {
                WaterTrackerCard()
                Spacer(Modifier.height(24.dp))
            }

            // 7 ─ Today's meal plan
            item {
                TodayMealPlanSection(
                    goal = userGoal,
                    diet = userDiet,
                    onSlotTap = { slot ->
                        selectedSlot = slot
                        scope.launch { sheetState.show() }
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Modal Bottom Sheet ────────────────────────────────────────────────
        if (selectedSlot != null) {
            MealSelectionBottomSheet(
                slot        = selectedSlot!!,
                sheetState  = sheetState,
                initialGoal = userGoal,
                initialDiet = userDiet,
                onDismiss   = {
                    scope.launch { sheetState.hide() }
                    selectedSlot = null
                },
                onApply     = { newGoal, newDiet ->
                    userGoal = newGoal
                    userDiet = newDiet
                    scope.launch { sheetState.hide() }
                    selectedSlot = null
                },
                onMealTap   = { detailMeal = it }
            )
        }

        // ── Meal Detail Dialog ────────────────────────────────────────────────
        if (detailMeal != null) {
            MealDetailDialog(
                meal    = detailMeal!!,
                onClose = { detailMeal = null },
                onAdd   = { detailMeal = null }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 1. ANIMATED HERO BANNER (GIF via Coil)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun AnimatedHeroBanner() {
    val context = LocalContext.current

    // Build a Coil ImageLoader with GIF support
    val gifLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // GIF layer
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(R.drawable.salad_gif)
                .crossfade(true)
                .build(),
            imageLoader    = gifLoader,
            contentDescription = "Nutrition hero animation",
            contentScale   = ContentScale.Crop,
            modifier       = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
        )

        // Dark scrim for text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color.Black.copy(alpha = 0.25f),
                        1f   to Color.Black.copy(alpha = 0.72f)
                    )
                )
        )

        // Text overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, bottom = 22.dp, end = 22.dp)
        ) {
            Text(
                text = "FitMate Nutrition",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Your nutrition partner",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Eat smarter.  Train harder.  Recover better.",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppAccentGreen,
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. HERO BANNER CARD (goal-aware, gradient)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun HeroBannerCard(goal: String) {
    val config = goalConfigs.find { it.label == goal } ?: goalConfigs[0]
    val gradient = Brush.linearGradient(listOf(config.gradientStart, config.gradientEnd))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "CURRENT GOAL",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.2.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(config.emoji, fontSize = 34.sp)
                    Column {
                        Text(
                            config.label,
                            fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White
                        )
                        Text(
                            config.subtitle,
                            fontSize = 13.sp, fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.75f), lineHeight = 18.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(AppAccentGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.FlashOn, contentDescription = null,
                        tint = AppText, modifier = Modifier.size(13.dp))
                    Text("85% on track today", fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = AppText)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. TODAY'S PROGRESS
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun TodayProgressSection(
    consumedCal: Int, targetCal: Int,
    consumedPro: Int, targetPro: Int,
    consumedCarb: Int, targetCarb: Int,
    consumedFats: Int, targetFats: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Today's Progress", fontSize = 18.sp,
                fontWeight = FontWeight.Bold, color = AppText)
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                    CircularProgressIndicator(
                        progress = { consumedCal.toFloat() / targetCal.toFloat() },
                        modifier  = Modifier.fillMaxSize(),
                        color     = AppText,
                        trackColor = AppBackground,
                        strokeWidth = 9.dp,
                        strokeCap   = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$consumedCal", fontSize = 22.sp,
                            fontWeight = FontWeight.Black, color = AppText)
                        Text("/ $targetCal kcal", fontSize = 10.sp,
                            fontWeight = FontWeight.Medium, color = AppSecondaryText)
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    MacroMetricRow("Protein", consumedPro,  targetPro,  AppMacroProtein)
                    MacroMetricRow("Carbs",   consumedCarb, targetCarb, AppMacroCarbs)
                    MacroMetricRow("Fat",     consumedFats, targetFats, AppMacroFat)
                }
            }
        }
    }
}

@Composable
private fun MacroMetricRow(label: String, current: Int, target: Int, color: Color) {
    val progress by animateFloatAsState(
        targetValue   = (current.toFloat() / target.toFloat()).coerceAtMost(1f),
        animationSpec = tween(800),
        label         = "macro_$label"
    )
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Column(modifier = Modifier.width(112.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppText)
                Text("${current}g / ${target}g", fontSize = 10.sp, color = AppSecondaryText)
            }
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier
                .fillMaxWidth().height(4.dp)
                .background(AppBackground, CircleShape)) {
                Box(modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(color, CircleShape))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. NUTRITION INSIGHT
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun NutritionInsightCard(currentProtein: Int, targetProtein: Int) {
    val remaining = targetProtein - currentProtein
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardWhite)
    ) {
        Row(modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp)
                    .background(AppBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Lightbulb, null, tint = AppText,
                    modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Nutrition Insight", fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = AppSecondaryText)
                Text(
                    text = if (remaining > 0)
                        "Increase protein intake by ${remaining}g today to hit your macro target."
                    else
                        "Macro targets met perfectly. Excellent consistency today.",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppText,
                    lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 5. MEALS FOR YOUR GOAL
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MealsForYourGoalSection(
    goal: String,
    diet: String,
    onMealTap: (DietMealItem) -> Unit
) {
    val meals = remember(goal, diet) { FoodImageRepository.getRecommendations(goal, diet) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "Meals For Your Goal",
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(meals, key = { it.name }) { meal ->
                PremiumMealCard(meal = meal, onTap = { onMealTap(meal) })
            }
        }
    }
}

@Composable
fun PremiumMealCard(meal: DietMealItem, onTap: () -> Unit) {
    val badge = meal.primaryBadge()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "card_scale_${meal.name}"
    )

    Card(
        modifier = Modifier
            .width(220.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(bounded = true),
                onClick           = onTap
            ),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(meal.imageRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = meal.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    placeholder = painterResource(meal.imageRes),
                    error       = painterResource(meal.imageRes)
                )
                // Top-right badge
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(badge.second, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(badge.first, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                // Bottom-left meal type
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        meal.mealType.uppercase(),
                        fontSize = 8.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, letterSpacing = 0.8.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(meal.name, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = AppText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.FlashOn, null,
                            tint = AppSecondaryText, modifier = Modifier.size(13.dp))
                        Text(meal.calories, fontSize = 12.sp,
                            color = AppSecondaryText, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier
                            .background(AppBackground, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(meal.protein, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, color = AppMacroProtein)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 6. WATER TRACKER – fully fixed click handling + animated progress
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun WaterTrackerCard() {
    var waterCount by remember { mutableIntStateOf(5) }
    val targetGlasses = 8
    val progress by animateFloatAsState(
        targetValue   = waterCount.toFloat() / targetGlasses.toFloat(),
        animationSpec = tween(600),
        label         = "water_progress"
    )
    val goalReached = waterCount >= targetGlasses

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape    = RoundedCornerShape(22.dp),
        colors   = CardDefaults.cardColors(containerColor = AppCardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Water Intake", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = AppText)
                    Text("Daily Water Goal", fontSize = 12.sp,
                        color = AppSecondaryText, fontWeight = FontWeight.Medium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // DECREMENT – uses Box+clickable to guarantee hit area
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AppBackground, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = ripple(bounded = false, radius = 22.dp)
                            ) { if (waterCount > 0) waterCount-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Remove, "Remove glass",
                            modifier = Modifier.size(18.dp), tint = AppText)
                    }

                    Icon(Icons.Rounded.LocalDrink, null,
                        tint = Color(0xFF007AFF), modifier = Modifier.size(28.dp))

                    // INCREMENT
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AppAccentGreen, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = ripple(bounded = false, radius = 22.dp)
                            ) { if (waterCount < 20) waterCount++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, "Add glass",
                            modifier = Modifier.size(18.dp), tint = AppText)
                    }
                }
            }

            // Count label + animated progress bar
            Text(
                text = "$waterCount / $targetGlasses glasses",
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppText
            )
            LinearProgressIndicator(
                progress          = { progress },
                modifier          = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color             = Color(0xFF007AFF),
                trackColor        = AppBackground,
                strokeCap         = StrokeCap.Round
            )

            // Celebration banner
            AnimatedVisibility(
                visible = goalReached,
                enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
                exit    = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 2 }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        "🎉 Hydration Goal Completed!",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 7. TODAY'S MEAL PLAN – upgraded cards
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun TodayMealPlanSection(
    goal: String,
    diet: String,
    onSlotTap: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Today's Meal Plan", fontSize = 20.sp,
            fontWeight = FontWeight.Bold, color = AppText)
        mealSlots.forEach { slot ->
            val count = remember(goal, diet) {
                FoodImageRepository.countForSlot(slot.slotKey, goal, diet)
            }
            MealSlotCard(meta = slot, mealCount = count,
                onTap = { onSlotTap(slot.slotKey) })
        }
    }
}

@Composable
private fun MealSlotCard(
    meta: MealSlotMeta,
    mealCount: Int,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "slot_scale_${meta.label}"
    )
    val arrowOffset by animateFloatAsState(
        targetValue   = if (isPressed) 5f else 0f,
        animationSpec = tween(150),
        label         = "arrow_${meta.label}"
    )

    // Gradient accent strip color per slot
    val accentColor = when (meta.slotKey) {
        "Breakfast" -> Color(0xFFFFB347)
        "Lunch"     -> Color(0xFF4CAF50)
        "Dinner"    -> Color(0xFF5C6BC0)
        else        -> Color(0xFFFF7043)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(bounded = true),
                onClick           = onTap
            ),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = AppCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Accent strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.5f))),
                        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(AppBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(meta.emoji, fontSize = 20.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(meta.label, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = AppText)
                        Text(
                            if (mealCount > 0) "$mealCount Recommended Meals"
                            else "Tap to view meals",
                            fontSize = 12.sp, color = AppSecondaryText
                        )
                    }
                }
                Icon(
                    Icons.Rounded.ArrowForwardIos,
                    contentDescription = "Open",
                    tint   = AppSecondaryText,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = arrowOffset.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 8. MODAL BOTTOM SHEET – two-step (select then apply)
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealSelectionBottomSheet(
    slot:        String,
    sheetState:  SheetState,
    initialGoal: String,
    initialDiet: String,
    onDismiss:   () -> Unit,
    onApply:     (String, String) -> Unit,
    onMealTap:   (DietMealItem) -> Unit
) {
    // Local selections – don't commit until "Apply Plan"
    var pendingGoal by remember { mutableStateOf(initialGoal) }
    var pendingDiet by remember { mutableStateOf(initialDiet) }

    val filteredMeals = remember(pendingGoal, pendingDiet, slot) {
        FoodImageRepository.getMealsForSlot(slot, pendingGoal, pendingDiet)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor   = AppCardWhite,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp).height(4.dp)
                    .background(Color(0xFFDDDDDD), CircleShape)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(slot.uppercase(), fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppSecondaryText, letterSpacing = 1.sp)
                    Text("Choose Your Nutrition Goal", fontSize = 22.sp,
                        fontWeight = FontWeight.Black, color = AppText)
                }
            }

            // Goal chips
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    goalConfigs.forEach { gc ->
                        GoalChip(
                            emoji    = gc.emoji,
                            label    = gc.label,
                            selected = pendingGoal == gc.label,
                            gradient = Brush.linearGradient(
                                listOf(gc.gradientStart, gc.gradientEnd)),
                            onClick  = { pendingGoal = gc.label }   // ← no close
                        )
                    }
                }
            }

            // Divider
            item {
                Box(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp).height(1.dp)
                    .background(AppBackground))
            }

            // Diet preference
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Food Preference", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = AppText)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        dietOptions.forEach { (emoji, label) ->
                            DietChip(
                                emoji    = emoji,
                                label    = label,
                                selected = pendingDiet == label,
                                onClick  = { pendingDiet = label },  // ← no close
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Divider
            item {
                Box(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 24.dp).height(1.dp)
                    .background(AppBackground))
            }

            // Meal results
            item {
                AnimatedVisibility(
                    visible = filteredMeals.isNotEmpty(),
                    enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                    exit    = fadeOut(tween(200))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            "Suggested for $slot",
                            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppText,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredMeals, key = { it.name }) { meal ->
                                PremiumMealCard(meal = meal, onTap = { onMealTap(meal) })
                            }
                        }
                    }
                }
            }

            // Apply Plan button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick  = { onApply(pendingGoal, pendingDiet) },
                        modifier = Modifier.weight(2f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AppText)
                    ) {
                        Text("Apply Plan", fontWeight = FontWeight.Bold,
                            color = AppAccentGreen)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 9. MEAL DETAIL DIALOG
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MealDetailDialog(
    meal:    DietMealItem,
    onClose: () -> Unit,
    onAdd:   () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .animateContentSize(),
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppCardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                // Large image
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(meal.imageRes)
                            .crossfade(true)
                            .build(),
                        contentDescription = meal.name,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        placeholder = painterResource(meal.imageRes),
                        error       = painterResource(meal.imageRes)
                    )
                    // Close button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, "Close",
                            tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    // Meal type pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.Black.copy(0.55f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(meal.mealType.uppercase(), fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = Color.White,
                            letterSpacing = 0.8.sp)
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(meal.name, fontSize = 20.sp,
                        fontWeight = FontWeight.Black, color = AppText)

                    // Macro chips row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailChip("⚡ ${meal.calories}", Color(0xFFFFF3E0), Color(0xFFE65100))
                        DetailChip("💪 ${meal.protein} protein", Color(0xFFE8EAF6), AppMacroProtein)
                    }

                    // Info rows
                    DetailInfoRow("Category",     meal.mealType)
                    DetailInfoRow("Suitable for", meal.suitableGoals())
                    DetailInfoRow("Diet",         meal.dietLabel())

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick  = onClose,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape    = RoundedCornerShape(12.dp)
                        ) { Text("Close", fontWeight = FontWeight.Bold) }

                        Button(
                            onClick  = onAdd,
                            modifier = Modifier.weight(2f).height(48.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = AppText)
                        ) {
                            Text("Add To Plan", fontWeight = FontWeight.Bold,
                                color = AppAccentGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailChip(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppSecondaryText, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = AppText, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp))
    }
    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(AppBackground))
}

// ═══════════════════════════════════════════════════════════════════════════════
// GOAL CHIP
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun GoalChip(
    emoji: String, label: String,
    selected: Boolean, gradient: Brush,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (selected) Color.Transparent else AppBackground,
        animationSpec = tween(200),
        label         = "chip_bg_$label"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (selected) Modifier.background(gradient)
                else Modifier.background(bgColor)
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else Color(0xFFE5E5E5),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(),
                onClick           = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(emoji, fontSize = 22.sp)
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) Color.White else AppText
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// DIET CHIP
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun DietChip(
    emoji: String, label: String,
    selected: Boolean, onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue   = if (selected) AppText else AppBackground,
        animationSpec = tween(200), label = "diet_$label"
    )
    val textColor by animateColorAsState(
        targetValue   = if (selected) AppCardWhite else AppText,
        animationSpec = tween(200), label = "dietText_$label"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(),
                onClick           = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════
@Preview(showBackground = true)
@Composable
fun PersonalizedNutritionScreenPreview() {
    PersonalizedNutritionScreen()
}