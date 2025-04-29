package com.example.chronosyne

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.formatAsDate
import com.example.chronosyne.database.relations.EventWithCategories
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.collections.filter

class MyEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventDao = AppDatabase.getInstance(this).eventDao()
        val crossRefDao = AppDatabase.getInstance(this).crossRefDao()
        val eventRepository = EventRepository(
            eventDao = eventDao,
            crossRefDao = crossRefDao,
            auth = Firebase.auth,
            firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        )

        val eventViewModel = EventViewModelFactory(eventRepository).create(EventViewModel::class.java)

        setContent {
            ChronoSyneTheme {
                MyEventScreen(
                    eventViewModel = eventViewModel,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@Composable
fun MyEventScreen(
    eventViewModel: EventViewModel,
    onBackPressed: () -> Unit,
    profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString()
) {
    val backgroundColor = Color(0xFF282828)
    val cardColor = Color(0xFFb29985)
    val textColor = Color(0xFFb29985)

    val context = LocalContext.current
    val allEvents by eventViewModel.allEvents.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Upcoming") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Top Bar
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { onBackPressed() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            Text(
                text = "My Events",
                color = textColor,
                fontSize = 28.sp,
                fontFamily = PlayfairFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = textColor
                    )
                }

                IconButton(
                    onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cardColor)
                    ) {
                        if (profilePhotoUrl != null) {
                            AsyncImage(
                                model = profilePhotoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.Black,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        EventSearchBar(searchQuery) { searchQuery = it }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        EventFilterChips(selectedFilter) { selectedFilter = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Events List
        FilteredEventList(
            events = allEvents,
            searchQuery = searchQuery,
            selectedFilter = selectedFilter,
            cardColor = cardColor,
            context = context
        )
    }
}

@Composable
fun EventSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search events...", color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(50.dp), // <--- THIS makes it really rounded
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black
        )
    )
}


@Composable
fun EventFilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("Past", "Upcoming", "Future").forEach { filter ->
            Button(
                onClick = { onFilterSelected(filter) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == filter) Color.White else Color.Gray,
                    contentColor = if (selectedFilter == filter) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(50), // More rounded
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Nice spacing
                modifier = Modifier
                    .defaultMinSize(minHeight = 36.dp) // Minimum height
            ) {
                Text(
                    text = filter,
                    fontFamily = PlayfairFontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun FilteredEventList(
    events: List<EventWithCategories>,
    searchQuery: String,
    selectedFilter: String,
    cardColor: Color,
    context: Context
) {
    val now = System.currentTimeMillis()

    val filtered = events.filter { eventWithCategory ->
        val titleMatches = eventWithCategory.event.title.contains(searchQuery, ignoreCase = true)

        val timeMatches = when (selectedFilter) {
            "Past" -> eventWithCategory.event.endTime.time < now
            "Upcoming" -> eventWithCategory.event.startTime.time in (now..(now + 7 * 24 * 60 * 60 * 1000))
            "Future" -> eventWithCategory.event.startTime.time > (now + 7 * 24 * 60 * 60 * 1000)
            else -> true
        }
        titleMatches && timeMatches
    }.sortedByDescending { it.event.startTime }

    if (filtered.isEmpty()) {
        Text(
            text = "No events found",
            fontFamily = RalewayFontFamily,
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(8.dp)
        )
    } else {
        Column {
            filtered.forEach { eventWithCategory ->
                val categoryName = eventWithCategory.categories.firstOrNull()?.name ?: "Custom"

                val iconPainter = when (categoryName) {
                    "Task" -> painterResource(id = R.drawable.assignment_icon)
                    "Event" -> painterResource(id = R.drawable.school_icon)
                    "Birthday" -> painterResource(id = R.drawable.birthday_icon)
                    else -> painterResource(id = R.drawable.default_icon)
                }

                EventNotifCard(
                    icon = iconPainter,
                    title = eventWithCategory.event.title,
                    date = "Date: ${eventWithCategory.event.startTime.formatAsDate()}",
                    cardColor = cardColor,
                    onClick = {
                        val intent = Intent(context, EventDetailActivity::class.java).apply {
                            putExtra("EVENT_ID", eventWithCategory.event.eventId.toInt())
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun EventNotifCard(
    icon: Painter,
    title: String,
    date: String,
    cardColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RalewayFontFamily,
                    fontSize = 16.sp
                )

                Text(
                    text = date,
                    color = Color.Black,
                    fontFamily = RalewayFontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}
