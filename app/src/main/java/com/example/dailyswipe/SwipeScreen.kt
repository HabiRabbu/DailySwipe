package com.example.dailyswipe

import android.annotation.SuppressLint
import android.os.Debug
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.dailyswipe.Screen
import com.example.dailyswipe.ui.theme.InterExtraBold
import com.example.dailyswipe.fetchArticles
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

var offset = 0

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen() {
    val coroutineScope = rememberCoroutineScope()
    val articles = remember { mutableStateOf<List<ApiArticle>>(emptyList()) }
    val api = createNewsApi()
    val isLoading = remember { mutableStateOf(false) }

    val pagerState = rememberPagerState()

    val dragOffset = remember { mutableStateOf(0f) }
    val cardHeight = remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    val isDescriptionVisible = remember { mutableStateOf(false) }

    val sharedPreferencesHelper = SharedPreferencesHelper(LocalContext.current)
    val countryCode = remember { mutableStateOf(sharedPreferencesHelper.getUserCountry()) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val countryMapping = mapOf(
        "UK" to "gb",
        "USA" to "us",
        "France" to "fr",
        "China" to "cn"
    )

    suspend fun fetchMoreArticles() {
        val selectedCategoryStrings = selectedCategories
            .filter { it.value }
            .keys
            .toList()

        var newArticles: List<ApiArticle>
        var attempts = 0

        do {
            val params = mapOf(
                "access_key" to "b6022a2a22e24c69c4b9adca763613ff",
                "countries" to countryCode.value.toString(),
                "categories" to selectedCategoryStrings.joinToString(","),
                "limit" to "5",
                "offset" to offset.toString()
            )

            isLoading.value = true
            val response = api.getNews(params)
            Log.d("SwipeScreen", "API response received: $response")
            isLoading.value = false
            newArticles = response.data
               // .filter { it.image != null }
                .map {
                    ApiArticle(
                        title = it.title,
                        image = it.image,
                        url = it.url,
                        description = it.description,
                        author = it.author,
                        published_at = it.published_at
                    )
                }
            Log.d("SwipeScreen", "New articles fetched: ${newArticles.size}. Total articles: ${articles.value.size}")

            attempts += 1
            offset += newArticles.size

            if (attempts >= 10) {
                Log.d("SwipeScreen", "Maximum attempts reached. Breaking.")
                return
            }

        } while (newArticles.isEmpty())

        articles.value = articles.value + newArticles
    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        if (pagerState.currentPage == articles.value.size - 1 || articles.value.isEmpty()) {
            fetchMoreArticles()
        }
    }
    LaunchedEffect(key1 = Unit) {
        fetchMoreArticles()
    }

    LaunchedEffect(countryCode.value) {
        articles.value = listOf()
        fetchMoreArticles()
    }

    ModalNavigationDrawer(
        gesturesEnabled = false,
        drawerState = drawerState,
        drawerContent = {
            if (drawerState.isOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .clickable {
                            coroutineScope.launch { drawerState.close() }
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Card(
                        modifier = Modifier
                            .width(200.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val countries = listOf("USA", "UK", "France", "China")
                            countries.forEach { country ->
                                Card(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(10.dp)
                                        .clickable {
                                            val newCountryCode = countryMapping[country] ?: country
                                            sharedPreferencesHelper.setUserCountry(newCountryCode)
                                            countryCode.value = newCountryCode
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                    colors = if (countryCode.value == (countryMapping[country] ?: country)) CardDefaults.cardColors(
                                        Color.Red
                                    ) else CardDefaults.cardColors(Color.LightGray)
                                ) {
                                    Text(
                                        text = country,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (countryCode.value == (countryMapping[country] ?: country)) Color.White else Color.Black,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "DailySwipe",
                            fontFamily = InterExtraBold,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_density_medium_24),
                                contentDescription = "menu",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp)) // Space between name and the card

                    HorizontalPager(
                        state = pagerState,
                        pageCount = articles.value.size,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !isDescriptionVisible.value,
                    ) { page ->
                        // Content of a single article/card
                        val article = articles.value[page]

                        Box {
                            // Description card below the image card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(30.dp)),
                                colors = CardDefaults.cardColors(Color.White),
                            ) {
                                WebViewContainer(
                                    url = article.url,
                                    modifier = Modifier.padding(top = 100.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                                    load = page == pagerState.currentPage
                                )
                            }
                        }
                        val isCardAtTopOrBottom = remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .onSizeChanged { size ->
                                    cardHeight.value = size.height.toFloat()
                                }
                                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 0.dp))
                                .offset {
                                    IntOffset(
                                        0,
                                        dragOffset.value.roundToInt()
                                    )
                                } // apply the drag offset
                                .draggable(
                                    state = rememberDraggableState { delta ->
                                        dragOffset.value += delta
                                        if (dragOffset.value > 0f) dragOffset.value =
                                            0f // prevent dragging down
                                        if (dragOffset.value < -cardHeight.value + 200f) dragOffset.value =
                                            -cardHeight.value + 200f
                                    },
                                    orientation = Orientation.Vertical,
                                    onDragStopped = { velocity ->
                                        val targetValue = if (velocity < 0) {
                                            // if dragging upwards, snap to top
                                            isDescriptionVisible.value = true
                                            -cardHeight.value + 200f
                                        } else {
                                            // else snap back to 0
                                            isDescriptionVisible.value = false
                                            0f
                                        }
                                        val animation = coroutineScope.launch {
                                            animate(
                                                initialValue = dragOffset.value,
                                                targetValue = targetValue,
                                                animationSpec = spring()
                                            ) { value, _ ->
                                                dragOffset.value = value
                                            }
                                        }
                                        coroutineScope.launch {
                                            animation.join()
                                            isCardAtTopOrBottom.value =
                                                true // change the state to true when the card is at top or bottom
                                            Log.d(
                                                "SwipeScreen",
                                                "isDescriptionVisible: ${isDescriptionVisible.value}"
                                            )
                                        }
                                    }
                                ),
                            colors = CardDefaults.cardColors(Color.White),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    val article = articles.value[page]
                                    val image = rememberImagePainter(data = article.image)
                                    Image(
                                        painter = image,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .height(200.dp)
                                            .fillMaxWidth(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = article.title,
                                        fontFamily = InterExtraBold,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.Black,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    val descriptionLimit = 100
                                    val description = if (article.description.length > descriptionLimit) {
                                        article.description.substring(0, descriptionLimit) + "[...]"
                                    } else {
                                        article.description
                                    }
                                    Text(
                                        text = description,
                                        fontFamily = InterExtraBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Black,
                                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    )
                                    if(article.author != null){
                                        Text(
                                            text = article.author,
                                            fontFamily = InterExtraBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black,
                                            modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                                        )
                                    }
                                    val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", java.util.Locale.US)
                                    val targetFormat = SimpleDateFormat("dd MMMM yyyy", java.util.Locale.US)
                                    val originalDate: Date? = originalFormat.parse(article.published_at)
                                    val formattedDate: String? = originalDate?.let { targetFormat.format(it) }
                                    Text(
                                        text = formattedDate ?: "Unknown date",
                                        fontFamily = InterExtraBold,
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    )
                                }

                                val infiniteTransition = rememberInfiniteTransition()
                                val bounce by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = -10f,  //control bounce distance
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(
                                            durationMillis = 500, // control speed of bouncing
                                            easing = LinearEasing
                                        ),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )

                                // Arrow image
                                Image(
                                    painter = painterResource(R.drawable.baseline_arrow_upward_24),
                                    contentDescription = "Swipe arrow",
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .size(48.dp)
                                        .offset(y = bounce.dp)
                                        .rotate(if (isDescriptionVisible.value && isCardAtTopOrBottom.value) 180f else 0f)
                                )
                            }
                        }
                    }
                }
            }


            if (isLoading.value) {
                // Display a loading indicator?
                Log.d("test","Is loading")
            } else if (articles.value.isEmpty()) {
                // Display an error message
                Log.d("test","Is Empty")
            } else {
                // Display articles
                Log.d("test","Display articles!")
            }
        })
}



@Composable
fun WebViewContainer(
    url: String,
    modifier: Modifier = Modifier,
    load: Boolean
) {
    if (load) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            })
    } else {
        Box(modifier = modifier) {
        }
    }
}
