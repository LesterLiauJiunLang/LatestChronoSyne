package com.example.chronosyne

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
               AboutScreen(onBackPressed = { finish() })
            }
        }
    }
}

@Composable
fun AboutScreen(onBackPressed: () -> Unit, profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString()) {
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
                text = "About",
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

        FAQTitle(
            title = "FAQ",
        )

        LazyColumn(
            contentPadding = PaddingValues(13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FaqItem(
                    question = "How do I manage my notifications?",
                    answer = "Go to Settings > Notifications, from there you can toggle notifications for calendar or your events."
                )
            }
            item {
                FaqItem(
                    question = "How do I add new countdown events?",
                    answer = "Tap the '+' button, enter the event name, date, time, and any optional settings like notifications or images."
                )
            }
            item {
                FaqItem(
                    question = "How do I set reminders for an event?",
                    answer = "While creating or editing an event, turn on notifications and choose when you want to be reminded."
                )
            }

            item {
                FaqItem(
                    question = "How do I delete my account?",
                    answer = "Go to Settings > Account > Delete Account. Please note that this will erase all your data and you cannot delete your account if you signed in with Google."
                )
            }
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    val transitionDuration = 300 // Adjust the duration for a smoother transition

    // Animate the rotation of the icon
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 360f else 0f,
        animationSpec = tween(durationMillis = 300) // Adjust duration for smoothness
    )


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)
            .clickable(
                onClick = { expanded = !expanded },
                indication = null, // Remove the default clickable indication
                interactionSource = remember { MutableInteractionSource() } // Prevent any interaction feedback
            )
            .animateContentSize(tween(durationMillis = transitionDuration))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question,
                fontWeight = FontWeight.Bold,
                fontFamily = RalewayFontFamily,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    // Check if the target phrase exists in the answer
                    if (answer.contains("Please note that this will erase all your data.")) {
                        append(answer.substringBefore("Please note that this will erase all your data."))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Please note this will erase all your data.")
                        }
                        append(answer.substringAfter("Please note that this will erase all your data."))
                    } else {
                        append(answer) // If the phrase doesn't exist, just append the full answer normally
                    }
                },
                fontSize = 14.sp,
                fontFamily = RalewayFontFamily,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun FAQTitle(
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






