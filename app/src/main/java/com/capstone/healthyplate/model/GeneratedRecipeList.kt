package com.capstone.healthyplate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeneratedRecipeList(
    val foodName: String,
    val foto: String,
    val bahan: String,
    val langkah: String
) : Parcelable {
    constructor() : this("","","","")
}