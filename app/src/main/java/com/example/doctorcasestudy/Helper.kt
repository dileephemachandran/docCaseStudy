package com.example.doctorcasestudy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class TextFieldState {
    var textInput: String by mutableStateOf("")
}