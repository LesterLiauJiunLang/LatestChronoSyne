package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManageNotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                ManageNotificationScreen(onBackPressed = { finish() })
            }
        }
    }
}

@Composable
fun ManageNotificationScreen(onBackPressed: () -> Unit, profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString()) {
    val backgroundColor = Color(0xFF282828)
    val cardColor = Color(0xFFb29985)
    val textColor = Color(0xFFb29985)
    val currentUser = Firebase.auth.currentUser
    val photoUrl = currentUser?.photoUrl

    val context = LocalContext.current



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
                IconButton(onClick = { context.startActivity(Intent(context, NotificationActivity::class.java)) }) {
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

        ManageNotificationTitle(
            title = "Calendar Notification",
        )


        // Buttons: Change Name / Email / Password
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NotificationItem(title = "Show notifications")
            NotificationItem(title = "Allow Sound")

            Spacer(modifier = Modifier.height(32.dp))

            ManageNotificationTitle(
                title = "Event Notification",
            )

            NotificationItem(title = "Show notifications")
            NotificationItem(title = "Allow Sound")
        }
    }
}
@Composable
fun ManageNotificationTitle(
    title: String,
) {
    val textColor = Color(0xFFb29985)
    val dividerColor = Color(0xFFb29985)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Column {
                Text(
                    modifier = Modifier.padding(bottom = 13.dp),
                    text = title,
                    fontSize = 23.sp,
                    fontFamily = PlayfairFontFamily,
                    color = textColor
                )
            }

            Divider(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                color = dividerColor,
                thickness = 1.dp
            )
        }
    }

@Composable
fun NotificationItem(
    title: String,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
)
{
    var isChecked by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp)) // Make it a nice rounded card
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = RalewayFontFamily
            )

            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = Color(0xFF282828),
                    uncheckedThumbColor = Color(0xFFb29985).copy(alpha = 0.4f),
                    checkedThumbColor = backgroundColor,
                    checkedTrackColor = Color(0xFFb29985),
                    uncheckedBorderColor = Color(0xFFb29985).copy(alpha = 0.4f)
                )
            )
        }
    }
}





