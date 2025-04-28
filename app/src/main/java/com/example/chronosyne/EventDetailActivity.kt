package com.example.chronosyne

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.formatAsDate
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manual dependency setup
        val eventDao = AppDatabase.getInstance(this).eventDao()
        val crossRefDao = AppDatabase.getInstance(this).crossRefDao()
        val repository = EventRepository(
            eventDao = eventDao,
            crossRefDao = crossRefDao,
            auth = Firebase.auth,
            firestore = Firebase.firestore
        )

        val eventId = intent.getIntExtra("EVENT_ID", -1)

        val viewModel = ViewModelProvider(
            this,
            EventViewModelFactory(repository)  // Use your existing factory
        )[EventViewModel::class.java]

        setContent {
            ChronoSyneTheme {
                EventDetailScreen(eventViewModel = viewModel)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(eventViewModel: EventViewModel = viewModel()) {
    val backgroundColor = Color(0xFF1E1E1E)
    val cardColor = Color(0xFFBEA99B)
    val textColor = Color(0xFFBEA99B)
    val context = LocalContext.current
    val eventId = (context as Activity).intent.getIntExtra("EVENT_ID", -1)
    var event by remember { mutableStateOf<Event?>(null) }
    var timeRemaining by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // Fetch event details
    LaunchedEffect(eventId) {
        if (eventId != -1) {
            event = eventViewModel.getEventById(eventId.toLong()) // if your repository still expects Long
        }
    }


    // Update time remaining every second
    LaunchedEffect(Unit) {
        while (true) {
            event?.startTime?.let {
                val diff = it.time - System.currentTimeMillis()
                timeRemaining = formatDuration(diff)
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Event Details",
                        color = textColor,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Event Button
                Button(
                    onClick = {
                        val intent = Intent(context, EditEventActivity::class.java)
                        intent.putExtra("EVENT_ID", eventId)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardColor,
                        contentColor = Color.Black
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Edit Event")
                }

                // Delete Event Button
                Button(
                    onClick = { showDialog = true }, // Set showDialog to true
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Delete Event")
                }
            }

            // ✅ AlertDialog shown outside Button but still inside bottomBar
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                if (eventId != -1) {
                                    eventViewModel.deleteEventById(eventId.toLong())
                                    val intent = Intent(context, MainActivity::class.java)
                                    context.startActivity(intent)
                                    (context as Activity).finish()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            event?.let {
                // Event Title
                DetailCard(title = "Title", content = it.title, cardColor = cardColor)

                // Event Description
                if (!it.description.isNullOrEmpty()) {
                    DetailCard(title = "Description", content = it.description, cardColor = cardColor)
                }

                // Start Time
                DetailCard(
                    title = "Starts",
                    content = it.startTime.formatAsDate(),
                    cardColor = cardColor
                )

                // End Time
                DetailCard(
                    title = "Ends",
                    content = it.endTime.formatAsDate(),
                    cardColor = cardColor
                )

                // Time Remaining
                DetailCard(
                    title = "Time Remaining",
                    content = timeRemaining,
                    cardColor = cardColor
                )
            } ?: Text("Loading event details...", color = textColor)
        }
    }
}

@Composable
fun DetailCard(title: String, content: String, cardColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    return when {
        millis <= 0 -> "Event has started"
        else -> {
            val days = TimeUnit.MILLISECONDS.toDays(millis)
            val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

            buildString {
                if (days > 0) append("$days days ")
                if (hours > 0) append("$hours hours ")
                if (minutes > 0) append("$minutes minutes ")
                append("$seconds seconds")
            }
        }
    }
}

@Preview
@Composable
fun EventDetailPreview() {
    ChronoSyneTheme {
        EventDetailScreen()
    }
}