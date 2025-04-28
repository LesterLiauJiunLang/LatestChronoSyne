package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.ViewModelProvider
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import coil.compose.AsyncImage
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.formatAsDate
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.TimeZone



class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


        val eventViewModel = ViewModelProvider(this, EventViewModelFactory(eventRepository))
            .get(EventViewModel::class.java)
        setContent {
            ChronoSyneTheme {
                NotificationScreen (
                    eventViewModel = eventViewModel,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}


@Composable
fun NotificationScreen(
    eventViewModel: EventViewModel,
    onBackPressed: () -> Unit,
    profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString()) {
    val backgroundColor = Color(0xFF282828)
    val cardColor = Color(0xFFb29985)
    val textColor = Color(0xFFb29985)
    val currentUser = Firebase.auth.currentUser
    val photoUrl = currentUser?.photoUrl

    val context = LocalContext.current
    val upcomingEvents by eventViewModel.upcomingEvents.collectAsState(initial = emptyList())



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 16.dp)
        ) {
            // Back button (LEFT)
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

            // Center Title
            Text(
                text = "Notifications",
                color = textColor,
                fontFamily = PlayfairFontFamily,
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Profile + Notifications (RIGHT)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = textColor
                    )
                }


                IconButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                ProfileActivity::class.java
                            )
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cardColor)
                    ) {
                        val photoUrl = Firebase.auth.currentUser?.photoUrl
                        if (photoUrl != null) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.Black,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

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
                    val categoryName =
                        eventWithCategories.categories.firstOrNull()?.name ?: "Custom"
                    val iconPainter = when (categoryName) {
                        "Task" -> painterResource(id = R.drawable.assignment_icon)
                        "Event" -> painterResource(id = R.drawable.school_icon)
                        "Birthday" -> painterResource(id = R.drawable.birthday_icon)
                        else -> painterResource(id = R.drawable.default_icon)
                    }

                    EventNotifCard(
                        icon = iconPainter,
                        title = eventWithCategories.event.title,
                        date = "Date: ${eventWithCategories.event.startTime.formatAsDate()}",
                        cardColor = cardColor,
                        onClick = {
                            val intent = Intent(context, EventDetailActivity::class.java).apply {
                                putExtra(
                                    "EVENT_ID",
                                    eventWithCategories.event.eventId.toInt()
                                ) // Convert to Int
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