package com.lkovari.mobile.apps.treecalc.viewmodel

import androidx.lifecycle.ViewModel
import com.lkovari.mobile.apps.treecalc.engine.CalculatorEngine
import com.lkovari.mobile.apps.treecalc.engine.CalculatorKey
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel : ViewModel() {
    private val engine = CalculatorEngine()
    private val mutableState = MutableStateFlow(engine.snapshot())
    val state: StateFlow<EvaluationResult> = mutableState.asStateFlow()

    fun press(key: CalculatorKey) {
        engine.press(key)
        mutableState.value = engine.snapshot()
    }

    fun setBase(base: NumericBase) {
        engine.setBase(base)
        mutableState.value = engine.snapshot()
    }
}
