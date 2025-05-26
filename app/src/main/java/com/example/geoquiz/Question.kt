package com.example.geoquiz

import androidx.annotation.IdRes
import androidx.annotation.StringRes

data class Question(@StringRes val textResId: Int, val answer:Boolean, var usedCheat:Boolean=false)
