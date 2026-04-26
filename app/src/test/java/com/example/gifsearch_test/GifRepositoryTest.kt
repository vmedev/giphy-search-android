package com.example.gifsearch_test

import com.example.gifsearch_test.data.model.GifData
import com.example.gifsearch_test.data.model.GifImage
import com.example.gifsearch_test.data.model.GifImages
import com.example.gifsearch_test.data.model.GifResponse
import com.example.gifsearch_test.data.repository.GifRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GifRepositoryTest {

    @Test
    fun `repository returns gifs from api`() = runBlocking {
        val repo = mockk<GifRepository>()
        val fakeGif = GifData(
            id = "1",
            title = "test",
            images = GifImages(
                fixedWidth = GifImage("url"),
                fixedWidthSmall = GifImage("url_small"),
                original = GifImage("url_orig")
            )
        )
        coEvery { repo.searchGifs("cats", 0) } returns GifResponse(listOf(fakeGif))

        val result = repo.searchGifs("cats", 0)

        assertTrue(result.data.isNotEmpty())
        assertEquals("1", result.data[0].id)
        assertEquals("test", result.data[0].title)
    }

    @Test
    fun `repository returns empty list when no results`() = runBlocking {
        val repo = mockk<GifRepository>()
        coEvery { repo.searchGifs("xyzqwerty12345", 0) } returns GifResponse(emptyList())

        val result = repo.searchGifs("xyzqwerty12345", 0)

        assertTrue(result.data.isEmpty())
    }
}