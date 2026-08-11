package com.phishware.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phishware.android.data.model.UrlAnalysisResponse
import com.phishware.android.data.repository.PhishwareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrlAnalysisViewModel @Inject constructor(
    private val repository: PhishwareRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState

    fun analyzeUrl(url: String) {
        if (url.isBlank()) {
            _analysisState.value = AnalysisState.Error("Ingresa una URL válida")
            return
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisState.Loading
            repository.analyzeUrl(url.trim())
                .onSuccess { result ->
                    _analysisState.value = AnalysisState.Success(result)
                }
                .onFailure { e ->
                    _analysisState.value = AnalysisState.Error(
                        e.message ?: "Error al analizar la URL"
                    )
                }
        }
    }

    fun reset() {
        _analysisState.value = AnalysisState.Idle
    }

    sealed class AnalysisState {
        data object Idle    : AnalysisState()
        data object Loading : AnalysisState()
        data class  Success(val result: UrlAnalysisResponse) : AnalysisState()
        data class  Error(val message: String) : AnalysisState()
    }
}
