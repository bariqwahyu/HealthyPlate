package com.capstone.healthyplate.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Users(
    var imageUri: String?,
    var name: String?,
    var email: String?,
    var age: String?,
    var gender: String?,
    var job: String?
) : Parcelable