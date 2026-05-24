package com.lottolab.probability.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lottolab.probability.data.LottoRepository
import com.lottolab.probability.domain.DailyCombination
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.SavedNumberSet
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LottoUiState(
    val draws: List<DrawResult> = emptyList(),
    val numberSets: List<SavedNumberSet> = emptyList(),
    val selectedRound: Int? = null,
    val isSyncing: Boolean = true,
    val historyReady: Boolean = false,
    val syncMessage: String = "공식 당첨번호를 불러오는 중입니다.",
    val dailyCombinations: List<DailyCombination> = emptyList(),
    val actionMessage: String? = null,
) {
    val selectedDraw: DrawResult?
        get() = draws.firstOrNull { it.round == selectedRound } ?: draws.firstOrNull()
}

private data class DrawSyncUiState(
    val isSyncing: Boolean = true,
    val historyReady: Boolean = false,
    val message: String = "공식 당첨번호를 불러오는 중입니다.",
)

private data class UiAuxState(
    val selectedRound: Int?,
    val syncState: DrawSyncUiState,
    val actionMessage: String?,
)

class LottoViewModel(private val repository: LottoRepository) : ViewModel() {
    private val selectedRound = MutableStateFlow<Int?>(null)
    private val drawSyncState = MutableStateFlow(DrawSyncUiState())
    private val actionMessage = MutableStateFlow<String?>(null)
    private val todayDate = LocalDate.now(ZoneId.of("Asia/Seoul")).toString()
    private var syncJob: Job? = null
    private val auxiliaryState = combine(
        selectedRound,
        drawSyncState,
        actionMessage,
    ) { selectedRound, syncState, actionMessage ->
        UiAuxState(selectedRound, syncState, actionMessage)
    }

    val uiState: StateFlow<LottoUiState> = combine(
        repository.observeDraws(),
        repository.observeNumberSets(),
        repository.observeDailyCombinations(todayDate),
        auxiliaryState,
    ) { draws, numberSets, dailyCombinations, auxState ->
        LottoUiState(
            draws = draws,
            numberSets = numberSets,
            selectedRound = auxState.selectedRound,
            isSyncing = auxState.syncState.isSyncing,
            historyReady = auxState.syncState.historyReady,
            syncMessage = auxState.syncState.message,
            dailyCombinations = dailyCombinations,
            actionMessage = auxState.actionMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LottoUiState(),
    )

    init {
        refreshDraws()
    }

    fun selectRound(round: Int) {
        selectedRound.value = round
    }

    fun refreshDraws() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            drawSyncState.value = drawSyncState.value.copy(
                isSyncing = true,
                message = "공식 당첨번호를 확인하는 중입니다.",
            )

            runCatching {
                repository.syncOfficialDrawHistory { progress ->
                    drawSyncState.value = DrawSyncUiState(
                        isSyncing = !progress.historyReady,
                        historyReady = progress.historyReady,
                        message = if (progress.historyReady) {
                            "역대 회차 기록 ${progress.cachedDrawCount}개를 준비했습니다."
                        } else {
                            "역대 회차 ${progress.cachedDrawCount}개를 캐시하는 중입니다."
                        },
                    )
                    repository.ensureDailyCombinations(todayDate, repository.getCachedDraws())
                }
            }.onFailure { error ->
                drawSyncState.value = drawSyncState.value.copy(
                    isSyncing = false,
                    message = "갱신하지 못했습니다. 캐시된 결과를 표시합니다. ${error.message.orEmpty()}",
                )
                repository.ensureDailyCombinations(todayDate, repository.getCachedDraws())
            }

            drawSyncState.value = drawSyncState.value.copy(isSyncing = false)
        }
    }

    fun saveNumberSet(
        existing: SavedNumberSet?,
        name: String,
        numbers: Collection<Int>,
        favorite: Boolean = existing?.favorite ?: false,
        collectionName: String = existing?.collectionName ?: "기본",
    ) {
        viewModelScope.launch {
            runCatching {
                repository.saveNumberSet(existing, name, numbers, favorite, collectionName)
            }.onSuccess {
                actionMessage.value = "번호 세트를 저장했습니다."
            }.onFailure { error ->
                actionMessage.value = error.message ?: "번호 세트를 저장하지 못했습니다."
            }
        }
    }

    fun toggleFavorite(numberSet: SavedNumberSet) {
        viewModelScope.launch {
            runCatching { repository.toggleFavorite(numberSet) }
                .onSuccess { actionMessage.value = if (numberSet.favorite) "즐겨찾기를 해제했습니다." else "즐겨찾기에 고정했습니다." }
                .onFailure { error -> actionMessage.value = error.message ?: "즐겨찾기를 변경하지 못했습니다." }
        }
    }

    fun deleteNumberSet(numberSet: SavedNumberSet) {
        viewModelScope.launch {
            repository.deleteNumberSet(numberSet)
            actionMessage.value = "번호 세트를 삭제했습니다."
        }
    }

    fun clearActionMessage() {
        actionMessage.value = null
    }

    class Factory(private val repository: LottoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LottoViewModel::class.java)) {
                return LottoViewModel(repository) as T
            }
            error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
