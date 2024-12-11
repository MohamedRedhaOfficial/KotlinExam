package com.example.examwithjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.examwithjetpackcompose.ui.theme.ExamWithJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExamWithJetpackComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageCarouselWithList(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class ImageData(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val items: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCarouselWithList(modifier: Modifier = Modifier) {
    // List of image data
    val imageDataList = listOf(
        ImageData(R.drawable.fruits, "Fruits", "Fresh and delicious", listOf("Apple", "Banana", "Orange")),
        ImageData(R.drawable.vegetables, "Vegetables", "Healthy and nutritious", listOf("Carrot", "Potato", "Tomato")),
        ImageData(R.drawable.berries, "Berries", "Sweet and tangy", listOf("Strawberry", "Blueberry", "Blackberry")),
        ImageData(R.drawable.tropicalfruits, "Tropical Fruits", "Exotic and tasty", listOf("Pineapple", "Mango", "Papaya"))
    )

    // Pager state for the carousel
    val pagerState = rememberPagerState(pageCount = { imageDataList.size })

    // State for search query
    var searchQuery by remember { mutableStateOf("") }

    // Current page and filtered list
    val currentPage = remember { mutableStateOf(0) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            currentPage.value = page
        }
    }
    val currentImageItems = imageDataList[currentPage.value].items
    val filteredItems = currentImageItems.filter { it.contains(searchQuery, ignoreCase = true) }

    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Show ModalBottomSheet only when showBottomSheet is true
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            StatisticsSheet(
                itemCount = filteredItems.size,
                topCharacters = calculateTopCharacters(filteredItems)
            ) {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    showBottomSheet = false
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showBottomSheet = true
                    scope.launch { sheetState.show() }
                },
                containerColor = Color(0xff24caeb),
                shape = CircleShape,
            ) {
                Icon(painterResource(id = R.drawable.dotdotdot),
                    contentDescription = "Show Stats",
                    tint = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Search bar
            item {
                SearchBar(query = searchQuery) { query ->
                    searchQuery = query
                }
            }

            // Image carousel with title and subtitle
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) { page ->
                        Image(
                            painter = painterResource(id = imageDataList[page].imageRes),
                            contentDescription = "Image ${imageDataList[page].title}",
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(64.dp))
                        )
                    }

                    // Title and subtitle
                    val currentImage = imageDataList[pagerState.currentPage]
                    Text(
                        text = currentImage.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Text(
                        text = currentImage.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Page indicator (dots)
                    Row(
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .align(CenterHorizontally)
                            .padding(top = 8.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(12.dp)
                            )
                        }
                    }
                }
            }

            // Filtered list
            items(filteredItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color(0xffd0ece4),shape = RoundedCornerShape(10.dp))
                ) {
                    // Thumbnail for the current image
                    Image(
                        painter = painterResource(id = imageDataList[currentPage.value].imageRes),
                        contentDescription = "Thumbnail for $item",
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(64.dp))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f), // Ensures it takes up remaining space in the Row
                        horizontalAlignment = Alignment.Start // Aligns text to the left
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Details about $item",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsSheet(
    itemCount: Int,
    topCharacters: List<Pair<Char, Int>>,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text("Item Count: $itemCount", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Top Characters:", style = MaterialTheme.typography.bodyLarge)
        topCharacters.forEach { (char, count) ->
            Text("$char = $count", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onClose) {
            Text("Close")
        }
    }
}

fun calculateTopCharacters(items: List<String>): List<Pair<Char, Int>> {
    val characterFrequency = mutableMapOf<Char, Int>()

    // Count character frequencies across all items
    items.forEach { item ->
        item.lowercase().forEach { char ->
            if (char.isLetter()) {
                characterFrequency[char] = characterFrequency.getOrDefault(char, 0) + 1
            }
        }
    }

    // Sort by frequency and take the top 3
    return characterFrequency.entries
        .sortedByDescending { it.value }
        .take(3)
        .map { it.key to it.value }
}

@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Search") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExamWithJetpackComposeTheme {
        ImageCarouselWithList()
    }
}
