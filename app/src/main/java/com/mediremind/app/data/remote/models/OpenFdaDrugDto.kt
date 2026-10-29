package com.mediremind.app.data.remote.models

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("results") val results: List<OpenFdaDrugDto>? = null,
    @SerializedName("error") val error: OpenFdaError? = null
)

data class OpenFdaError(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?
)

data class OpenFdaDrugDto(
    @SerializedName("product_ndc") val productNdc: String? = null,
    @SerializedName("generic_name") val genericName: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    @SerializedName("brand_name_base") val brandNameBase: String? = null,
    @SerializedName("dosage_form") val dosageForm: String? = null,
    @SerializedName("route") val route: List<String>? = null,
    @SerializedName("substance_name") val substanceName: List<String>? = null,
    @SerializedName("active_ingredients") val activeIngredients: List<ActiveIngredientDto>? = null,
    @SerializedName("pharm_class") val pharmClass: List<String>? = null,
    @SerializedName("marketing_category") val marketingCategory: String? = null
)

data class ActiveIngredientDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("strength") val strength: String? = null
)
