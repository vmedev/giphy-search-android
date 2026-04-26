package com.example.gifsearch_test.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gifsearch_test.data.model.GifData
import com.example.gifsearch_test.data.repository.GifRepository
import com.example.gifsearch_test.util.NetworkUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val gifs: List<GifData>) : UiState()
    data class Error(val message: String) : UiState()
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    application: Application,
    private val repository: GifRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    private var currentOffset = 0
    private var currentQuery = ""
    private var isLoadingMore = false
    private var endReached = false
    private val pageSize = 20

    private var isOnline = true

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collect { q ->
                    currentQuery = q
                    currentOffset = 0
                    endReached = false
                    loadFirstPage()
                }
        }

        viewModelScope.launch {
            var firstEmission = true   // ← добавить
            NetworkUtils.observeNetwork(getApplication()).collect { online ->
                isOnline = online
                if (firstEmission) {
                    firstEmission = false
                    return@collect      // первое значение игнорим — init сам грузанёт
                }
                if (!online) {
                    _state.value = UiState.Error("No internet connection")
                } else if (_state.value is UiState.Error) {
                    loadFirstPage()
                }
            }
        }
    }

    fun onQueryChanged(query: String) {
        queryFlow.value = query.trim()
    }

    fun retry() {
        loadFirstPage()
    }

    private fun loadFirstPage() {
        if (!isOnline) {
            _state.value = UiState.Error("No internet connection")
            return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val response = if (currentQuery.isBlank())
                    repository.getTrendingGifs(0)
                else
                    repository.searchGifs(currentQuery, 0)

                currentOffset = response.data.size
                if (response.data.size < pageSize) endReached = true
                _state.value = UiState.Success(response.data)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadNextPage() {
        if (isLoadingMore || endReached || !isOnline) return
        val current = (_state.value as? UiState.Success)?.gifs ?: return
        isLoadingMore = true
        viewModelScope.launch {
            try {
                val response = if (currentQuery.isBlank())
                    repository.getTrendingGifs(currentOffset)
                else
                    repository.searchGifs(currentQuery, currentOffset)

                if (response.data.size < pageSize) endReached = true
                currentOffset += response.data.size
                _state.value = UiState.Success(current + response.data)
            } catch (_: Exception) {
                // тихо игнорим ошибку догрузки страницы — список уже отрисован
            } finally {
                isLoadingMore = false
            }
        }
    }
}