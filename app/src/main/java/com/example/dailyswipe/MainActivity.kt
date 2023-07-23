@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.dailyswipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyswipe.ui.theme.DailySwipeTheme
import java.util.Calendar
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dailyswipe.ui.theme.InterExtraBold
import com.example.dailyswipe.Screen

val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController provided") }

val categories = listOf("general", "business", "entertainment", "health", "science", "sports", "technology")
val selectedCategories = mutableStateMapOf<String, Boolean>().apply { putAll(categories.associateWith { false }) }

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferencesHelper = SharedPreferencesHelper(this)


        setContent {
            DailySwipeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    CompositionLocalProvider(LocalNavController provides navController) {
                        NavHost(navController, startDestination = if (sharedPreferencesHelper.isFirstLaunch()) "firstLaunch" else Screen.Category.route) {
                            composable("firstLaunch") {
                                FirstLaunchScreen { name, country ->
                                    sharedPreferencesHelper.setFirstLaunch(false)
                                    sharedPreferencesHelper.setUserName(name)
                                    sharedPreferencesHelper.setUserCountry(country)
                                    navController.navigate(Screen.Category.route) {
                                        popUpTo("firstLaunch") { inclusive = true }
                                    }
                                }
                                Log.d("test", sharedPreferencesHelper.getUserCountry().toString())
                            }
                            composable(Screen.Category.route) {
                                CategoryScreen()
                            }
                            composable(Screen.Swipe.route) {
                                SwipeScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirstLaunchScreen(onFirstLaunchComplete: (String, String) -> Unit) {
    val nameState = remember { mutableStateOf("") }
    val countryState = remember { mutableStateOf("") } // default country is none
    val countryOptions = mapOf(
        "gb" to "UK",
        "us" to "USA",
        "fr" to "France",
        "cn" to "China"
    )

    val isFabEnabled = nameState.value.isNotEmpty() && countryState.value.isNotEmpty()

    Surface(color = Color.Red, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                fontFamily = InterExtraBold,
                text = "DailySwipe",
                color = Color.White,
                fontSize = 36.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Color.White),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("What is your name?", fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.Black, fontFamily = InterExtraBold)
                    OutlinedTextField(value = nameState.value, onValueChange = { newValue -> nameState.value = newValue }, label = { Text("Name") })
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Where do you want to view news from?", fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.Black, fontFamily = InterExtraBold)

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(countryOptions.size) { index ->
                            val option = countryOptions.entries.toList()[index]
                            CountryButton(
                                selectedCountry = countryState.value,
                                countryCode = option.key,
                                countryName = option.value,
                                onCountrySelected = { selectedCountry -> countryState.value = selectedCountry }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { if (isFabEnabled) onFirstLaunchComplete(nameState.value, countryState.value) },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                containerColor = if (isFabEnabled) Color.Red else Color.LightGray,
                content = {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            )
        }
    }
}

@Composable
fun CountryButton(selectedCountry: String, countryCode: String, countryName: String, onCountrySelected: (String) -> Unit) {
    Button(
        onClick = { onCountrySelected(countryCode) },
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedCountry == countryCode) Color.Red else Color.LightGray,
            contentColor = if (selectedCountry == countryCode) Color.White else Color.Black
        )
    ) {
        Text(countryName, textAlign = TextAlign.Center, fontFamily = InterExtraBold)
    }
}

@Composable
fun Greeting(name: String) {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good Morning,\n$name!"
        currentHour < 17 -> "Good Afternoon,\n$name!"
        else -> "Good Evening,\n$name!"
    }

    Surface(color = Color.Red, modifier = Modifier.height(120.dp)) {
        Text(
            text = greeting,
            fontFamily = InterExtraBold,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun CategoryScreen() {
    val navController = LocalNavController.current
    val sharedPreferencesHelper = SharedPreferencesHelper(LocalContext.current)
    val userName = sharedPreferencesHelper.getUserName()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red)
    ) {
        Greeting(name = userName.toString())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
                .padding(top = 16.dp)
        ) {
            Surface(color = Color.White, modifier = Modifier.height(100.dp)) {
                Text(
                    modifier = Modifier.padding(top = 16.dp, start = 8.dp),
                    textAlign = TextAlign.Left,
                    text = "What do you want to read about today?",
                    fontSize = 28.sp,
                    fontFamily = InterExtraBold,
                    color = Color.Black,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories.size) { index ->
                    CategoryItem(categories[index])
                }
            }
        }

        val anyCategorySelected = remember { mutableStateOf(false) }

        anyCategorySelected.value = selectedCategories.any { it.value }

        val fabColor = if (anyCategorySelected.value) Color.Red else Color.LightGray

        FloatingActionButton(
            onClick = {
                if (anyCategorySelected.value) {
                    navController.navigate(Screen.Swipe.route)
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = fabColor
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Next",
                tint = Color.White
            )
        }
    }
}

@Composable
fun CategoryItem(categoryName: String) {
    val isSelected = selectedCategories[categoryName] ?: false

    val cardColor = if (isSelected) Color.Red else Color.LightGray
    val textColor = if (isSelected) Color.White else Color.Black

    val cardModifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .requiredHeight(100.dp)
        .clickable { selectedCategories[categoryName] = !isSelected }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(cardColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        content = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = categoryName.capitalize(),
                    fontFamily = InterExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    )
}
