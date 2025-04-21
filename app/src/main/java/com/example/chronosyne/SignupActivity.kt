package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chronosyne.repository.UserRepository
import com.example.chronosyne.ui.theme.ChronoSyneTheme
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.*
import kotlin.jvm.java

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoSyneTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282828)) {
                    SignupScreen()
                }
            }
        }
    }
}



@Composable
fun SignupScreen() {
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
            "Create An Account",
            style = TextStyle(
                fontFamily = PlayfairFontFamily,
                fontSize = 40.sp,
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
                    placeholder = { Text(
                        "email@domain.com",
                        fontFamily = RobotoFontFamily
                    ) },
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
                    placeholder = { Text(
                        "password123",
                        fontFamily = RobotoFontFamily
                    ) },
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
                        text = inputError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontFamily = RalewayFontFamily,
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
                            "SIGN UP",
                            style = TextStyle(
                                fontFamily = RalewayFontFamily,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                TermsAndPrivacyText()

                Button(
                    onClick = {
                        // TODO: Handle Google sign-in
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google icon",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )

                    Text(
                        "Continue with Google",
                        style = TextStyle(
                            fontFamily = RalewayFontFamily,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TermsAndPrivacyText() {
    val openDialog = remember { mutableStateOf(false) }
    val dialogText = remember { mutableStateOf("") }

    val annotatedText = buildAnnotatedString {
        append("By clicking continue, you agree to our ")

        // Terms of Service
        pushStringAnnotation(tag = "TOS", annotation = "tos")
        withStyle(style = SpanStyle(color = Color(0xFFb29985))) {
            append("Terms of Service")
        }
        pop()

        append(" and ")

        // Privacy Policy
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(style = SpanStyle(color = Color(0xFFb29985))) {
            append("Privacy Policy")
        }
        pop()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ClickableText(
            text = annotatedText,
            onClick = { offset ->
                annotatedText.getStringAnnotations(offset, offset).firstOrNull()?.let {
                    when (it.tag) {
                        "TOS" -> {
                            dialogText.value = "Here are our Terms of Service..." // Replace with your actual content
                            openDialog.value = true
                        }
                        "PRIVACY" -> {
                            dialogText.value = "Here is our Privacy Policy..."
                            openDialog.value = true
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontFamily = RalewayFontFamily,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text("Info")
            },
            text = {
                Text(dialogText.value)
            },
            confirmButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignupScreenPreview() {
    ChronoSyneTheme {
        SignupScreen()
    }
}
