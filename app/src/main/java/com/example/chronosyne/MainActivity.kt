package com.example.chronosyne

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.formatAsDate
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.utils.NotificationHelper
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check authentication state
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Check exact alarm permission FIRST
            checkExactAlarmPermission()
            NotificationHelper.createNotificationChannel(this)

            // Initialize Room Database DAOs
            val eventDao = AppDatabase.getInstance(this).eventDao()
            val eventCategoryDao = AppDatabase.getInstance(this).crossRefDao()
            val firestore = FirebaseFirestore.getInstance()

            // Manually construct EventRepository
            val eventRepository = EventRepository(
                eventDao = eventDao,
                crossRefDao = eventCategoryDao,
                auth = FirebaseAuth.getInstance(),
                firestore = firestore
            )

            // Create ViewModel
            val eventViewModel = ViewModelProvider(this, EventViewModelFactory(eventRepository))
                .get(EventViewModel::class.java)

            setContent {
                ChronoSyneTheme {
                    HomeScreen(eventViewModel = eventViewModel)
                }
            }
        }
    }

    // Move the permission check function OUTSIDE onCreate
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun HomeScreen(eventViewModel: EventViewModel = viewModel(),
                auth: FirebaseAuth = Firebase.auth
) {
    val backgroundColor = Color(0xFF1E1E1E)
    val cardColor = Color(0xFFBEA99B)
    val textColor = Color(0xFFBEA99B)
    val currentUser = Firebase.auth.currentUser
    val userId = currentUser?.uid

    val userDocRef = Firebase.firestore.collection("users").document(userId ?: "")
    val email = currentUser?.email
    val photoUrl = currentUser?.photoUrl

    val context = LocalContext.current
    // Collect events from ViewModel
    val upcomingEvents by eventViewModel.upcomingEvents.collectAsState(initial = emptyList())
    val futureEvents by eventViewModel.futureEvents.collectAsState(initial = emptyList())


    Scaffold(
        bottomBar = {
            BottomNavBar(backgroundColor = cardColor)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { val intent = Intent(context, AddNewEvent::class.java)
                    context.startActivity(intent)},
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Event"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
        ) {
            // Removed leaf decoration image to avoid reference error

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Home",
                        color = textColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = textColor
                            )
                        }

                        IconButton(onClick = { /* Open notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = textColor
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cardColor)
                        ) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                // ADD TEST BUTTON HERE
                Button(
                    onClick = {
                        val testEvent = Event(
                            eventId = 0,
                            userId = userId ?: "test",
                            title = "Test Event",
                            description = "Test Notification",
                            startTime = Date(System.currentTimeMillis() + 60000), // 1 minute from now
                            endTime = Date(System.currentTimeMillis() + 120000),
                            isAllDay = false,
                            createdAt = Date(),
                            updatedAt = Date(),
                            location = null
                        )
                        NotificationHelper.scheduleNotification(context, testEvent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Test Notification Scheduling")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Upcoming Events Section
                Text(
                    text = "Upcoming Events",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (upcomingEvents.isEmpty()) {
                    Text(
                        text = "No upcoming events",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    upcomingEvents
                        .sortedBy { it.event.startTime }
                        .forEach { eventWithCategories ->
                            val categoryName = eventWithCategories.categories.firstOrNull()?.name ?: "Custom"
                            val iconPainter = when (categoryName) {
                                "Task" -> painterResource(id = R.drawable.assignment_icon)
                                "Event" -> painterResource(id = R.drawable.school_icon)
                                "Birthday" -> painterResource(id = R.drawable.birthday_icon)
                                else -> painterResource(id = R.drawable.default_icon)
                            }

                            EventCard(
                                icon = iconPainter,
                                title = eventWithCategories.event.title,
                                date = "Date: ${eventWithCategories.event.startTime.formatAsDate()}",
                                cardColor = cardColor
                            )
                        }

                }

                Spacer(modifier = Modifier.height(16.dp))

                // Future Events Section

                Text(
                    text = "Future Events",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (futureEvents.isEmpty()) {
                    Text(
                        text = "No future events",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    futureEvents
                        .sortedBy { it.event.startTime }
                        .forEach { eventWithCategories ->
                            val categoryName = eventWithCategories.categories.firstOrNull()?.name ?: "Custom"
                            val iconPainter = when (categoryName) {
                                "Task" -> painterResource(id = R.drawable.assignment_icon)
                                "Event" -> painterResource(id = R.drawable.school_icon)
                                "Birthday" -> painterResource(id = R.drawable.birthday_icon)
                                else -> painterResource(id = R.drawable.default_icon)
                            }

                            EventCard(
                                icon = iconPainter,
                                title = eventWithCategories.event.title,
                                date = "Date: ${eventWithCategories.event.startTime.formatAsDate()}",
                                cardColor = cardColor
                            )
                        }
                }

            }
        }
    }
}

@Composable
fun EventCard(
    icon: Painter,
    title: String,
    date: String,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                    fontSize = 16.sp
                )

                Text(
                    text = date,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(backgroundColor: Color) {
    BottomAppBar(
        containerColor = backgroundColor,
        contentColor = Color.Black,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange, // Using DateRange instead of CalendarMonth
                    contentDescription = "Calendar",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Calendar",
                    fontSize = 12.sp
                )
            }

            // Empty space for FAB
            Spacer(modifier = Modifier.width(56.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "My Events",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "My Events",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ChronoSyneTheme {
        HomeScreen()
    }
}
