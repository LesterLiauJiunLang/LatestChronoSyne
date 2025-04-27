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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.Event
import com.example.chronosyne.fake.EventRepositoryStub
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.utils.NotificationHelper
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AddNewEvent : ComponentActivity() {
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up DAOs and repository
        val eventDao = AppDatabase.getInstance(this).eventDao()
        val eventCategoryDao = AppDatabase.getInstance(this).crossRefDao()
        val firestore = FirebaseFirestore.getInstance()

        val eventRepository = EventRepository(
            eventDao = eventDao,
            crossRefDao = eventCategoryDao,
            auth = auth,
            firestore = firestore
        )

        val eventViewModel = ViewModelProvider(
            this,
            EventViewModelFactory(eventRepository)
        ).get(EventViewModel::class.java)

        setContent {
            ChronoSyneTheme {
                AddNewEventScreen(eventViewModel = eventViewModel)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewEventScreen(eventViewModel: EventViewModel) {
    val backgroundColor = Color(0xFF1E1E1E)
    val buttonColor = Color(0xFFBEA99B)
    val textColor = Color.White
    val context = LocalContext.current

    // State variables
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedCategory by remember { mutableStateOf("Event") }
    var isAllDay by remember { mutableStateOf(false) }
    var enableNotification by remember { mutableStateOf(true) }

    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)

    var selectedNotificationTiming by remember { mutableStateOf("1 hour before") }
    val notificationOptions = listOf("1 week before", "1 day before", "1 hour before", "30 minutes before")

    val scope = rememberCoroutineScope()

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
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button

                            val event = Event(
                                userId = Firebase.auth.currentUser?.uid ?: "",
                                title = title,
                                description = description.ifBlank { null },
                                startTime = selectedDate, // Use selectedDate directly for startTime
                                endTime = Date(selectedDate.time + 60_000), // Add 1 minute to endTime
                                isAllDay = isAllDay,
                                createdAt = Date(),
                                updatedAt = Date(),
                                location = null
                            )

                            scope.launch {
                                try {
                                    eventViewModel.insertEventWithCategory(event, selectedCategory)
                                    // Navigate back AFTER insertion succeeds
                                    withContext(Dispatchers.Main) {
                                        val intent = Intent(context, MainActivity::class.java)
                                        context.startActivity(intent)
                                        (context as Activity).finish()
                                    }
                                } catch (e: Exception) {
                                    Log.e("AddEvent", "Failed to save: ${e.message}")
                                }
                                if (enableNotification) {
                                    NotificationHelper.createNotificationChannel(context)
                                    NotificationHelper.scheduleNotification(context, event)
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

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
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
            verticalArrangement = Arrangement.spacedBy(8.dp) // Adds vertical spacing between rows
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

            // Event type selection
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

            // Second row: Custom (aligned under "Event")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start // Align to the left
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

            // Date and settings
            EventSettingRow(
                icon = Icons.Default.DateRange,
                text = "All-day",
                hasToggle = true,
                initialToggleState = isAllDay,
                onToggleChange = { isAllDay = it }
            )


            EventSettingRow(
                icon = Icons.Default.DateRange,
                text = selectedDate.toString(),
                hasToggle = false,
                onClick = { showDatePicker = true }
            )

            EventSettingRow(
                icon = Icons.Default.Notifications,
                text = "Enable Notification",
                hasToggle = true,
                initialToggleState = enableNotification,
                onToggleChange = { enableNotification = it }
            )

            if (enableNotification) {
                var expanded by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Notify me",
                        color = Color(0xFFBEA99B),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedNotificationTiming,
                            onValueChange = {},
                            label = { Text("Notification Timing", color = Color(0xFFBEA99B)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = backgroundColor,
                                unfocusedContainerColor = backgroundColor
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            notificationOptions.forEach { selectionOption ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        selectedNotificationTiming = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }


        }
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

@Composable
fun EventTypeChip(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFFBEA99B) else Color.Transparent
    val textColor = if (selected) Color.Black else Color(0xFFBEA99B)

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, Color(0xFFBEA99B))
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // Add ellipsis if text is too long
        )
    }
}



@Composable
fun EventSettingRow(
    icon: ImageVector,
    text: String,
    hasToggle: Boolean,
    initialToggleState: Boolean = false,
    onToggleChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    var isToggled by remember { mutableStateOf(initialToggleState) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFBEA99B),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                color = Color(0xFFBEA99B),
                fontSize = 16.sp
            )
        }

        if (hasToggle) {
            Switch(
                checked = isToggled,
                onCheckedChange = {
                    isToggled = it
                    onToggleChange?.invoke(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFBEA99B)
                )
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun AddNewEventScreenPreview() {
    val dummyViewModel = remember {
        object : EventViewModel(EventRepositoryStub()) {
            // Match the return type signature
            override suspend fun insertEventWithCategory(event: Event, category: String): Long {
                // Return dummy value for preview
                return 0L
            }
        }
    }

    AddNewEventScreen(eventViewModel = dummyViewModel)
}