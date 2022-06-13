package com.capstone.healthyplate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeneratedRecipe(
    val foodName: String
) : Parcelable