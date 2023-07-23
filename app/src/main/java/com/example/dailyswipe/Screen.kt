package com.example.dailyswipe

sealed class Screen(val route: String) {
    object Category : Screen("category")
    object Swipe : Screen("swipe")
}