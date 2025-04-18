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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.repository.UserRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282828)) {
                    LoginScreen()
                }
            }
        }
    }
}

val PlayfairFontFamily = FontFamily(
    Font(R.font.playfairdisplay_regular)
)

val RalewayFontFamily = FontFamily(
    Font(R.font.raleway_regular)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var showForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accountNotFound by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf("") }
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }

    LaunchedEffect(Unit) {
        showForm = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF282828))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Sign In",
            style = TextStyle(
                fontFamily = PlayfairFontFamily,
                fontSize = 48.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFb29985)
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = showForm,
            enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) + fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        inputError = ""
                    },
                    placeholder = { Text("email@domain.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.White,
                        focusedBorderColor = colorResource(id = R.color.main_color),
                        unfocusedBorderColor = colorResource(id = R.color.lighter_main_color)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        inputError = ""
                    },
                    placeholder = { Text("password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedPlaceholderColor = Color.LightGray,
                        unfocusedPlaceholderColor = Color.White,
                        focusedBorderColor = colorResource(id = R.color.main_color),
                        unfocusedBorderColor = colorResource(id = R.color.lighter_main_color)
                    )
                )

                if (inputError.isNotEmpty()) {
                    Text(
                        inputError,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (accountNotFound) {
                    Text(
                        "Account does not exist",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    TextButton(onClick = {
                        context.startActivity(Intent(context, SignupActivity::class.java))
                    }) {
                        Text(
                            "Sign Up",
                            style = TextStyle(
                                fontFamily = RalewayFontFamily,
                                fontSize = 12.sp,
                                color = Color(0xFFb29985)
                            )
                        )
                    }
                }

                TextButton(onClick = { /* TODO: Forgot Password Logic */ }) {
                    Text(
                        "Forgot Password?",
                        style = TextStyle(
                            fontFamily = RalewayFontFamily,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFb29985)
                        )
                    )
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            inputError = "Please enter your email and password"
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            inputError = "Please enter a valid email address"
                        } else {
                            isLoading = true
                            accountNotFound = false
                            inputError = ""

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    userRepository.loginWithEmail(email, password)
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        // Handle successful login here
                                    }
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        accountNotFound = true
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        inputError = "Account does not exist"
                                    }
                                }
                            }
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
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ChronoSyneTheme {
        LoginScreen()
    }
}
