package com.example.dailyswipe

data class ApiResponse(
    val pagination: Pagination,
    val data: List<ApiArticle>
)

data class ApiArticle(
    val title: String,
    val url: String,
    val image: String?,
    val description: String,
    val author: String?,
    val published_at: String
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)