package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: throw Exception("Google ID token not found")

                CoroutineScope(Dispatchers.IO).launch {
                    val userRepository = UserRepository()
                    val result = userRepository.signInWithGoogle(idToken)

                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish() // Add finish() here
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Google Sign-In failed: ${result.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Google sign-in error", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Google Sign-In failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        val prefilledEmail = intent.getStringExtra("prefilled_email")

        setContent {
            ChronoSyneTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF282828)) {
                    LoginScreen(
                        onGoogleSignIn = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                        prefilledEmail = prefilledEmail ?: ""
                    )
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

val RobotoFontFamily = FontFamily(
    Font(R.font.robotomono_regular)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onGoogleSignIn: () -> Unit, prefilledEmail: String = "") {
    var showForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf(prefilledEmail) }
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
                    placeholder = {
                        Text(
                            "email@domain.com",
                            fontFamily = RobotoFontFamily
                        )
                    },
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
                    placeholder = {
                        Text(
                            "password123",
                            fontFamily = RobotoFontFamily
                        )
                    },
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
                                    Log.d("Login", "Login success")
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        // Handle successful login here
                                        val intent = Intent(context, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        inputError = "Account not found. Please sign up."
                                        accountNotFound = true
                                    }
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        inputError = "Incorrect email or password" // More user-friendly message
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        inputError = "Login failed: ${e.message}"
                                        Log.e("Login", "Login failed", e) // Log with stacktrace
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

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onGoogleSignIn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 12.dp)
                    )
                    Text(
                        text = "Sign in with Google",
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    ChronoSyneTheme {
        LoginScreen(
            onGoogleSignIn = {}
        )
    }
}