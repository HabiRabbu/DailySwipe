package com.example.dailyswipe

suspend fun fetchArticles(): List<Article> {
    //Temp fake articles
    return listOf(
        Article("Headline 1", "https://example.com/image1.png", "https://example.com/article1"),
        Article("Headline 2", "https://example.com/image2.png", "https://example.com/article2"),
        Article("Headline 3", "https://example.com/image3.png", "https://example.com/article3")
    )
}