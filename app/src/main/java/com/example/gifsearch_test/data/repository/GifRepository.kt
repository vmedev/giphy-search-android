package com.example.gifsearch_test.data.repository

import com.example.gifsearch_test.data.model.GifResponse
import com.example.gifsearch_test.data.network.RetrofitClient

private const val API_KEY = "QfrbFyrrBeUX0yxJyQXKc5QmBqH2CGKS"
private const val PAGE_SIZE = 20

class GifRepository {

    suspend fun searchGifs(query: String, offset: Int): GifResponse {
        return RetrofitClient.apiService.searchGifs(
            apiKey = API_KEY,
            query = query,
            limit = PAGE_SIZE,
            offset = offset
        )
    }

    suspend fun getTrendingGifs(offset: Int): GifResponse {
        return RetrofitClient.apiService.getTrendingGifs(
            apiKey = API_KEY,
            limit = PAGE_SIZE,
            offset = offset
        )
    }
}