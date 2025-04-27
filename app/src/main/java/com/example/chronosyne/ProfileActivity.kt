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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase



class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                ProfileScreen (
                    onBackPressed = { finish() },
                    onCameraClick = {
                        // add function camera here
                    }
                )
            }
        }
    }
}


@Composable
fun ProfileScreen(onBackPressed: () -> Unit, profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString(), onCameraClick: () -> Unit)
{
    val backgroundColor = Color(0xFF282828)
    val cardColor = Color(0xFFb29985)
    val textColor = Color(0xFFb29985)
    val currentUser = Firebase.auth.currentUser
    val name = currentUser?.displayName ?: "Unknown"
    val email = currentUser?.email ?: "No email"
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
                text = "Profile",
                color = textColor,
                fontFamily = PlayfairFontFamily,
                fontSize = 28.sp,
                modifier = Modifier.align(Alignment.Center)
            )


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                SettingsActivity::class.java
                            )
                        )
                    }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = textColor
                    )
                }

                IconButton(onClick = { /* Notifications */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        // Profile Image with Camera Icon
        Column(
            modifier = Modifier.fillMaxSize(), // Ensures space for alignment
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(cardColor)
                    .padding(16.dp)
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center),
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .size(36.dp) // Size of the background circle
                    .background(textColor, CircleShape) // Apply background color and make it circular
                    .align(Alignment.CenterHorizontally) // Align it to the center horizontally
                    .padding(4.dp) // Padding inside the circle
            ) {
                IconButton(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxSize() // Make the icon button fill the circle
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Profile Picture",
                        tint = Color.Black // Set the camera icon color to white
                    )
                }
            }

                Spacer(modifier = Modifier.height(32.dp))

                // Name Section
                ProfileItemSection(
                    title = "Name",
                    content = name,
                    icon = Icons.Default.Person
                )

                // Email Section
                ProfileItemSection(
                    title = "Email",
                    content = email,
                    icon = Icons.Default.Email

                )
            }
        }
    }

    @Composable
    fun ProfileItemSection(
        title: String,
        content: String,
        icon: ImageVector
    ) {
        val textColor = Color(0xFFb29985)
        val dividerColor = Color(0xFFb29985)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp) // Space between icon and text
            ) {
                Icon(
                    imageVector = icon, // Use the passed icon
                    contentDescription = title,
                    modifier = Modifier
                        .size(34.dp) // Adjust icon size
                        .padding(end = 8.dp),// Space between icon and title text
                         tint = textColor
                )

                Text(
                    text = title,
                    fontSize = 23.sp,
                    fontFamily = PlayfairFontFamily,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
                Text(
                    text = content,
                    fontSize = 16.sp,
                    fontFamily = PlayfairFontFamily,
                    color = textColor
                )
                Divider(
                    modifier = Modifier.padding(top = 16.dp),
                    color = dividerColor,
                    thickness = 1.dp
                )
            }
        }


