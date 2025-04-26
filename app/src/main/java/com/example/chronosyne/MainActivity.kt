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
                    )
                }
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
    }
}