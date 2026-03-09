package com.vitanova.app.ui.brain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.VitaNovaApp
import com.vitanova.app.data.local.entity.BrainTest
import com.vitanova.app.data.repository.BrainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TestType(val key: String, val displayName: String) {
    REACTION("reaction_time", "Timp Reactie"),
    MEMORY("memory", "Memorie"),
    ATTENTION("attention", "Atentie"),
    LOGIC("stroop", "Logica")
}

enum class TestState {
    IDLE,
    RUNNING,
    FINISHED
}

data class BrainUiState(
    val globalScore: Int = 0,
    val reactionScore: Int = 0,
    val memoryScore: Int = 0,
    val attentionScore: Int = 0,
    val logicScore: Int = 0,
    val currentTest: TestType? = null,
    val testState: TestState = TestState.IDLE,
    val lastTestDate: String? = null,
    val reactionHistory: List<Float> = emptyList(),
    val memoryHistory: List<Float> = emptyList(),
    val attentionHistory: List<Float> = emptyList(),
    val logicHistory: List<Float> = emptyList(),
    val globalHistory: List<Float> = emptyList(),
    val isLoading: Boolean = false
)

class BrainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VitaNovaApp.getInstance().database
    private val brainRepository = BrainRepository(database.brainDao())
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(BrainUiState())
    val uiState: StateFlow<BrainUiState> = _uiState.asStateFlow()

    init {
        loadScores()
        loadHistory()
    }

    private fun loadScores() {
        viewModelScope.launch {
            brainRepository.getLatestScores().collect { scores ->
                val reaction = scores["reaction_time"]?.toInt() ?: 0
                val memory = scores["memory"]?.toInt() ?: 0
                val attention = scores["attention"]?.toInt() ?: 0
                val logic = scores["stroop"]?.toInt() ?: 0

                val validScores = listOf(reaction, memory, attention, logic).filter { it > 0 }
                val global = if (validScores.isNotEmpty()) validScores.average().toInt() else 0

                _uiState.update {
                    it.copy(
                        globalScore = global,
                        reactionScore = reaction,
                        memoryScore = memory,
                        attentionScore = attention,
                        logicScore = logic
                    )
                }
            }
        }

        viewModelScope.launch {
            database.brainDao().getLatestByType("reaction_time").collect { test ->
                if (test != null) {
                    _uiState.update { it.copy(lastTestDate = test.date) }
                }
            }
        }
    }

    private fun loadHistory() {
        TestType.entries.forEach { type ->
            viewModelScope.launch {
                brainRepository.getScoreHistory(type.key, 30).collect { tests ->
                    val scores = tests.sortedBy { it.timestamp }.map { it.score }
                    _uiState.update { state ->
                        when (type) {
                            TestType.REACTION -> state.copy(reactionHistory = scores)
                            TestType.MEMORY -> state.copy(memoryHistory = scores)
                            TestType.ATTENTION -> state.copy(attentionHistory = scores)
                            TestType.LOGIC -> state.copy(logicHistory = scores)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            val startDate = LocalDate.now().minusDays(30).format(dateFormatter)
            database.brainDao().getGlobalScore().collect { score ->
                // Build global history from all types combined
                val allHistories = listOf(
                    _uiState.value.reactionHistory,
                    _uiState.value.memoryHistory,
                    _uiState.value.attentionHistory,
                    _uiState.value.logicHistory
                ).filter { it.isNotEmpty() }

                if (allHistories.isNotEmpty()) {
                    val maxLen = allHistories.maxOf { it.size }
                    val globalHist = (0 until maxLen).map { i ->
                        allHistories.mapNotNull { hist ->
                            hist.getOrNull(i)
                        }.average().toFloat()
                    }
                    _uiState.update { it.copy(globalHistory = globalHist) }
                }
            }
        }
    }

    fun startTest(type: TestType) {
        _uiState.update {
            it.copy(
                currentTest = type,
                testState = TestState.RUNNING
            )
        }
    }

    fun submitAnswer(score: Float, rawValue: Float = 0f, durationSeconds: Int = 0) {
        val currentTest = _uiState.value.currentTest ?: return

        viewModelScope.launch {
            val now = Instant.now().toEpochMilli()
            val today = LocalDate.now().format(dateFormatter)

            val test = BrainTest(
                timestamp = now,
                date = today,
                testType = currentTest.key,
                score = score,
                reactionTimeMs = if (currentTest == TestType.REACTION) rawValue.toInt() else null,
                accuracyPercent = if (currentTest != TestType.REACTION) rawValue else null,
                durationSeconds = durationSeconds
            )

            brainRepository.saveTestResult(test)

            _uiState.update {
                it.copy(
                    testState = TestState.FINISHED,
                    lastTestDate = today
                )
            }
        }
    }

    fun getTestResults() {
        _uiState.update {
            it.copy(
                currentTest = null,
                testState = TestState.IDLE
            )
        }
        loadScores()
        loadHistory()
    }
}
