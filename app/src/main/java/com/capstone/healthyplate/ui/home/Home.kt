package com.capstone.healthyplate.ui.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Home (
    var ingredients : String ?=null
) : Parcelable