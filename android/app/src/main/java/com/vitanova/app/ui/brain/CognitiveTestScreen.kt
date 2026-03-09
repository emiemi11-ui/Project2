package com.vitanova.app.ui.brain

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.VitaNovaApp
import com.vitanova.app.data.local.entity.BrainTest
import com.vitanova.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// ── ViewModel ───────────────────────────────────────────────────────

class CognitiveTestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = VitaNovaApp.getInstance().database

    data class TestResult(val score: Int, val details: String)

    private val _result = MutableStateFlow<TestResult?>(null)
    val result = _result.asStateFlow()

    fun saveResult(testType: String, score: Int, rawValue: Float, details: String) {
        _result.value = TestResult(score, details)
        viewModelScope.launch {
            db.brainDao().insertTest(
                BrainTest(
                    timestamp = System.currentTimeMillis(),
                    date = LocalDate.now().toString(),
                    testType = testType,
                    score = rawValue,
                    reactionTimeMs = if (testType == "reaction_time") rawValue.toInt() else null,
                    accuracyPercent = if (testType != "reaction_time") rawValue else null,
                    durationSeconds = 60
                )
            )
        }
    }

    fun clearResult() { _result.value = null }
}

// ── Main Screen ─────────────────────────────────────────────────────

@Composable
fun CognitiveTestScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CognitiveTestViewModel = viewModel()
) {
    var selectedTest by remember { mutableStateOf<String?>(null) }
    val result by viewModel.result.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedTest != null && result == null) {
                    selectedTest = null
                } else {
                    viewModel.clearResult()
                    onNavigateBack()
                }
            }) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = VitaTextPrimary)
            }
            Text(
                "Cognitive Tests",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary
            )
        }

        if (result != null) {
            // Result Screen
            ResultScreen(result = result!!, onDone = {
                viewModel.clearResult()
                selectedTest = null
            })
        } else if (selectedTest == null) {
            // Test Selection
            TestSelection(onSelect = { selectedTest = it })
        } else {
            when (selectedTest) {
                "reaction" -> ReactionTimeTest(onComplete = { ms ->
                    val score = when {
                        ms < 200 -> 100
                        ms < 250 -> 90
                        ms < 300 -> 80
                        ms < 350 -> 70
                        ms < 400 -> 60
                        ms < 500 -> 40
                        else -> 20
                    }
                    viewModel.saveResult("reaction_time", score, ms.toFloat(), "Average: ${ms}ms")
                })
                "memory" -> MemoryNBackTest(onComplete = { accuracy ->
                    viewModel.saveResult("memory", accuracy.toInt(), accuracy, "Accuracy: ${accuracy.toInt()}%")
                })
                "attention" -> StroopTest(onComplete = { accuracy, avgTime ->
                    viewModel.saveResult("attention", accuracy.toInt(), accuracy, "Accuracy: ${accuracy.toInt()}% • Avg: ${avgTime}ms")
                })
                "logic" -> PatternTest(onComplete = { score ->
                    viewModel.saveResult("stroop", score, score.toFloat(), "Score: $score/8")
                })
            }
        }
    }
}

// ── Test Selection ──────────────────────────────────────────────────

@Composable
private fun TestSelection(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TestCard("Reaction Time", "Tap when the screen turns green", BrainAccent, "reaction", onSelect)
        TestCard("Memory (N-Back)", "Remember and match patterns", SleepAccent, "memory", onSelect)
        TestCard("Attention (Stroop)", "Name the color, not the word", FocusAccent, "attention", onSelect)
        TestCard("Logic (Pattern)", "Find the missing piece", FitnessAccent, "logic", onSelect)
    }
}

@Composable
private fun TestCard(title: String, desc: String, color: Color, key: String, onSelect: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(key) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Psychology, "Test", tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = VitaTextPrimary)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = VitaTextTertiary)
            }
            Icon(Icons.Filled.ChevronRight, "Go", tint = VitaTextTertiary)
        }
    }
}

// ── Test 1: Reaction Time ───────────────────────────────────────────

@Composable
private fun ReactionTimeTest(onComplete: (Long) -> Unit) {
    var phase by remember { mutableStateOf("waiting") } // waiting, ready, go, tapped
    var startTime by remember { mutableLongStateOf(0L) }
    var results by remember { mutableStateOf(listOf<Long>()) }
    var trial by remember { mutableIntStateOf(0) }

    LaunchedEffect(phase) {
        if (phase == "waiting") {
            val delayMs = (2000..5000).random().toLong()
            delay(delayMs)
            startTime = System.nanoTime()
            phase = "go"
        }
    }

    val bgColor = when (phase) {
        "waiting" -> VitaError
        "go" -> VitaSuccess
        "tapped" -> VitaSurface
        else -> VitaSurface
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable {
                when (phase) {
                    "go" -> {
                        val reactionMs = (System.nanoTime() - startTime) / 1_000_000
                        results = results + reactionMs
                        trial++
                        phase = "tapped"
                    }
                    "waiting" -> {} // too early, ignore
                    "tapped" -> {
                        if (trial >= 5) {
                            onComplete(results.average().toLong())
                        } else {
                            phase = "waiting"
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (phase) {
            "waiting" -> {
                Text("Wait for green...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            "go" -> {
                Text("TAP NOW!", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            "tapped" -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${results.last()} ms", color = VitaGreen, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("Trial $trial/5", color = VitaTextSecondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        if (trial >= 5) "Tap to see results" else "Tap to continue",
                        color = VitaTextTertiary, fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ── Test 2: Memory N-Back ───────────────────────────────────────────

@Composable
private fun MemoryNBackTest(onComplete: (Float) -> Unit) {
    var stimulusIndex by remember { mutableIntStateOf(0) }
    var currentDigit by remember { mutableIntStateOf(0) }
    val sequence = remember { List(20) { (1..9).random() }.toMutableList().apply {
        // Ensure some matches at n-2
        for (i in 4 until size step 3) { this[i] = this[i - 2] }
    }}
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    var falseAlarms by remember { mutableIntStateOf(0) }
    var responded by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        for (i in sequence.indices) {
            stimulusIndex = i
            currentDigit = sequence[i]
            responded = false
            delay(1500)
            if (!responded && i >= 2 && sequence[i] == sequence[i - 2]) {
                misses++
            }
        }
        val totalTargets = (2 until sequence.size).count { sequence[it] == sequence[it - 2] }
        val accuracy = if (totalTargets + falseAlarms > 0) {
            (hits.toFloat() / (totalTargets + falseAlarms).coerceAtLeast(1)) * 100
        } else 100f
        onComplete(accuracy.coerceIn(0f, 100f))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
            .clickable {
                if (!responded && stimulusIndex >= 2) {
                    responded = true
                    if (sequence[stimulusIndex] == sequence[stimulusIndex - 2]) {
                        hits++
                    } else {
                        falseAlarms++
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("2-Back Test", color = VitaTextTertiary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap when current = 2 back", color = VitaTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "$currentDigit",
                color = VitaTextPrimary,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = { (stimulusIndex + 1f) / sequence.size },
                modifier = Modifier.width(200.dp),
                color = BrainAccent,
                trackColor = VitaSurfaceVariant
            )
            Text(
                "${stimulusIndex + 1}/${sequence.size}",
                color = VitaTextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// ── Test 3: Stroop ──────────────────────────────────────────────────

@Composable
private fun StroopTest(onComplete: (Float, Long) -> Unit) {
    val colorWords = listOf("RED", "GREEN", "BLUE", "YELLOW")
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow)
    val colorNames = listOf("Red", "Green", "Blue", "Yellow")

    var trial by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    var totalTime by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val stimuli = remember {
        List(15) {
            val wordIdx = (0..3).random()
            val colorIdx = (0..3).random()
            Pair(wordIdx, colorIdx) // word index, color index
        }
    }

    if (trial >= 15) {
        val accuracy = (correct.toFloat() / 15) * 100
        val avgTime = totalTime / 15
        LaunchedEffect(Unit) { onComplete(accuracy, avgTime) }
        return
    }

    val (wordIdx, colorIdx) = stimuli[trial]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("What COLOR is the text?", color = VitaTextTertiary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = colorWords[wordIdx],
                color = colors[colorIdx],
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colorNames.forEachIndexed { i, name ->
                    Button(
                        onClick = {
                            val elapsed = System.currentTimeMillis() - startTime
                            totalTime += elapsed
                            if (i == colorIdx) correct++
                            trial++
                            startTime = System.currentTimeMillis()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors[i].copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(name, color = colors[i], fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("${trial + 1}/15", color = VitaTextTertiary, fontSize = 12.sp)
        }
    }
}

// ── Test 4: Pattern ─────────────────────────────────────────────────

@Composable
private fun PatternTest(onComplete: (Int) -> Unit) {
    var trial by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    val puzzles = remember {
        List(8) {
            val base = (it + 1) * 3
            val grid = List(9) { i -> base + i * (it + 2) }
            val missingIdx = (0..8).random()
            val correctAnswer = grid[missingIdx]
            val options = listOf(
                correctAnswer,
                correctAnswer + (1..5).random(),
                correctAnswer - (1..5).random(),
                correctAnswer + (6..12).random()
            ).shuffled()
            Triple(grid.toMutableList().apply { this[missingIdx] = -1 }, correctAnswer, options)
        }
    }

    if (trial >= 8) {
        LaunchedEffect(Unit) { onComplete(score) }
        return
    }

    val (grid, answer, options) = puzzles[trial]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Find the missing number", color = VitaTextTertiary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // 3x3 Grid
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0..2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (col in 0..2) {
                            val idx = row * 3 + col
                            val value = grid[idx]
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (value == -1) VitaGreen.copy(alpha = 0.2f) else VitaSurfaceCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (value == -1) "?" else "$value",
                                    color = if (value == -1) VitaGreen else VitaTextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Options
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { opt ->
                    OutlinedButton(
                        onClick = {
                            if (opt == answer) score++
                            trial++
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("$opt", fontSize = 18.sp, color = VitaTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("${trial + 1}/8", color = VitaTextTertiary, fontSize = 12.sp)
        }
    }
}

// ── Result Screen ───────────────────────────────────────────────────

@Composable
private fun ResultScreen(result: CognitiveTestViewModel.TestResult, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = VitaGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Test Complete!", color = VitaTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Score: ${result.score}",
                color = VitaGreen,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(result.details, color = VitaTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VitaGreen)
            ) {
                Text("Done", color = VitaBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}
