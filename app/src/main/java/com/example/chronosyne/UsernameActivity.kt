package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import android.app.Activity
import com.example.chronosyne.repository.UserRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.*
import kotlin.jvm.java

class UsernameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282828)) {
                    UsernameScreen()
                }
            }
        }
    }
}

@Composable
fun UsernameScreen() {
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current
    var inputError by remember { mutableStateOf("") }


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.chronologo),
                contentDescription = "ChronoSyne Logo",
                modifier = Modifier
                    .size(300.dp)
            )

            Text(
                "Enter Your Username",
                modifier = Modifier
                    .fillMaxWidth(),
                style = TextStyle(
                    fontFamily = PlayfairFontFamily,
                    fontSize = 40.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFFb29985),
                    textAlign = TextAlign.Center
                )

            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    inputError = ""
                },
                placeholder = { Text("e.g. chrono_master") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedPlaceholderColor = Color.LightGray,
                    unfocusedPlaceholderColor = Color.White,
                    focusedBorderColor = colorResource(id = R.color.main_color),
                    unfocusedBorderColor = colorResource(id = R.color.lighter_main_color)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (inputError.isNotEmpty()) {
                Text(
                    text = inputError,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontFamily = RalewayFontFamily,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (username.isBlank()) {
                        inputError = "Username cannot be empty"
                    } else {
                        // TODO: Save username to Firestore or Realtime Database
                        Toast.makeText(context, "Username saved!", Toast.LENGTH_SHORT).show()
                        // Navigate to MainActivity
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)

                        // Finish current activity to prevent going back
                        (context as? Activity)?.finish()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 17.dp)
            ) {
                Text(
                    "CONTINUE",
                    style = TextStyle(
                        fontFamily = RalewayFontFamily,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UsernameScreenPreview() {
    ChronoSyneTheme {
        UsernameScreen()
    }
}