package com.example.chronosyne

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.chronosyne.database.AppDatabase
import com.example.chronosyne.database.relations.EventWithCategories
import com.example.chronosyne.repository.EventRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.example.chronosyne.viewmodel.EventViewModel
import com.example.chronosyne.viewmodel.EventViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventDao = AppDatabase.getInstance(this).eventDao()
        val crossRefDao = AppDatabase.getInstance(this).crossRefDao()
        val repository = EventRepository(
            eventDao = eventDao,
            crossRefDao = crossRefDao,
            auth = Firebase.auth,
            firestore = Firebase.firestore
        )

        val eventViewModel = ViewModelProvider(this, EventViewModelFactory(repository))
            .get(EventViewModel::class.java)



        setContent {
            ChronoSyneTheme {
                MainCalendarScreen(eventViewModel)
            }
        }
    }
}

@Composable
fun LocalActivity(): Activity {
    val context = LocalContext.current
    return context as? Activity
        ?: throw IllegalStateException("Context is not an Activity!")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalendarScreen(eventViewModel: EventViewModel) {
    val backgroundColor = Color(0xFF1E1E1E) // Same as AddNewEvent
    val accentColor = Color(0xFFBEA99B)
    val textColor = Color.White
    val todayColor = Color(0xFF4CAF50)

    val events by eventViewModel.allEvents.collectAsState(initial = emptyList())
    var selectedDate by rememberSaveable { mutableStateOf<Calendar?>(null) }
    var currentMonth by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    val activity = LocalActivity()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendar",
                        color = textColor,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (activity as? Activity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            CalendarView(
                backgroundColor = backgroundColor,
                accentColor = accentColor,
                textColor = textColor,
                todayColor = todayColor,
                events = events,
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                onPreviousMonth = {
                    currentMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, -1)
                    }
                },
                onNextMonth = {
                    currentMonth = Calendar.getInstance().apply {
                        time = currentMonth.time
                        add(Calendar.MONTH, 1)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Events list for selected date
            selectedDate?.let { date ->
                val dateEvents = events.filter { event ->
                    val cal = Calendar.getInstance().apply { time = event.event.startTime }
                    cal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                            cal.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
                }

                if (dateEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Events on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date.time)}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(dateEvents) { event ->
                            EventItem(
                                event = event,
                                onClick = {
                                    val intent = Intent(activity, EventDetailActivity::class.java)
                                    intent.putExtra("EVENT_ID", event.event.eventId.toInt())
                                    activity.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun MonthYearHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
        }

        Text(
            text = SimpleDateFormat("MMMM yyyy").format(currentMonth.time),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row {
            Button(
                onClick = onToday,
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBC9F84),
                    contentColor = Color.White
                )
            ) {
                Text("Today")
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next month")
            }
        }
    }
}

@Composable
fun CalendarView(
    backgroundColor: Color,
    accentColor: Color,
    textColor: Color,
    todayColor: Color,
    events: List<EventWithCategories>,
    currentMonth: Calendar,
    selectedDate: Calendar?,
    onDateSelected: (Calendar) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var showYearDialog by remember { mutableStateOf(false) }
    var localSelectedDate by remember(selectedDate) { mutableStateOf(selectedDate) }

    val calendar = remember(currentMonth) {
        Calendar.getInstance().apply {
            time = currentMonth.time
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val activity = LocalActivity()

    val filteredEvents = remember(events, currentMonth) {
        events.filter { event ->
            val eventCal = Calendar.getInstance().apply { time = event.event.startTime }
            eventCal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                    eventCal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
        }
    }

    val today = Calendar.getInstance()

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Month / Year navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onPreviousMonth() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous month", tint = accentColor)
                }

                TextButton(onClick = { showYearDialog = true }) {
                    Text(
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time),
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = { onNextMonth() }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next month", tint = accentColor)
                }
            }

            // Year Selection Dialog
            if (showYearDialog) {
                val currentYear = currentMonth.get(Calendar.YEAR)
                AlertDialog(
                    onDismissRequest = { showYearDialog = false },
                    title = {
                        Text(
                            text = "Select Year",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items((currentYear - 10..currentYear + 10).toList()) { year ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                        .clickable {
                                            val newMonth = Calendar.getInstance().apply {
                                                time = currentMonth.time
                                                set(Calendar.YEAR, year)
                                            }
                                            onDateSelected(newMonth)
                                            showYearDialog = false
                                        }
                                ) {
                                    Text(
                                        text = year.toString(),
                                        color = accentColor,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showYearDialog = false }) {
                            Text("Cancel", color = accentColor)
                        }
                    },
                    containerColor = backgroundColor
                )
            }

            Spacer(Modifier.height(16.dp))

            // Weekday Headers
            Row(Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = accentColor,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar Grid
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            var dayCounter = 1

            for (week in 0..5) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp) // ⬅️  previously 48.dp — bigger!
                ) {
                    for (dayOfWeek in 0..6) {
                        if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        } else {
                            val currentDay = dayCounter
                            val isToday = (today.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                                    today.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                    today.get(Calendar.DAY_OF_MONTH) == currentDay)

                            CalendarDay(
                                day = currentDay,
                                month = currentMonth.get(Calendar.MONTH),
                                year = currentMonth.get(Calendar.YEAR),
                                isSelected = localSelectedDate?.let {
                                    it.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                                            it.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                            it.get(Calendar.DAY_OF_MONTH) == currentDay
                                } ?: false,
                                isToday = isToday,
                                events = filteredEvents,
                                onEventClick = { eventId ->
                                    val intent = Intent(activity, EventDetailActivity::class.java)
                                    intent.putExtra("EVENT_ID", eventId.toInt())
                                    activity.startActivity(intent)
                                },
                                onDateSelected = { day ->
                                    val selected = Calendar.getInstance().apply {
                                        time = currentMonth.time
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }
                                    localSelectedDate = selected
                                    onDateSelected(selected)
                                },
                                textColor = textColor,
                                todayColor = todayColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                            dayCounter++
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun CalendarDay(
    modifier: Modifier = Modifier,
    day: Int,
    month: Int,
    year: Int,
    isSelected: Boolean,
    isToday: Boolean,
    events: List<EventWithCategories>,
    onEventClick: (Long) -> Unit,
    onDateSelected: (Int) -> Unit,
    textColor: Color,
    todayColor: Color
) {
    val eventOnThisDay = events.find { event ->
        val cal = Calendar.getInstance().apply { time = event.event.startTime }
        cal.get(Calendar.DAY_OF_MONTH) == day
    }

    Box(
        modifier = modifier
            .padding(4.dp) // ⬅️ was 2.dp — increase padding slightly for tap area
            .clip(CircleShape)
            .background(
                when {
                    isToday -> todayColor
                    eventOnThisDay != null -> todayColor.copy(alpha = 0.4f)
                    isSelected -> Color(0xFFBEA99B).copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable {
                if (eventOnThisDay != null) {
                    onEventClick(eventOnThisDay.event.eventId)
                } else {
                    onDateSelected(day)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isToday) Color.White else textColor,
            fontSize = 16.sp, // ⬅️ was 14.sp — now larger day numbers
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}




@Composable
fun EventItem(
    event: EventWithCategories,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBEA99B), // Correct beige-pink
            contentColor = Color.Black // Important! Text inside button/cards is Black
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.event.title ?: "Untitled Event",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(event.event.startTime),
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}
