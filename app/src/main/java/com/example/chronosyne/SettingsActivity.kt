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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.TimeZone



class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
            SettingsScreen (
                onBackPressed = { finish() }
                )
            }
        }
    }
}


@Composable
fun SettingsScreen(onBackPressed: () -> Unit, profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString()) {
    val backgroundColor = Color(0xFF282828)
    val cardColor = Color(0xFFb29985)
    val textColor = Color(0xFFb29985)
    val currentUser = Firebase.auth.currentUser
    val photoUrl = currentUser?.photoUrl

    val context = LocalContext.current
    var timeZoneEnabled by remember { mutableStateOf(false) }

    val deviceTimeZone = TimeZone.getDefault().id

    // Dynamically change subtitle based on toggle
    val timeZoneSubtitle = if (timeZoneEnabled) {
        "Use device's time zone\n$deviceTimeZone"
    } else {
        "Malaysia Time GMT+8"
    }

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
            text = "Settings",
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
            IconButton(onClick = { /* Notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = textColor
                )
            }


            IconButton(
                onClick = { context.startActivity(Intent(context, ProfileActivity::class.java)) }
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

        Spacer(modifier = Modifier.height(16.dp))

        SettingsItemSection(
            title = "Account",
            subtitle = "Security Notifications, Manage accounts",
            onClick = { context.startActivity(Intent(context, ManageAccountActivity::class.java)) }
        )

        SettingsItemSection(
            title = "Time Zone",
            subtitle = timeZoneSubtitle,
            hasToggle = true,
            toggleValue = timeZoneEnabled,
            onToggleChange = { timeZoneEnabled = it }
        )

        SettingsItemSection(
            title = "Notifications",
            subtitle = "Calendar Notifications\nEvent Notifications",
            onClick = { context.startActivity(Intent(context, ManageNotificationActivity::class.java)) }
        )

        SettingsItemSection(
            title = "About",
            subtitle = "Help Center",
            onClick = {  context.startActivity(Intent(context, AboutActivity::class.java)) }
        )
    }
}


@Composable
fun SettingsItemSection(
    title: String,
    subtitle: String,
    hasToggle: Boolean = false,
    toggleValue: Boolean = false,
    onToggleChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = Color(color = 0xFF282828 )
    val textColor = Color(0xFFb29985)
    val dividerColor = Color(0xFFb29985)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(bottom = 13.dp),
                    text = title,
                    fontSize = 23.sp,
                    fontFamily = PlayfairFontFamily,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    fontFamily = PlayfairFontFamily,
                    color = textColor
                )
            }

            if (hasToggle && onToggleChange != null) {
                Switch(
                    checked = toggleValue,
                    onCheckedChange = onToggleChange,
                    colors = SwitchDefaults.colors(
                        uncheckedTrackColor = backgroundColor,
                        uncheckedThumbColor = textColor.copy(alpha = 0.4f),
                        checkedThumbColor = backgroundColor,    // Circle color when ON
                        checkedTrackColor = textColor, // Track color when ON
                        uncheckedBorderColor = textColor.copy(alpha = 0.4f) //
                    )
                )
            }
        }
        Divider(
            modifier = Modifier.padding(top = 25.dp, bottom = 10.dp),
            color = dividerColor,
            thickness = 1.dp
        )
    }
}

