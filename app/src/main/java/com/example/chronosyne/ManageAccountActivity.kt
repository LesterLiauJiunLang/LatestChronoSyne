package com.example.chronosyne

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.chronosyne.repository.UserRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ManageAccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                ManageAccountScreen(onBackPressed = { finish() })
            }
        }
    }
}

@Composable
fun ManageAccountScreen(onBackPressed: () -> Unit, profilePhotoUrl: String? = Firebase.auth.currentUser?.photoUrl?.toString()) {
    val backgroundColor = Color(0xFF282828)
    val cardColor = Color(0xFFb29985)
    val textColor = Color(0xFFb29985)
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var username by remember { mutableStateOf("") }
    val userRepository = remember { UserRepository() }
    var showNameDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    // Input text states
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

// Event flags to trigger updates
    var performNameUpdate by remember { mutableStateOf(false) }
    var performEmailUpdate by remember { mutableStateOf(false) }
    var performPasswordUpdate by remember { mutableStateOf(false) }



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
                text = "Account",
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

        ManageAccountTitle(
            title = "Account Information",
        )

        suspend fun reloadUsername() {
            val fetchedUsername = userRepository.getUsernameFromFirestore()
            if (fetchedUsername != null) {
                username = fetchedUsername
            }
        }

        if (showChangeNameDialog) {
            InputDialog(
                title = "Change Name",
                onDismiss = { showChangeNameDialog = false },
                onConfirm = { input ->
                    newName = input
                    performNameUpdate = true
                    showChangeNameDialog = false
                }
            )
        }

        if (showChangeEmailDialog) {
            InputDialog(
                title = "Change Email",
                onDismiss = { showChangeEmailDialog = false },
                onConfirm = { input ->
                    newEmail = input
                    performEmailUpdate = true
                    showChangeEmailDialog = false
                }
            )
        }

        if (showChangePasswordDialog) {
            InputDialog(
                title = "Change Password",
                onDismiss = { showChangePasswordDialog = false },
                onConfirm = { input ->
                    newPassword = input
                    performPasswordUpdate = true
                    showChangePasswordDialog = false
                }
            )
        }


        LaunchedEffect(Unit) {
            reloadUsername()
        }


        LaunchedEffect(performNameUpdate) {
            if (performNameUpdate) {
                val success = userRepository.updateUsername(newName)
                if (success) {
                    Firebase.auth.currentUser?.reload()?.await()
                    currentUser = Firebase.auth.currentUser
                    reloadUsername()
                    Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Name update failed.", Toast.LENGTH_SHORT).show()
                }
                performNameUpdate = false
            }
        }


// 4. Update email
        LaunchedEffect(performEmailUpdate) {
            if (performEmailUpdate) {
                val success = userRepository.updateEmail(newEmail)
                if (success) {
                    Firebase.auth.currentUser?.reload()?.await()
                    currentUser = Firebase.auth.currentUser
                    reloadUsername() // optional, if you want
                    Toast.makeText(context, "Email updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Email update failed.", Toast.LENGTH_SHORT).show()
                }
                performEmailUpdate = false
            }
        }

// 5. Update password
        LaunchedEffect(performPasswordUpdate) {
            if (performPasswordUpdate) {
                val success = userRepository.updatePassword(newPassword)
                if (success) {
                    Firebase.auth.currentUser?.reload()?.await()
                    currentUser = Firebase.auth.currentUser
                    Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Password update failed.", Toast.LENGTH_SHORT).show()
                }
                performPasswordUpdate = false
            }
        }


        // Buttons: Change Name / Email / Password
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AccountActionButton(
                text = "Change Name",
                trailingText = username.ifEmpty { "Not Set" }
            ) {
                showChangeNameDialog = true
            }

            AccountActionButton(
                text = "Change Email",
                trailingText = currentUser?.email ?: "Not Set"
            ) {
                showChangeEmailDialog = true
            }

            AccountActionButton(
                text = "Change Password"
            ) {
                showChangePasswordDialog = true
            }

            Spacer(modifier = Modifier.height(32.dp))

            ManageAccountTitle(title = "Account Management")

            AccountActionButton(
                text = "Delete Account",
                backgroundColor = Color.White,
                textColor = Color.Red
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    val success = userRepository.deleteUserAccount()
                    Log.d("DeleteAccount", "Delete account success: $success") // Log the result

                    withContext(Dispatchers.Main) {
                        if (success) {
                            Log.d("DeleteAccount", "Account deletion successful")
                            Toast.makeText(context, "Account deleted.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        } else {
                            Log.e("DeleteAccount", "Deletion failed")
                            Toast.makeText(context, "Deletion failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


            AccountActionButton(
                text = "Sign Out",
                backgroundColor = Color.White,
                textColor = Color.Red
            ) {
                userRepository.signOut()
                val intent = Intent(context, WelcomeActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        }
    }
}
    @Composable
    fun ManageAccountTitle(
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
    fun AccountActionButton(
        text: String,
        backgroundColor: Color = Color.White,
        textColor: Color = Color.Black,
        trailingText: String? = null,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp)) // Make it a nice rounded card
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(vertical = 16.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 16.sp,
                    fontFamily = RalewayFontFamily
                )
                trailingText?.let {
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = it,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontFamily = PlayfairFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // 👈 show "..." if it's too long
                    )
                }
            }
        }
    }


@Composable
fun InputDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val textColor = Color(0xFFb29985)

    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
            text = title,
            fontFamily = PlayfairFontFamily,) },
        text = {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter new value") }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(inputText) },
                    colors = ButtonDefaults.buttonColors(
                    containerColor = textColor,
                contentColor = Color.White
            )) {
                Text("Confirm",
                    fontFamily = RalewayFontFamily,)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )) {
                Text("Cancel",
                    fontFamily = RalewayFontFamily,)
            }
        }
    )
}


