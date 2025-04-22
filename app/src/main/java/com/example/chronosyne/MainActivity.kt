package com.example.chronosyne

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.ui.theme.ChronoSyneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronoSyneTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val backgroundColor = Color(0xFF1E1E1E)
    val cardColor = Color(0xFFBEA99B)
    val textColor = Color(0xFFBEA99B)

    Scaffold(
        bottomBar = {
            BottomNavBar(backgroundColor = cardColor)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new event */ },
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
                        IconButton(onClick = { /* Open settings */ }) {
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
                            // Profile image placeholder
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

                Spacer(modifier = Modifier.height(16.dp))

                // Upcoming Events Section
                Text(
                    text = "Upcoming Events",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                EventCard(
                    icon = Icons.Default.Person,
                    title = "Group Study",
                    date = "Date: 20 March 2025",
                    cardColor = cardColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Future Events Section
                Text(
                    text = "Future Events",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                EventCard(
                    icon = Icons.Default.Notifications, // Using as placeholder for birthday icon
                    title = "Lester's Birthday",
                    date = "Date: 26th September 2025",
                    cardColor = cardColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                EventCard(
                    icon = Icons.Default.Person, // Using person icon instead of School
                    title = "Exam",
                    date = "Date: 25th October 2025",
                    cardColor = cardColor
                )
            }
        }
    }
}

@Composable
fun EventCard(
    icon: ImageVector,
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
                imageVector = icon,
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