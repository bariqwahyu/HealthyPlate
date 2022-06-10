package com.capstone.healthyplate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe (
    val food_name: String,
    val foto: String,
    val bahan: String,
    val langkah: String
) : Parcelable {
    constructor() : this("","","","")
}