package com.example.chronosyne

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewEventScreen() {
    val backgroundColor = Color(0xFF1E1E1E)
    val buttonColor = Color(0xFFBEA99B)
    val textColor = Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = buttonColor
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { /* Save event */ },
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
                .padding(16.dp)
        ) {
            Text(
                text = "Add Title",
                color = buttonColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Event type selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventTypeChip(text = "Event", selected = true)
                EventTypeChip(text = "Task", selected = false)
                EventTypeChip(text = "Birthday", selected = false)
                EventTypeChip(text = "Custom", selected = false)
            }

            // All-day toggle
            EventSettingRow(
                icon = Icons.Default.DateRange,
                text = "All-day",
                hasToggle = true,
                initialToggleState = true
            )

            // Date selection
            EventSettingRow(
                icon = Icons.Default.DateRange,
                text = "Select Date",
                hasToggle = false
            )

            // Repeat setting
            EventSettingRow(
                icon = Icons.Default.Refresh,
                text = "Does not repeat",
                hasToggle = true,
                initialToggleState = false
            )

            // Notification setting
            EventSettingRow(
                icon = Icons.Default.Notifications,
                text = "Enable Notification",
                hasToggle = true,
                initialToggleState = true
            )

            // Reminder setting
            EventSettingRow(
                icon = Icons.Default.Notifications,
                text = "Enable Reminder",
                hasToggle = true,
                initialToggleState = true,
                description = "1 hour remind"
            )

            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Add details section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = buttonColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Add details",
                    color = buttonColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun EventTypeChip(text: String, selected: Boolean) {
    val backgroundColor = if (selected) Color(0xFFBEA99B) else Color.Transparent
    val textColor = if (selected) Color.Black else Color(0xFFBEA99B)
    val borderColor = Color(0xFFBEA99B)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun EventSettingRow(
    icon: ImageVector,
    text: String,
    hasToggle: Boolean,
    initialToggleState: Boolean = false,
    description: String? = null
) {
    var isToggled by remember { mutableStateOf(initialToggleState) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
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

            Column {
                Text(
                    text = text,
                    color = Color(0xFFBEA99B),
                    fontSize = 16.sp
                )

                if (description != null) {
                    Text(
                        text = description,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (hasToggle) {
            Switch(
                checked = isToggled,
                onCheckedChange = { isToggled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFBEA99B),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNewEventScreenPreview() {
    AddNewEventScreen()
}