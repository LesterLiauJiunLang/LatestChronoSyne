package com.example.chronosyne


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainCalendarScreen()
        }
    }
}

@Composable
fun MainCalendarScreen() {
    val backgroundColor = Color(0xFF1E1E1E)
    val accentColor = Color(0xFFBC9F84)
    val textColor = Color.White

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // App header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Calendar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            // Calendar UI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Left side: Calendar view
                CalendarView(
                    modifier = Modifier.weight(1f),
                    backgroundColor = backgroundColor,
                    accentColor = accentColor,
                    textColor = textColor
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Right side: Event detail view
                EventDetailView(
                    modifier = Modifier.weight(1f),
                    backgroundColor = backgroundColor,
                    accentColor = accentColor,
                    textColor = textColor
                )

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
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

fun CalendarView(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    accentColor: Color,
    textColor: Color
) {
    var showEventDialog by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableIntStateOf(21) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // Header with back button, title, settings, and profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Calender",
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .border(1.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile icon or image would go here
                    Text(
                        text = "👤",
                        fontSize = 16.sp
                    )
                }
            }

            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous month",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "2025 March",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next month",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Calendar days of week header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = accentColor
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    for (day in listOf("M", "T", "W", "T", "F", "S", "S")) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Calendar days grid
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = accentColor
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Week 1
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (i in 0..6) {
                            if (i < 5) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                CalendarDay(
                                    day = i - 4,
                                    isSelected = selectedDate == (i - 4),
                                    onDateSelected = { selectedDate = it },
                                    textColor = Color.Black
                                )
                            }
                        }
                    }

                    // Weeks 2-6
                    for (week in 0..4) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (day in 1..7) {
                                val date = week * 7 + day - 1
                                if (date <= 31) {
                                    CalendarDay(
                                        day = date,
                                        isSelected = selectedDate == date,
                                        onDateSelected = { selectedDate = it },
                                        textColor = Color.Black
                                    )
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Events section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Events",
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Event items
                EventItem(
                    title = "Group Study",
                    isSelected = true,
                    accentColor = accentColor
                )

                EventItem(
                    title = "Gym",
                    isSelected = false,
                    accentColor = accentColor
                )

                EventItem(
                    title = "Basketball Training",
                    isSelected = false,
                    accentColor = accentColor
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { showEventDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendar",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // WL indicator at bottom right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WL",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
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

    // Event dialog
    if (showEventDialog) {
        EventDialog(
            onDismiss = { showEventDialog = false }
        )
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    onDateSelected: (Int) -> Unit,
    textColor: Color
) {
    Box(
        modifier = Modifier

            .padding(4.dp)
            .clickable { onDateSelected(day) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = if (isSelected) {
                Modifier
                    .size(30.dp)
                    .background(Color(0xFF1E90FF), CircleShape)
            } else {
                Modifier.size(30.dp)
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                color = if (isSelected) Color.White else textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EventItem(
    title: String,
    isSelected: Boolean,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.Blue else Color.Gray)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            color = accentColor,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EventDetailView(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    accentColor: Color,
    textColor: Color
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // Header with back button, title, settings, and profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Calender",
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .border(1.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Profile icon or image would go here
                    Text(
                        text = "👤",
                        fontSize = 16.sp
                    )
                }
            }

            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous month",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "2025 March",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next month",
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Event details header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = accentColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "20 March 2024 Thursday",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Event details form
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = accentColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title field
                    EventFormField(
                        label = "Title:",
                        value = "Group Study"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle field
                    EventFormField(
                        label = "Subtitle:",
                        value = "With Edwin and Lester"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date field
                    EventFormField(
                        label = "20 March 2024",
                        value = "All Day"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Edit",
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Okay",
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Calendar",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
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

@Composable

fun EventFormField(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Text(
                text = value,
                color = Color.Black,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun EventDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFBC9F84)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Create New Event",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                EventFormField(
                    label = "Title:",
                    value = ""
                )

                Spacer(modifier = Modifier.height(8.dp))

                EventFormField(
                    label = "Date:",
                    value = "20 March 2024"
                )

                Spacer(modifier = Modifier.height(8.dp))

                EventFormField(
                    label = "Time:",
                    value = "All Day"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Save",
                            color = Color.Black
                        )
                    }
                }
            }
        }
fun HomeScreenPreview() {
    ChronoSyneTheme {
        HomeScreen()
    }
}
