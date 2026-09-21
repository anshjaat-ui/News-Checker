package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FactCheckResult
import com.example.data.model.FactCheckVerdict
import com.example.data.repository.FactCheckRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream

data class FactCheckUiState(
    val inputText: String = "",
    val selectedImageUri: Uri? = null,
    val selectedBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val loadingStep: String = "",
    val currentResult: FactCheckResult? = null,
    val activeTab: Int = 0, // 0 = Verify, 1 = History, 2 = About
    val selectedHistoryFilter: FactCheckVerdict? = null,
    val errorMessage: String? = null,
    val inspectingHistoryItem: FactCheckResult? = null
)

class FactCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FactCheckRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FactCheckRepository(database.factCheckDao())
    }

    val history: StateFlow<List<FactCheckResult>> = repository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(FactCheckUiState())
    val uiState: StateFlow<FactCheckUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                _uiState.update {
                    it.copy(
                        selectedImageUri = uri,
                        selectedBitmap = bitmap,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Image load nahi ho saki: ${e.message}") }
            }
        }
    }

    fun onClearImage() {
        _uiState.update { it.copy(selectedImageUri = null, selectedBitmap = null) }
    }

    fun onClearInput() {
        _uiState.update { it.copy(inputText = "", selectedImageUri = null, selectedBitmap = null, errorMessage = null) }
    }

    fun onSampleSelected(sample: SampleClaim) {
        _uiState.update {
            it.copy(
                inputText = sample.text,
                selectedImageUri = null,
                selectedBitmap = null,
                errorMessage = null
            )
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(activeTab = tab, inspectingHistoryItem = null) }
    }

    fun onFilterSelected(verdict: FactCheckVerdict?) {
        _uiState.update { it.copy(selectedHistoryFilter = verdict) }
    }

    fun inspectHistoryItem(item: FactCheckResult?) {
        _uiState.update { it.copy(inspectingHistoryItem = item) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun verifyClaim() {
        val state = _uiState.value
        val query = state.inputText.trim()
        val bitmap = state.selectedBitmap

        if (query.isBlank() && bitmap == null) {
            _uiState.update { it.copy(errorMessage = "Kripya koi text forward, claim ya photo provide karein.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingStep = "Claim nikaala ja raha hai...",
                    errorMessage = null
                )
            }

            try {
                delay(400)
                _uiState.update { it.copy(loadingStep = "PIB, Alt News aur verified databases check ho rahe hain...") }
                delay(400)
                _uiState.update { it.copy(loadingStep = "Factual evidence ke against verdict analyze ho raha hai...") }

                val result = repository.verifyAndSave(
                    query = query,
                    bitmap = bitmap,
                    imageUri = state.selectedImageUri?.toString()
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingStep = "",
                        currentResult = result
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingStep = "",
                        errorMessage = "Fact check mein takleef aayi: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteCheck(id)
            if (_uiState.value.inspectingHistoryItem?.id == id) {
                _uiState.update { it.copy(inspectingHistoryItem = null) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(inspectingHistoryItem = null) }
        }
    }
}
