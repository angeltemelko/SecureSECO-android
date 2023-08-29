package com.secureseco.trustsecoandroid.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrustScoreViewModel : ViewModel() {

    // Private mutable state flow
    private val _trustScore = MutableStateFlow<String?>(null)
    // Public read-only state flow
    val trustScore = _trustScore.asStateFlow()

    fun fetchTrustScoreForApp(appPackageName: String) {
        // For demonstration purposes, I'm setting mock scores based on package names.
        when (appPackageName) {
            "com.example.app1" -> _trustScore.value = "85/100"
            "com.example.app2" -> _trustScore.value = "72/100"
            else -> _trustScore.value = "Unknown trust score for $appPackageName"
        }
    }
}