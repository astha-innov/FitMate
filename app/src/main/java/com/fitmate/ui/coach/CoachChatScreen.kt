package com.fitmate.ui.coach

import androidx.compose.animation.AnimatedVisibility
import com.fitmate.BuildConfig
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitmate.ui.coach.viewmodel.CoachViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val BackgroundColor = Color(0xFF0B0D12)
val CardColor = Color(0xFF151922)
val GlassColor = Color(0xFF1A1F2B).copy(alpha = 0.6f)
val BorderColor = Color.White.copy(alpha = 0.08f)
val PrimaryTextColor = Color(0xFFF5F7FA)
val SecondaryTextColor = Color(0xFF8B95A5)
val AccentGreen = Color(0xFF1DE9B6)
val AccentBlue = Color(0xFF4FC3F7)
val AccentOrange = Color(0xFFFFB74D)
val AccentPurple = Color(0xFFB388FF)
val AccentRed = Color(0xFFFF6B6B)
val NeonCyan = Color(0xFF00E5FF)
val DeepSpace = Color(0xFF05070A)
val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
val SurfaceBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)

enum class MessageRole { USER, AI }

data class CoachMessage(
    val id: String,
    val text: String,
    val role: MessageRole,
    val timestamp: String
)

data class SuggestedPrompt(
    val id: String,
    val text: String,
    val icon: ImageVector,
    val color: Color
)

data class Metric(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CoachChatScreen() {
    val coachViewModel: CoachViewModel = viewModel()
    var inputText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<CoachMessage>>(emptyList()) }
    val listState = rememberLazyListState()


    val currentTime = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    LaunchedEffect(coachViewModel.loading) {

        if (coachViewModel.loading) {

            if (messages.none { it.id == "loading" }) {
                messages = messages + CoachMessage(
                    id = "loading",
                    text = "Thinking...",
                    role = MessageRole.AI,
                    timestamp = currentTime
                )
            }

        } else {

            messages = messages.filterNot {
                it.id == "loading"
            }
        }
    }

    LaunchedEffect(coachViewModel.response) {

        if (coachViewModel.response.isNotBlank()) {

            messages = messages.filterNot {
                it.id == "loading"
            }

            messages = messages + CoachMessage(
                id = System.currentTimeMillis().toString(),
                text = coachViewModel.response,
                role = MessageRole.AI,
                timestamp = currentTime
            )
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            MessageInputBar(
                inputText = inputText,
                onValueChange = { inputText = it },
                onSend = {

                    if (inputText.isNotBlank()) {

                        val question = inputText

                        messages = messages + CoachMessage(
                            id = System.currentTimeMillis().toString(),
                            text = question,
                            role = MessageRole.USER,
                            timestamp = currentTime
                        )

                        inputText = ""

                        coachViewModel.askCoach(
                            apiKey = BuildConfig.GEMINI_API_KEY,
                            prompt = question
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { PremiumHeroHeader() }

            if (messages.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(24.dp))
                        DailyBriefingCard()
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Quick Actions",
                            color = PrimaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                    }
                }

                item { QuickActionsRow() }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "AI Metrics Dashboard",
                            color = PrimaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                    }
                }

                item { MetricsDashboard() }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Suggested Prompts",
                            color = PrimaryTextColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                        )
                        SuggestedPromptsSection(onPromptSelected = { promptText ->
                            inputText = promptText
                        })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            items(messages, key = { it.id }) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ChatBubble(message = message)
                }
            }
        }
    }


}

@Composable
fun PremiumHeroHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepSpace,
                        BackgroundColor
                    )
                )
            )
            .padding(top = 48.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(AccentPurple, AccentBlue, AccentPurple)
                            ),
                            radius = size.width / 1.5f,
                            alpha = 0.15f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .rotate(rotation)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(AccentPurple, NeonCyan, AccentGreen, AccentPurple)
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .background(CardColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "AI Coach",
                        tint = AccentBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp, end = 6.dp)
                        .size(14.dp)
                        .background(CardColor, CircleShape)
                        .padding(2.dp)
                        .background(AccentGreen, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FitMate Coach",
                color = PrimaryTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your personal fitness intelligence",
                color = AccentPurple.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }


}

@Composable
fun DailyBriefingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        CardColor,
                        GlassColor
                    )
                )
            )
            .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Good Evening Anurag",
                color = PrimaryTextColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Today's Summary",
                color = SecondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BriefingStat(value = "18", label = "Day Streak", color = AccentOrange)
                BriefingStat(value = "84%", label = "Consistency", color = AccentBlue)
                BriefingStat(value = "5", label = "Workouts", color = AccentGreen)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassWhite)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Insight",
                        tint = AccentOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Insight",
                            color = AccentOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You are ahead of last week's pace. Focus on recovery tonight.",
                            color = PrimaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }


}

@Composable
fun BriefingStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = SecondaryTextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickActionsRow() {
    val actions = listOf(
        "Analyze Progress" to Icons.Rounded.CheckCircle,
        "Weekly Plan" to Icons.Rounded.DateRange,
        "Recovery Advice" to Icons.Rounded.Favorite,
        "Nutrition Tips" to Icons.Rounded.List
    )


    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(actions) { (text, icon) ->
            PremiumActionChip(text = text, icon = icon)
        }
    }



}

@Composable
fun PremiumActionChip(text: String, icon: ImageVector) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()


    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "chip_scale")
    val bgColor by animateColorAsState(targetValue = if (isPressed) CardColor else GlassColor, label = "chip_color")

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null) {}
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryTextColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = PrimaryTextColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }


}

@Composable
fun MetricsDashboard() {
    val metrics = listOf(
        Metric("Streak", "18 Days", Icons.Rounded.PlayArrow, AccentOrange),
        Metric("Fitness Score", "92", Icons.Rounded.Star, AccentPurple),
        Metric("Consistency", "84%", Icons.Rounded.ThumbUp, NeonCyan),
        Metric("Recovery", "Optimal", Icons.Rounded.Favorite, AccentGreen)
    )


    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(metrics) { metric ->
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardColor)
                    .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(metric.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = metric.icon,
                            contentDescription = null,
                            tint = metric.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = metric.value,
                        color = PrimaryTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = metric.label,
                        color = SecondaryTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }



}

@Composable
fun SuggestedPromptsSection(onPromptSelected: (String) -> Unit) {
    val prompts = listOf(
        SuggestedPrompt("1", "How am I progressing this month?", Icons.Rounded.ThumbUp, NeonCyan),
        SuggestedPrompt("2", "What should I improve in my routine?", Icons.Rounded.Star, AccentPurple),
        SuggestedPrompt("3", "Build next week's workout plan", Icons.Rounded.DateRange, AccentGreen),
        SuggestedPrompt("4", "Analyze my consistency trends", Icons.Rounded.List, AccentOrange)
    )


    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        prompts.forEach { prompt ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "prompt_scale")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardColor)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .clickable(interactionSource = interactionSource, indication = null) {
                        onPromptSelected(prompt.text)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(prompt.color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = prompt.icon,
                        contentDescription = null,
                        tint = prompt.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = prompt.text,
                    color = PrimaryTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }


}

@Composable
fun ChatBubble(message: CoachMessage) {
    val isUser = message.role == MessageRole.USER


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(AccentPurple, NeonCyan, AccentGreen, AccentPurple)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = BackgroundColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = if (isUser) 24.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 24.dp
                        )
                    )
                    .background(
                        if (isUser) Brush.linearGradient(listOf(Color(0xFF6C63FF), AccentPurple))
                        else SolidColor(GlassColor)
                    )
                    .then(
                        if (!isUser) Modifier.border(
                            1.dp,
                            SurfaceBorder,
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 24.dp
                            )
                        ) else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = PrimaryTextColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message.timestamp,
                color = SecondaryTextColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }



}

@Composable
fun MessageInputBar(
    inputText: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = BackgroundColor.copy(alpha = 0.8f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(GlassColor)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Attach",
                    tint = SecondaryTextColor
                )
            }

            BasicTextField(
                value = inputText,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textStyle = TextStyle(
                    color = PrimaryTextColor,
                    fontSize = 15.sp
                ),
                decorationBox = { innerTextField ->
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Ask FitMate Coach...",
                            color = SecondaryTextColor,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )

            AnimatedVisibility(
                visible = inputText.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPurple)
                        .clickable { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = PrimaryTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = inputText.isBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "AI Features",
                        tint = NeonCyan
                    )
                }
            }
        }
    }



}