package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Converter
import com.example.model.LengthUnit
import com.example.model.MeasurementCategory
import com.example.model.MeasurementUnit
import com.example.model.TemperatureUnit
import com.example.model.VolumeUnit
import com.example.model.WeightUnit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConverterUiState(
    val category: MeasurementCategory = MeasurementCategory.LENGTH,
    val inputValue: String = "",
    val sourceUnit: MeasurementUnit = LengthUnit.CENTIMETER,
    val targetUnit: MeasurementUnit = LengthUnit.INCH,
    val resultText: String = "",
    val availableUnits: List<MeasurementUnit> = LengthUnit.values().toList()
)

class UnitConverterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun onCategorySelected(newCategory: MeasurementCategory) {
        val units = getUnitsForCategory(newCategory)
        _uiState.update { currentState ->
            currentState.copy(
                category = newCategory,
                availableUnits = units,
                sourceUnit = units[0],
                // Default target to index 1 if available, otherwise index 0
                targetUnit = if (units.size > 1) units[1] else units[0],
                resultText = "" // Reset result on category change
            )
        }
    }

    fun onInputValueChanged(newValue: String) {
        _uiState.update { it.copy(inputValue = newValue) }
    }

    fun onSourceUnitSelected(unit: MeasurementUnit) {
        _uiState.update { it.copy(sourceUnit = unit, resultText = "") }
    }

    fun onTargetUnitSelected(unit: MeasurementUnit) {
        _uiState.update { it.copy(targetUnit = unit, resultText = "") }
    }

    fun swapUnits() {
        _uiState.update { currentState ->
            currentState.copy(
                sourceUnit = currentState.targetUnit,
                targetUnit = currentState.sourceUnit,
                resultText = "" // Reset result to keep states consistent
            )
        }
    }

    fun performConversion() {
        val input = _uiState.value.inputValue.trim()
        
        if (input.isEmpty()) {
            viewModelScope.launch {
                _toastMessage.emit("Please enter a value to convert")
            }
            return
        }

        val numericValue = input.toDoubleOrNull()
        if (numericValue == null) {
            viewModelScope.launch {
                _toastMessage.emit("Invalid input: Please enter a valid number")
            }
            return
        }

        val state = _uiState.value
        val conversionResult = Converter.convert(
            value = numericValue,
            from = state.sourceUnit,
            to = state.targetUnit,
            category = state.category
        )

        // Format result: limit decimal places to 6 and drop trailing zeros
        val formattedResult = formatResult(conversionResult)
        
        _uiState.update {
            it.copy(
                resultText = "$input ${state.sourceUnit.symbol} = $formattedResult ${state.targetUnit.symbol}"
            )
        }
    }

    private fun getUnitsForCategory(category: MeasurementCategory): List<MeasurementUnit> {
        return when (category) {
            MeasurementCategory.LENGTH -> LengthUnit.values().toList()
            MeasurementCategory.WEIGHT -> WeightUnit.values().toList()
            MeasurementCategory.TEMPERATURE -> TemperatureUnit.values().toList()
            MeasurementCategory.VOLUME -> VolumeUnit.values().toList()
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isInfinite() || value.isNaN()) return "Error"
        
        // Convert to double format and trim trailing decimal zeros if it's a whole number
        val formatted = String.format("%.6f", value)
            .replace(",", ".") // ensure uniform locale decimal point
        
        return if (formatted.contains(".")) {
            var trimmed = formatted.dropLastWhile { it == '0' }
            if (trimmed.endsWith(".")) {
                trimmed = trimmed.dropLast(1)
            }
            trimmed
        } else {
            formatted
        }
    }
}
