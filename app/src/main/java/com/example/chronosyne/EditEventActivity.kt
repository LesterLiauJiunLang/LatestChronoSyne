package com.example.chronosyne

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.Event
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.utils.NotificationHelper
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventDao = AppDatabase.getInstance(this).eventDao()
        val eventCategoryDao = AppDatabase.getInstance(this).crossRefDao()
        val firestore = Firebase.firestore

        val repository = EventRepository(
            eventDao = eventDao,
            crossRefDao = eventCategoryDao,
            auth = Firebase.auth,
            firestore = firestore
        )

        val eventViewModel = ViewModelProvider(
            this,
            EventViewModelFactory(repository)
        ).get(EventViewModel::class.java)

        setContent {
            ChronoSyneTheme {
                EditEventScreen(eventViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(eventViewModel: EventViewModel) {
    val backgroundColor = Color(0xFF1E1E1E)
    val buttonColor = Color(0xFFBEA99B)
    val textColor = Color.White
    val context = LocalContext.current

    

    val eventId = (context as Activity).intent.getIntExtra("EVENT_ID", -1)

    var event by remember { mutableStateOf<Event?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Event") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var enableNotification by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)

    val notificationOptions = listOf("1 week before", "1 day before", "1 hour before", "30 minutes before")
    var selectedNotificationTiming by remember { mutableStateOf("1 hour before") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(eventId) {
        if (eventId != -1) {
            event = eventViewModel.getEventById(eventId.toLong())
            event?.let {
                title = it.title
                description = it.description ?: ""
                selectedDate = it.startTime
                selectedCategory = "Event" // You can load the real category if you track it
                enableNotification = true // Assume true if notifications exist (or adjust based on your system)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { (context as Activity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = buttonColor
                        )
                    }
                },
                actions = {
                    Row {
                        TextButton(
                            onClick = {
                                (context as Activity).finish()
                            }
                        ) {
                            Text("Discard", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (title.isBlank()) return@Button
                                    event?.let { existingEvent ->
                                        scope.launch {
                                            val updatedEvent = existingEvent.copy(
                                                title = title,
                                                description = description.ifBlank { null },
                                                startTime = selectedDate,
                                                endTime = Date(selectedDate.time + 60_000),
                                                updatedAt = Date()
                                            )

                                            try {
                                                eventViewModel.updateEvent(updatedEvent)

                                                if (enableNotification) {
                                                    NotificationHelper.createNotificationChannel(context)
                                                    NotificationHelper.scheduleNotification(context, updatedEvent)
                                                }

                                                // After successful update and (optional) notification setup
                                                withContext(Dispatchers.Main) {
                                                    val intent = Intent(context, MainActivity::class.java)
                                                    context.startActivity(intent)
                                                    (context as Activity).finish()
                                                }
                                            } catch (e: Exception) {
                                                Log.e("EditEvent", "Failed to update event: ${e.message}")
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save")
                            }

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title", color = buttonColor) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = backgroundColor,
                    unfocusedContainerColor = backgroundColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = buttonColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = backgroundColor,
                    unfocusedContainerColor = backgroundColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Event", "Task", "Birthday").forEach { category ->
                    EventTypeChip(
                        text = category,
                        selected = selectedCategory == category,
                        onSelect = { selectedCategory = category }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                listOf("Custom").forEach { category ->
                    EventTypeChip(
                        text = category,
                        selected = selectedCategory == category,
                        onSelect = { selectedCategory = category }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EventSettingRow(
                icon = Icons.Default.Notifications,
                text = "Enable Notification",
                hasToggle = true,
                initialToggleState = enableNotification,
                onToggleChange = { enableNotification = it }
            )

            EventSettingRow(
                icon = Icons.Default.DateRange,
                text = selectedDate.toString(),
                hasToggle = false,
                onClick = { showDatePicker = true }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = Date(it)
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}
