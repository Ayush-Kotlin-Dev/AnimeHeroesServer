package com.example

import com.example.Repository.HeroRepository
import com.example.Repository.HeroRepositoryImpl
import com.example.Repository.NEXT_PAGE_KEY
import com.example.Repository.PREVIOUS_PAGE_KEY
import com.example.models.ApiResponse
import com.example.plugins.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import kotlin.test.*

class ApplicationTest {

    private val heroRepository: HeroRepository by inject(HeroRepository::class.java)

    @Test
    fun `access root endpoint , assert correct information`() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(
                    HttpStatusCode.OK,
                    response.status()
                )
                assertEquals(
                    "Welcome to Boruto API",
                    response.content
                )
            }
        }
    }


    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`() {
        withTestApplication(moduleFunction = Application::module){
            val pages = 1..5
            val heroes = listOf(
                heroRepository.page1,
                heroRepository.page2,
                heroRepository.page3,
                heroRepository.page4,
                heroRepository.page5
            )
            pages.forEach { page ->
                handleRequest(HttpMethod.Get, "/boruto/heroes?page=$page").apply {
                    assertEquals(
                        HttpStatusCode.OK,
                        response.status()
                    )
                    val expected = ApiResponse(
                        success = true,
                        message = "Heroes fetched successfully",
                        prevPage = calculatePage(page = page)["prevPage"],
                        nextPage = calculatePage(page = page)["nextPage"],
                        heroes = heroes[page - 1],
                    )
                    val actual = Json.decodeFromString<ApiResponse>(response.content!!)

                    assertEquals(
                        expected = expected,
                        actual = actual
                    )
                }
            }
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query non existing page number, assert error`() {
        withTestApplication(moduleFunction = Application::module){
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=6").apply {
                assertEquals(
                    HttpStatusCode.NotFound,
                    response.status()
                )
                val expected = ApiResponse(
                    success = false,
                    message = "Heroes not Found."
                )
                val actual = Json.decodeFromString<ApiResponse>(response.content!!)
                assertEquals(
                    expected = expected,
                    actual = actual
                )
            }
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query invalid page number, assert error`() {
        withTestApplication(moduleFunction = Application::module){
            handleRequest(HttpMethod.Get, "/boruto/heroes?page=invalid").apply {
                assertEquals(
                    HttpStatusCode.BadRequest,
                    response.status()
                )
                val expected = ApiResponse(
                    success = false,
                    message = "Only Numbers Allowed."
                )
                val actual = Json.decodeFromString<ApiResponse>(response.content!!)
                assertEquals(
                    expected = expected,
                    actual = actual
                )
            }
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query existing hero name, assert correct information`() {
        withTestApplication(moduleFunction = Application::module){
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=Naruto").apply {
                assertEquals( HttpStatusCode.OK, response.status())
                val actual = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes?.size
                assertEquals(1, actual)
            }
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes result`() {
        withTestApplication(moduleFunction = Application::module){
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=sa").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val actual = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes?.size
                assertEquals(3, actual)
            }
        }
    }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero nam, assert multiple heroes result`() {
        withTestApplication(moduleFunction = Application::module){
            handleRequest(HttpMethod.Get, "/boruto/heroes/search?name=").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val actual = Json.decodeFromString<ApiResponse>(response.content.toString()).heroes?.size
                assertEquals(0 , actual)
            }
        }
    }


    private fun calculatePage(page : Int): Map<String, Int?>{
        var prevPage: Int? = page
        var nextPage: Int? = page + 1

        if(page in 1..4){
            nextPage = nextPage?.plus(1)
        }
        if(page in 2..5){
            prevPage = prevPage?.minus(1)
        }
        if(page == 1){
            prevPage = null
        }
        if(page == 5){
            nextPage = null
        }
        return mapOf(
            PREVIOUS_PAGE_KEY to prevPage,
            NEXT_PAGE_KEY to nextPage
        )

    }


}


