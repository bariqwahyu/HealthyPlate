package com.capstone.healthyplate.response

import com.google.gson.annotations.SerializedName

data class GenerateResponse(

	@field:SerializedName("result")
	val result: List<String?>? = null
)
