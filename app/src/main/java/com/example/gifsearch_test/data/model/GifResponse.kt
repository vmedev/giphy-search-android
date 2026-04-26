package com.example.gifsearch_test.data.model

import com.google.gson.annotations.SerializedName

data class GifData(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("images") val images: GifImages
)

data class GifImage(
    @SerializedName("url") val url: String
)

data class GifImages(
    @SerializedName("fixed_width") val fixedWidth: GifImage,
    @SerializedName("fixed_width_small") val fixedWidthSmall: GifImage,
    @SerializedName("original") val original: GifImage
)

data class GifResponse(
    @SerializedName("data") val data: List<GifData>
)